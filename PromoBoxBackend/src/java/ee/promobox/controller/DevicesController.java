/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ee.promobox.controller;

import ee.promobox.KioskConfig;
import ee.promobox.entity.AdCampaigns;
import ee.promobox.entity.CampaignsFiles;
import ee.promobox.entity.Devices;
import ee.promobox.entity.DevicesCampaigns;
import ee.promobox.entity.DevicesDisplays;
import ee.promobox.entity.ErrorLog;
import ee.promobox.entity.UsersDevicesPermissions;
import ee.promobox.jms.MailDto;
import ee.promobox.service.Session;
import ee.promobox.service.SessionService;
import ee.promobox.service.UserService;
import ee.promobox.util.ResponseUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.jms.Destination;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 *
 * @author Dan
 */
@Controller
public class DevicesController {

    public static final int CHECK_PERIOD = 15 * 60 * 1000;
    public static final int CHECK_COUNT_BEFORE_EMAIL = 3;

    private final static Logger log = LoggerFactory.getLogger(
            DevicesController.class);

    private final static SimpleDateFormat timeFormat = new SimpleDateFormat("H:mm");

    @Autowired
    private UserService userService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private KioskConfig config;

    @Autowired
    @Qualifier("mailDestination")
    private Destination mailDestination;
    @Autowired
    private JmsTemplate jmsTemplate;

    @Scheduled(fixedDelay = CHECK_PERIOD)
    public void checkDeviceStatus() {
        for (Devices d : userService.findAllDevices()) {
            if (System.currentTimeMillis() - d.getLastDeviceRequestDt().getTime() >= CHECK_PERIOD) {
                if (d.getStatus() != Devices.STATUS_AHRCHIVED
                        && d.getStatus() != Devices.STATUS_OFFLINE) {
                    if (d.getStatus() == Devices.STATUS_ONLINE || d.getStatus() == Devices.STATUS_USED) {
                        if (d.getOnOffCounterCheck() >= d.getOnOffCheckNumber()) {
                            d.setOnOffCounterCheck(0);
                        } else {
                            d.setOnOffCounterCheck(d.getOnOffCounter());
                        }
                    }
                    d.setStatus(Devices.STATUS_OFFLINE);
                }

                if (d.getStatus() == Devices.STATUS_OFFLINE) {
                    d.setOnOffCounterCheck(d.getOnOffCounterCheck() + 1);
                    d.setOnOffCounter(d.getOnOffCounter() + 1);

                    if (d.getOnOffCounterCheck() == d.getOnOffCheckNumber()) {
                        d.setStatePeriod(d.getStatePeriod()
                                + (d.getOnOffCounter() - d.getOnOffCheckNumber()) * CHECK_PERIOD);
                        sendDeviceEmail(d, "OFFLINE!");
                        d.setOnOffCounter(d.getOnOffCounterCheck());
                    }

                    userService.updateDevice(d);
                }

            } else if (d.getStatus() == Devices.STATUS_ONLINE) {
                d.setOnOffCounterCheck(d.getOnOffCounterCheck() + 1);
                d.setOnOffCounter(d.getOnOffCounter() + 1);

                if (d.getOnOffCounterCheck() == d.getOnOffCheckNumber()) {
                    d.setStatePeriod(d.getStatePeriod()
                            + (d.getOnOffCounter() - d.getOnOffCheckNumber()) * CHECK_PERIOD);
                    sendDeviceEmail(d, "ONLINE!");
                    d.setOnOffCounter(d.getOnOffCounterCheck());
                }

                userService.updateDevice(d);
            }
        }
    }

    @RequestMapping("/device/{uuid}/saveError")
    public @ResponseBody
    String saveError(
            @PathVariable("uuid") String uuid,
            @RequestParam String error,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        JSONObject resp = new JSONObject();
        Devices d = userService.findDeviceByUuid(uuid);
        if (d != null) {
            JSONObject jsonError = new JSONObject(error);

            String name = StringUtils.abbreviate(jsonError.getString("name"), 255);
            String message = StringUtils.abbreviate(jsonError.getString("message"), 255);
            String stackTrace = jsonError.getString("stackTrace");

            ErrorLog errorLog = new ErrorLog();
            errorLog.setDeviceId(d.getId());
            errorLog.setName(name);
            errorLog.setMessage(message);
            errorLog.setStackTrace(stackTrace);
            errorLog.setCreatedDt(new Date(jsonError.getLong("date")));

            userService.addErrorLog(errorLog);
            response.setStatus(HttpServletResponse.SC_OK);
            resp.put(ResponseUtils.RESULT, ResponseUtils.OK);
        }

        return resp.toString();
    }

    @RequestMapping("/device/{uuid}/pull")
    public @ResponseBody
    String showCampaign(
            @PathVariable("uuid") String uuid,
            @RequestParam String json,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        JSONObject resp = new JSONObject();
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        Devices d = userService.findDeviceByUuid(uuid);

        if (d != null) {
            resp.put("currentDt", new Date().getTime());

            JSONObject objectGiven = new JSONObject(json);

            d.setFreeSpace(objectGiven.has("freeSpace") ? objectGiven.getLong("freeSpace") : 0);
            d.setCache(objectGiven.has("cache") ? objectGiven.getLong("cache") : 0);
            d.setCurrentFileId(objectGiven.has("currentFileId") ? objectGiven.getInt("currentFileId") : null);
            d.setCurrentCampaignId(objectGiven.has("currentCampaignId") ? objectGiven.getInt("currentCampaignId") : null);
            d.setLoadingCampaignId(objectGiven.has("loadingCampaingId") ? objectGiven.getInt("loadingCampaingId") : null);
            d.setLoadingCampaignProgress(objectGiven.has("loadingCampaingProgress") ? objectGiven.getInt("loadingCampaingProgress") : null);

            boolean onTop = objectGiven.has("isOnTop") ? objectGiven.getBoolean("isOnTop") : true;
            d.setOnTop(onTop);

            if (objectGiven.has("ip")) {
                d.setNetworkData(objectGiven.getJSONArray("ip").toString());
            }

            if (objectGiven.has("errors")) {
                JSONArray errors = objectGiven.getJSONArray("errors");
                for (int i = 0; i < errors.length(); i++) {
                    JSONObject jsonError = errors.getJSONObject(i);

                    String name = StringUtils.abbreviate(jsonError.getString("name"), 255);
                    String message = StringUtils.abbreviate(jsonError.getString("message"), 255);
                    String stackTrace = jsonError.getString("stackTrace");

                    ErrorLog errorLog = new ErrorLog();
                    errorLog.setDeviceId(d.getId());
                    errorLog.setName(name);
                    errorLog.setMessage(message);
                    errorLog.setStackTrace(stackTrace);
                    errorLog.setCreatedDt(new Date(jsonError.getLong("date")));

                    userService.addErrorLog(errorLog);
                }
            }

            resp.put("audioOut", d.getAudioOut());

            resp.put("nextFile", d.getNextFile());

            resp.put("lastUpdate", d.getLastDeviceRequestDt().getTime());
            resp.put("orientation", d.getOrientation());
            resp.put("clearCache", d.isClearCache());
            resp.put("openApp", d.isOpenApp());

            resp.put("videoWall", d.isVideoWall());
            resp.put("resolutionVertical", d.getResolutionVertical());
            resp.put("resolutionHorizontal", d.getResolutionHorizontal());

            JSONArray displaysArray = new JSONArray();
            for (DevicesDisplays display : userService.findDevicesDisplays(d.getId())) {
                JSONObject displayJson = new JSONObject();
                displayJson.put("displayId", display.getDisplayId());
                displayJson.put("deviceId", display.getDeviceId());
                displayJson.put("point1", display.getPoint1());
                displayJson.put("point2", display.getPoint2());
                displayJson.put("point3", display.getPoint3());
                displayJson.put("point4", display.getPoint4());

                displaysArray.put(displayJson);
            }
            resp.put("displays", displaysArray);

            d.setOpenApp(false);
            d.setClearCache(false);
            d.setNextFile(null);

            JSONArray campaigns = new JSONArray();

            DevicesCampaigns dc = userService.findLastUpdatedDeviceCampaign(d.getId());

            if (dc != null) {

                if (dc.getUpdatedDt().after(d.getLastDeviceRequestDt()) || objectGiven.has("force")) {

                    for (AdCampaigns ad : userService.findCampaignByDeviceId(d.getId())) {
                        JSONObject campaign = new JSONObject();

                        campaign.put("campaignId", ad.getId());
                        campaign.put("campaignName", ad.getName());
                        campaign.put("campaignStatus", ad.getStatus());
                        campaign.put("clientId", ad.getClientId());
                        campaign.put("startDate", ad.getStart().getTime());
                        campaign.put("endDate", ad.getFinish().getTime());
                        campaign.put("sequence", ad.getSequence());
                        campaign.put("duration", ad.getDuration());
                        campaign.put("updateDate", ad.getUpdateDate().getTime());

                        try {
                            JSONObject workTimeData = new JSONObject(ad.getWorkTimeData());

                            campaign.put("days", workTimeData.getJSONArray("days"));
                            campaign.put("hours", workTimeData.getJSONArray("hours"));

                        } catch (Exception ex) {
                            log.error(ex.getMessage(), ex);
                        }

                        List<CampaignsFiles> campaignFiles = userService.findUsersCampaignFiles(ad.getId(), d.getClientId());

                        if (!campaignFiles.isEmpty()) {
                            JSONArray jsonCampaignFiles = new JSONArray();

                            for (CampaignsFiles file : campaignFiles) {
                                if (file.getStatus() == CampaignsFiles.STATUS_ACTIVE) {
                                    JSONObject jsonCampaignFile = new JSONObject();

                                    jsonCampaignFile.put("id", file.getId());
                                    jsonCampaignFile.put("type", file.getFileType().intValue());
                                    jsonCampaignFile.put("size", file.getSize());
                                    jsonCampaignFile.put("orderId", file.getOrderId());
                                    jsonCampaignFile.put("name", file.getFilename());

                                    if (file.getUpdatedDt() != null) {
                                        jsonCampaignFile.put("updatedDt", file.getUpdatedDt().getTime());
                                    }

                                    jsonCampaignFiles.put(jsonCampaignFile);
                                }
                            }

                            campaign.put("files", jsonCampaignFiles);
                        }

                        campaigns.put(campaign);
                    }

                    resp.put("campaigns", campaigns);
                }

                if (d.getStatus() == Devices.STATUS_OFFLINE) {
                    if (d.getOnOffCounterCheck() >= d.getOnOffCheckNumber()) {
                        d.setOnOffCounterCheck(0);
                    } else {
                        d.setOnOffCounterCheck(d.getOnOffCounter());
                    }
                }

                d.setLastDeviceRequestDt(new Date());
                d.setStatus(onTop ? Devices.STATUS_ONLINE : Devices.STATUS_USED);

                userService.updateDevice(d);

                response.setStatus(HttpServletResponse.SC_OK);

                return resp.toString();

            } else {
                resp.put("status", "error");
                resp.put("error", "no_active_campaign");

                response.setStatus(HttpServletResponse.SC_OK);

                return resp.toString();
            }

        } else {
            resp.put("status", "error");
            resp.put("error", "not_found_device");

            response.setStatus(HttpServletResponse.SC_NOT_FOUND);

            return resp.toString();
        }
    }

    private void sendDeviceEmail(Devices d, String status) {
        SimpleDateFormat dt = new SimpleDateFormat("dd.MM.yyyy hh:mm");
        String now = dt.format(new Date());

        MailDto mailDto = new MailDto();
        mailDto.setFrom("no-reply@promobox.ee");
        mailDto.setSubject("Device " + d.getUuid() + " " + status + " (" + now + ")");
        mailDto.setTo(config.getDeviceAdmin());
        mailDto.setHtml(false);

        StringBuilder text = new StringBuilder();
        text.append("Device: " + d.getUuid() + "\n");
        text.append("Description: " + d.getDescription() + "\n");
        text.append("Status: " + status + "\n");
        text.append("Date: " + now + "\n");

        PeriodFormatter formatter = new PeriodFormatterBuilder()
                .printZeroNever()
                .appendDays()
                .appendSuffix(" day ", " days ")
                .printZeroAlways()
                .appendHours()
                .appendSeparator(":")
                .minimumPrintedDigits(2)
                .appendMinutes()
                .toFormatter();

        Period period = new Period(d.getStatePeriod());

        text.append("Time between states: " + formatter.print(period.normalizedStandard()) + "\n");
        d.setStatePeriod(0);

        mailDto.setText(text.toString());

        jmsTemplate.convertAndSend(mailDestination, mailDto);
    }

    @RequestMapping(value = "token/{token}/devices", method = RequestMethod.GET)
    public @ResponseBody
    String showAllDevices(
            @PathVariable("token") String token,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        JSONObject resp = new JSONObject();
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        Session session = sessionService.findSession(token);

        if (session != null) {
            int clientId = session.getClientId();

            List<Devices> devices = null;
            if (session.isAdmin()) {
                devices = userService.findUserDevieces(clientId);
            } else {
                devices = userService.findUserDevieces(clientId, session.getUserId());
            }

            JSONArray devicesArray = new JSONArray();
            if (!devices.isEmpty()) {
                for (Devices d : devices) {
                    JSONObject jsonD = new JSONObject();

                    jsonD.put("id", d.getId());
                    jsonD.put("uuid", d.getUuid());

                    if ((System.currentTimeMillis() - d.getLastDeviceRequestDt().getTime()) > 5 * 60 * 1000) {
                        jsonD.put("status", Devices.STATUS_OFFLINE);
                    } else {
                        jsonD.put("status", d.getStatus());
                    }

                    jsonD.put("space", d.getFreeSpace());
                    jsonD.put("cache", d.getCache());
                    jsonD.put("currentFileId", d.getCurrentFileId());
                    jsonD.put("loadingCampaingId", d.getLoadingCampaignId());
                    jsonD.put("loadingCampaingProgress", d.getLoadingCampaignProgress());
                    jsonD.put("orientation", d.getOrientation());
                    jsonD.put("resolution", d.getResolution());
                    jsonD.put("audioOut", d.getAudioOut());
                    jsonD.put("lastRequestDt", d.getLastDeviceRequestDt().getTime());
                    jsonD.put("onTop", d.isOnTop());

                    if (session.isAdmin()) {
                        jsonD.put("permissionWrite", true);
                    } else {
                        jsonD.put("permissionWrite", checkWritePermission(session, d.getId()));
                    }

                    if (d.getCurrentCampaignId() != null) {
                        AdCampaigns campaign = userService.findCampaignByIdAndClientId(d.getCurrentCampaignId(), session.getClientId());
                        if (campaign != null) {
                            JSONObject currentCampaign = new JSONObject();
                            currentCampaign.put("id", campaign.getId());
                            currentCampaign.put("name", campaign.getName());

                            if (d.getCurrentFileId() != null) {
                                CampaignsFiles currentFile = userService.findCampaignFile(d.getCurrentFileId(), session.getClientId());
                                if (currentFile != null) {
                                    currentCampaign.put("file", currentFile.getFilename());
                                }
                            }

                            jsonD.put("currentCamp", currentCampaign);
                        }
                    }

                    if (d.getLoadingCampaignId() != null) {
                        AdCampaigns campaign = userService.findCampaignByIdAndClientId(d.getLoadingCampaignId(), session.getClientId());
                        if (campaign != null) {
                            JSONObject loadingCampaign = new JSONObject();
                            loadingCampaign.put("id", campaign.getId());
                            loadingCampaign.put("name", campaign.getName());

                            jsonD.put("loadingCamp", loadingCampaign);
                        }
                    }

                    jsonD.put("workStartAt", formatTimeString(d.getWorkStartAt()));
                    jsonD.put("workEndAt", formatTimeString(d.getWorkEndAt()));

                    jsonD.put("mo", d.isMon());
                    jsonD.put("tu", d.isTue());
                    jsonD.put("we", d.isWed());
                    jsonD.put("th", d.isThu());
                    jsonD.put("fr", d.isFri());
                    jsonD.put("sa", d.isSat());
                    jsonD.put("su", d.isSun());

                    jsonD.put("description", d.getDescription());

                    List<AdCampaigns> acs = userService.findCampaignByDeviceId(d.getId());

                    if (acs != null) {
                        JSONArray campaignIds = new JSONArray();
                        for (AdCampaigns ac : acs) {
                            campaignIds.put(ac.getId());
                        }
                        jsonD.put("campaignIds", campaignIds);
                    } else {
                        jsonD.put("campaignIds", -1);
                    }

                    List<AdCampaigns> adCampaignses = null;
                    if (session.isAdmin()) {
                        adCampaignses = userService.findUserAdCompaigns(session.getClientId());
                    } else {
                        adCampaignses = userService.findUserAdCompaigns(session.getClientId(), session.getUserId());
                    }

                    JSONArray array = new JSONArray();

                    for (AdCampaigns a : adCampaignses) {
                        JSONObject aObj = new JSONObject();

                        aObj.put("id", a.getId());
                        aObj.put("name", a.getName());

                        if (acs != null) {
                            aObj.put("playing", d.getCurrentCampaignId());
                        } else {
                            aObj.put("playing", false);
                        }

                        array.put(aObj);
                    }

                    resp.put("campaigns", array);

                    devicesArray.put(jsonD);
                }
            }

            resp.put("devices", devicesArray);

            response.setStatus(HttpServletResponse.SC_OK);

            return resp.toString();
        } else {
            ResponseUtils.sendUnauthorized(response);
        }

        return null;
    }

    @RequestMapping(value = "token/{token}/devicesCampaigns", method = RequestMethod.GET)
    public @ResponseBody
    String showAllDevicesCampaignsShort(
            @PathVariable("token") String token,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        JSONObject resp = new JSONObject();
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        Session session = sessionService.findSession(token);

        if (session != null) {
            int clientId = session.getClientId();

            List<Devices> devices = null;
            if (session.isAdmin()) {
                devices = userService.findUserDevieces(clientId);
            } else {
                devices = userService.findUserDevieces(clientId, session.getUserId());
            }

            JSONArray devicesArray = new JSONArray();
            if (!devices.isEmpty()) {
                for (Devices d : devices) {
                    JSONObject jsonD = new JSONObject();

                    jsonD.put("id", d.getId());
                    jsonD.put("uuid", d.getUuid());

                    List<AdCampaigns> acs = userService.findCampaignByDeviceId(d.getId());

                    if (acs != null) {
                        JSONArray campaigns = new JSONArray();
                        for (AdCampaigns ac : acs) {
                            JSONObject campaign = new JSONObject();
                            campaign.put("id", ac.getId());
                            campaign.put("name", ac.getName());
                            campaigns.put(campaign);
                        }
                        jsonD.put("campaigns", campaigns);
                    }

                    devicesArray.put(jsonD);
                }
            }

            resp.put("devices", devicesArray);

            response.setStatus(HttpServletResponse.SC_OK);

            return resp.toString();
        } else {
            ResponseUtils.sendUnauthorized(response);
        }

        return null;
    }

    @RequestMapping(value = "token/{token}/devices/{id}", method = RequestMethod.DELETE)
    public @ResponseBody
    String deleteDevice(
            @PathVariable("token") String token,
            @PathVariable("id") int id,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        JSONObject resp = new JSONObject();
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        Session session = sessionService.findSession(token);

        if (session != null && checkWritePermission(session, id)) {

            Devices device = userService.findDeviceByIdAndClientId(id, session.getClientId());

            device.setStatus(Devices.STATUS_AHRCHIVED);

            userService.updateDevice(device);

            response.setStatus(HttpServletResponse.SC_OK);

            return resp.toString();

        } else {
            ResponseUtils.sendUnauthorized(response);
        }

        return null;
    }

    @RequestMapping(value = "token/{token}/devices/{id}/campaign/{campaignId}", method = RequestMethod.DELETE)
    public @ResponseBody
    String deleteDeviceCampaign(
            @PathVariable("token") String token,
            @PathVariable("id") int id,
            @PathVariable("campaignId") int campaignId,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        JSONObject resp = new JSONObject();
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        Session session = sessionService.findSession(token);

        boolean ok = deleteDeviceCampaign(session, id, campaignId);

        if (ok) {
            response.setStatus(HttpServletResponse.SC_OK);
            return resp.toString();
        }

        ResponseUtils.sendUnauthorized(response);

        return null;
    }

    @RequestMapping(value = "token/{token}/devices/deleteAndSetCampaign", method = RequestMethod.PUT)
    public @ResponseBody
    String deleteOrSetCampaignToDevices(
            @PathVariable("token") String token,
            @RequestBody String jsonString,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        JSONObject resp = new JSONObject();
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        Session session = sessionService.findSession(token);
        if (session != null) {
            JSONObject json = new JSONObject(jsonString);
            JSONArray devicesJSON = json.getJSONArray("devices");
            int campaignId = json.getInt("campaignId");
            boolean allOK = true;
            JSONArray couldntSet = new JSONArray();
            JSONArray couldntDelete = new JSONArray();
            for (int i = 0; i < devicesJSON.length(); i++) {
                JSONObject device = devicesJSON.getJSONObject(i);
                int deviceId = device.getInt("id");
                boolean added = device.getBoolean("selected");
                if (!added) {
                    boolean ok = deleteDeviceCampaign(session, deviceId, campaignId);
                    if (!ok) {
                        couldntDelete.put(device);
                    }
                    allOK &= ok;
                } else {
                    boolean ok = addDeviceCampaign(session, deviceId, campaignId);
                    if (!ok) {
                        couldntDelete.put(device);
                    }
                    allOK &= ok;
                }
            }
            if (allOK) {
                resp.put(ResponseUtils.RESULT, ResponseUtils.OK);
            } else {
                resp.put("couldntDelete", couldntDelete);
                resp.put("couldntSet", couldntSet);
                resp.put(ResponseUtils.RESULT, ResponseUtils.ERROR);
            }
            response.setStatus(HttpServletResponse.SC_OK);
            return resp.toString();
        } else {
            ResponseUtils.sendUnauthorized(response);
        }
        return null;
    }

    @RequestMapping(value = "token/{token}/devices/{id}", method = RequestMethod.PUT)
    public @ResponseBody
    String updateDevice(
            @PathVariable("token") String token,
            @PathVariable("id") int id,
            @RequestBody String json,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        JSONObject resp = new JSONObject();
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        Session session = sessionService.findSession(token);

        if (session != null && checkWritePermission(session, id)) {
            int clientId = session.getClientId();

            Devices device = userService.findDeviceByIdAndClientId(id, clientId);

            if (device != null) {

                JSONObject deviceUpdate = new JSONObject(json);

                device.setOrientation(deviceUpdate.getInt("orientation"));
                device.setResolution(deviceUpdate.getInt("resolution"));
                device.setDescription(deviceUpdate.getString("description"));
                device.setAudioOut(deviceUpdate.getInt("audioOut"));

                device.setWorkStartAt(parseTimeString(deviceUpdate.getString("workStartAt")));
                device.setWorkEndAt(parseTimeString(deviceUpdate.getString("workEndAt")));

                device.setMon(deviceUpdate.getBoolean("mo"));
                device.setTue(deviceUpdate.getBoolean("tu"));
                device.setWed(deviceUpdate.getBoolean("we"));
                device.setThu(deviceUpdate.getBoolean("th"));
                device.setFri(deviceUpdate.getBoolean("fr"));
                device.setSat(deviceUpdate.getBoolean("sa"));
                device.setSun(deviceUpdate.getBoolean("su"));

                for (int i = 0; i < deviceUpdate.getJSONArray("campaignIds").length(); i++) {
                    int campaignId = deviceUpdate.getJSONArray("campaignIds").getInt(i);

                    DevicesCampaigns devicesCampaigns = userService.findDeviceCampaignByCampaignId(device.getId(), campaignId);

                    if (devicesCampaigns == null) {

                        AdCampaigns newAd = userService.findCampaignByCampaignId(campaignId);

                        boolean timeIntersection = false;
                        String intersectionName = "";
                        for (AdCampaigns ad : userService.findCampaignByDeviceId(device.getId())) {
                            if (checkTimeIntersection(newAd, ad)) {
                                timeIntersection = true;
                                intersectionName = ad.getName();

                                break;
                            }
                        }

                        if (!timeIntersection) {
                            resp.put(ResponseUtils.WARN, "time_intersection");
                            resp.put("name", intersectionName);
                        }

                        devicesCampaigns = new DevicesCampaigns();

                        devicesCampaigns.setDeviceId(device.getId());
                        devicesCampaigns.setAdCampaignsId(campaignId);
                        devicesCampaigns.setUpdatedDt(new Date());

                        userService.addDeviceAdCampaign(devicesCampaigns);

                        userService.updateDevice(device);

                    } else {
                        devicesCampaigns.setAdCampaignsId(campaignId);
                        devicesCampaigns.setUpdatedDt(new Date());

                        userService.updateDeviceAdCampaign(devicesCampaigns);

                        userService.updateDevice(device);
                    }
                }

                response.setStatus(HttpServletResponse.SC_OK);

                return resp.toString();

            } else {
                ResponseUtils.sendUnauthorized(response);
            }
        }

        return null;
    }

    public static boolean checkTimeIntersection(AdCampaigns campaign1, AdCampaigns campaign2) {
        try {
            boolean startCheck = campaign1.getStart().after(campaign2.getStart()) && campaign1.getStart().before(campaign2.getFinish());
            boolean finishCheck = campaign1.getFinish().after(campaign2.getStart()) && campaign1.getFinish().before(campaign2.getFinish());

            if (startCheck || finishCheck) {
                JSONObject workTime1 = new JSONObject(campaign1.getWorkTimeData());
                JSONObject workTime2 = new JSONObject(campaign2.getWorkTimeData());

                boolean daysMatch = false;
                List<String> workDays1 = new ArrayList<>();
                for (int i = 0; i < workTime1.getJSONArray("days").length(); i++) {
                    workDays1.add(workTime1.getJSONArray("days").getString(i));
                }

                for (int i = 0; i < workTime2.getJSONArray("days").length(); i++) {
                    if (workDays1.contains(workTime2.getJSONArray("days").getString(i))) {
                        daysMatch = true;

                        break;
                    }
                }

                if (daysMatch) {
                    List<String> workHours1 = new ArrayList<>();
                    for (int i = 0; i < workTime1.getJSONArray("hours").length(); i++) {
                        workHours1.add(workTime1.getJSONArray("hours").getString(i));
                    }

                    for (int i = 0; i < workTime2.getJSONArray("hours").length(); i++) {
                        if (workHours1.contains(workTime2.getJSONArray("hours").getString(i))) {
                            return true;
                        }
                    }
                }
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }

        return false;
    }

    @RequestMapping(value = "token/{token}/devices/{id}/clearcache", method = RequestMethod.PUT)
    public @ResponseBody
    String clearDeviceCache(
            @PathVariable("token") String token,
            @PathVariable("id") int id,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        JSONObject resp = new JSONObject();
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        Session session = sessionService.findSession(token);

        if (session != null && checkWritePermission(session, id)) {
            int clientId = session.getClientId();

            Devices device = userService.findDeviceByIdAndClientId(id, clientId);

            if (device != null) {

                device.setClearCache(true);

                userService.updateDevice(device);

                response.setStatus(HttpServletResponse.SC_OK);

                return resp.toString();

            } else {
                ResponseUtils.sendUnauthorized(response);
            }
        }

        return null;
    }

    @RequestMapping(value = "token/{token}/devices/{id}/openapp", method = RequestMethod.PUT)
    public @ResponseBody
    String openApp(
            @PathVariable("token") String token,
            @PathVariable("id") int id,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        JSONObject resp = new JSONObject();
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        Session session = sessionService.findSession(token);

        if (session != null) {
            int clientId = session.getClientId();

            Devices device = userService.findDeviceByIdAndClientId(id, clientId);

            if (device != null) {

                device.setOpenApp(true);

                userService.updateDevice(device);

                response.setStatus(HttpServletResponse.SC_OK);

                return resp.toString();

            } else {
                ResponseUtils.sendUnauthorized(response);
            }
        }

        return null;
    }

    private Date parseTimeString(String timeString) {
        Calendar cal = GregorianCalendar.getInstance();

        try {
            String[] timeParts = timeString.split(":");
            int hour = Integer.parseInt(timeParts[0]);
            int min = Integer.parseInt(timeParts[1]);
            cal.set(0, 0, 0, hour, min, 0);

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }

        return cal.getTime();
    }

    private String formatTimeString(Date time) {
        return timeFormat.format(time);
    }

    @RequestMapping(value = "token/{token}/devices", method = RequestMethod.POST)
    public @ResponseBody
    String createDevice(
            @PathVariable("token") String token,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        JSONObject resp = new JSONObject();
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        Session session = sessionService.findSession(token);

        if (session != null && session.isAdmin()) {

            Devices device = new Devices();

            device.setClientId(session.getClientId());

            device.setOrientation(Devices.ORIENTATION_LANDSCAPE);
            device.setResolution(Devices.RESOLUTION_1920X1080);
            device.setFreeSpace(0);
            device.setLastDeviceRequestDt(new Date());
            device.setStatus(Devices.STATUS_USED);
            device.setAudioOut(Devices.AUDIO_OUT_HDMI);
            device.setUuid(userService.findDeviceUuid());
            device.setDescription("");
            device.setNetworkData("");
            device.setCreatedDt(new Date());

            device.setWorkStartAt(parseTimeString("0:00"));
            device.setWorkEndAt(parseTimeString("0:00"));

            device.setMon(true);
            device.setTue(true);
            device.setWed(true);
            device.setThu(true);
            device.setFri(true);
            device.setSat(true);
            device.setSun(true);

            userService.addDevice(device);

            resp.put("id", device.getId());
            resp.put("uuid", device.getUuid());
            resp.put("status", device.getStatus());
            resp.put("freeSpace", device.getFreeSpace());
            resp.put("cache", device.getCache());
            resp.put("orientation", device.getOrientation());
            resp.put("resolution", device.getResolution());
            resp.put("description", device.getDescription());
            resp.put("audioOut", device.getAudioOut());

            resp.put("workStartAt", formatTimeString(device.getWorkStartAt()));
            resp.put("workEndAt", formatTimeString(device.getWorkEndAt()));

            resp.put("mon", device.isMon());
            resp.put("tue", device.isTue());
            resp.put("wed", device.isWed());
            resp.put("thu", device.isThu());
            resp.put("fir", device.isFri());
            resp.put("sat", device.isSat());
            resp.put("sun", device.isSun());

            response.setStatus(HttpServletResponse.SC_OK);

            return resp.toString();

        } else {
            ResponseUtils.sendUnauthorized(response);
        }

        return null;
    }

    private boolean checkWritePermission(Session session, int deviceId) {
        if (session.isAdmin()) {
            return true;
        }

        UsersDevicesPermissions permission = userService.findUsersDevicesPermissions(session.getUserId(), deviceId);

        return permission == null ? false : permission.isPermissionWrite();
    }

    @ExceptionHandler(Exception.class)
    public void handleAllException(Exception ex) {
        log.error(ex.getMessage(), ex);
    }

    private boolean addDeviceCampaign(Session session, int deviceId, int campaignId) {
        if (session != null && checkWritePermission(session, deviceId)) {
            
            DevicesCampaigns devicesCampaigns = userService.findDeviceCampaignByCampaignId(deviceId, campaignId);
            AdCampaigns campaign = userService.findCampaignByCampaignId(campaignId);
            Devices device = userService.findDeviceByIdAndClientId(deviceId, session.getClientId());
            
            if (devicesCampaigns == null && campaign!= null && device != null) {


                devicesCampaigns = new DevicesCampaigns();

                devicesCampaigns.setDeviceId(deviceId);
                devicesCampaigns.setAdCampaignsId(campaignId);
                devicesCampaigns.setUpdatedDt(new Date());

                userService.addDeviceAdCampaign(devicesCampaigns);

                userService.updateDevice(device);
                
                return true;
            }
        }
        return false;
    }

    private boolean deleteDeviceCampaign(Session session, int deviceId, int campaignId) {
        if (session != null && checkWritePermission(session, deviceId)) {

            Devices device = userService.findDeviceByIdAndClientId(deviceId, session.getClientId());
            DevicesCampaigns devicesCampaign = userService.findDeviceCampaignByCampaignId(deviceId, campaignId);
            if (device != null && devicesCampaign != null) {
                userService.deleteDeviceCampaign(device.getId(), campaignId);
                return true;
            }

        }
        return false;
    }
}

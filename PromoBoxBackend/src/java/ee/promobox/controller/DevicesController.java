/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ee.promobox.controller;

import ee.promobox.entity.AdCampaigns;
import ee.promobox.entity.CampaignsFiles;
import ee.promobox.entity.Devices;
import ee.promobox.entity.DevicesCampaigns;
import ee.promobox.service.Session;
import ee.promobox.service.SessionService;
import ee.promobox.service.UserService;
import ee.promobox.util.RequestUtils;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 *
 * @author Dan
 */
@Controller
public class DevicesController {
    
    private final static Logger log = LoggerFactory.getLogger(
            DevicesController.class);
    
    private final static  SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm");

    @Autowired
    private UserService userService;

    @Autowired
    private SessionService sessionService;

    @RequestMapping("/device/{uuid}/pull")
    public void showCampaign(
            @PathVariable("uuid") String uuid,
            @RequestParam String json,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        JSONObject resp = new JSONObject();
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        Devices d = userService.findDeviceByUuid(uuid);

        if (d != null) {
            JSONObject objectGiven = new JSONObject(json);

            d.setFreeSpace(objectGiven.getLong("freeSpace"));
            d.setCache(objectGiven.getLong("cache"));
            d.setCurrentFileId(objectGiven.has("currentFileId") ? objectGiven.getInt("currentFileId") : null);
            d.setCurrentCampaignId(objectGiven.has("currentCampaignId") ? objectGiven.getInt("currentCampaignId") : null);
            d.setLoadingCampaignId(objectGiven.has("loadingCampaingId") ? objectGiven.getInt("loadingCampaingId") : null);
            d.setLoadingCampaignProgress(objectGiven.has("loadingCampaingProgress") ? objectGiven.getInt("loadingCampaingProgress") : null);
            
            if (objectGiven.has("ip")) {
                d.setNetworkData(objectGiven.getJSONArray("ip").toString());
            }

            resp.put("lastUpdate", d.getLastDeviceRequestDt().getTime());
            resp.put("orientation", d.getOrientation());
            resp.put("clearCache", d.isClearCache());
            
            d.setClearCache(false);

            JSONArray campaigns = new JSONArray();
            

            DevicesCampaigns dc = userService.findLastUpdatedDeviceCampaign(d.getId());

            if (dc != null) {

                if (dc.getUpdatedDt().after(d.getLastDeviceRequestDt()) || objectGiven.has("force")) {

                    for (AdCampaigns ad: userService.findCampaignByDeviceId(d.getId())) {
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
                        
                        List<CampaignsFiles> campaignFiles = userService.findUsersCampaignFiles(dc.getAdCampaignsId(), d.getClientId());

                        if (!campaignFiles.isEmpty()) {
                            JSONArray jsonCampaignFiles = new JSONArray();

                            for (CampaignsFiles file : campaignFiles) {
                                if (file.getStatus() == CampaignsFiles.STATUS_ACTIVE) {
                                    JSONObject jsonCampaignFile = new JSONObject();

                                    jsonCampaignFile.put("id", file.getId());
                                    jsonCampaignFile.put("type", file.getFileType().intValue());
                                    jsonCampaignFile.put("size", file.getSize());

                                    jsonCampaignFiles.put(jsonCampaignFile);
                                }
                            }

                            campaign.put("files", jsonCampaignFiles);
                        }
                        
                        campaigns.put(campaign);
                    }
                    
                    resp.put("campaigns", campaigns);
                }

                d.setLastDeviceRequestDt(new Date());
                d.setStatus(Devices.STATUS_ONLINE);

                userService.updateDevice(d);

                response.setStatus(HttpServletResponse.SC_OK);

                RequestUtils.printResult(resp.toString(), response);

            }

        } else {
             resp.put("status", "error");
             resp.put("error", "not_found_device");
             
             response.setStatus(HttpServletResponse.SC_NOT_FOUND);
             
             RequestUtils.printResult(resp.toString(), response);
        }
    }

    @RequestMapping(value = "token/{token}/devices", method = RequestMethod.GET)
    public void showAllDevices(
            @PathVariable("token") String token,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        JSONObject resp = new JSONObject();
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        Session session = sessionService.findSession(token);

        if (session != null) {
            int clientId = session.getClientId();

            List<Devices> devices = userService.findUserDevieces(clientId);

            if (!devices.isEmpty()) {

                JSONArray devicesArray = new JSONArray();

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
                    jsonD.put("currentFileId", d.getCurrentFileId());
                    jsonD.put("loadingCampaingId", d.getLoadingCampaignId());
                    jsonD.put("loadingCampaingProgress", d.getLoadingCampaignProgress());
                    jsonD.put("orientation", d.getOrientation());
                    jsonD.put("resolution", d.getResolution());
                    jsonD.put("audioAut", d.getAudioOut());
                    
                    jsonD.put("workStartAt", formatTimeString(d.getWorkEndAt()));
                    jsonD.put("workEndAt", formatTimeString(d.getWorkEndAt()));
                    
                    jsonD.put("mon", d.isMon());
                    jsonD.put("tue", d.isTue());
                    jsonD.put("wed", d.isWed());
                    jsonD.put("thu", d.isThu());
                    jsonD.put("fir", d.isFri());
                    jsonD.put("sat", d.isSat());
                    jsonD.put("sun", d.isSun());
                    
                    jsonD.put("description", d.getDescription());
                    jsonD.put("lastRequestDate", d.getLastDeviceRequestDt().getTime());

                    List<AdCampaigns> acs = userService.findCampaignByDeviceId(d.getId());

                    if (acs != null) {
                        JSONArray campaignIds = new JSONArray();
                        for (AdCampaigns ac : acs) {
                            campaignIds.put(ac.getId());
                        }
                        jsonD.put("campaignId", campaignIds);
                    } else {
                        jsonD.put("campaignId", -1);
                    }

                    List<AdCampaigns> adCampaignses = userService.findUserAdCompaigns(session.getClientId());

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

                    jsonD.put("campaigns", array);

                    devicesArray.put(jsonD);
                }

                resp.put("devices", devicesArray);

                response.setStatus(HttpServletResponse.SC_OK);

                RequestUtils.printResult(resp.toString(), response);
            }
        } else {
            RequestUtils.sendUnauthorized(response);
        }

    }

    @RequestMapping(value = "token/{token}/devices/{id}", method = RequestMethod.DELETE)
    public void deleteDevice(
            @PathVariable("token") String token,
            @PathVariable("id") int id,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        JSONObject resp = new JSONObject();
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        Session session = sessionService.findSession(token);

        if (session != null) {

            Devices device = userService.findDeviceByIdAndClientId(id, session.getClientId());

            device.setStatus(Devices.STATUS_AHRCHIVED);

            userService.updateDevice(device);

            response.setStatus(HttpServletResponse.SC_OK);

            RequestUtils.printResult(resp.toString(), response);

        } else {
            RequestUtils.sendUnauthorized(response);
        }

    }

    @RequestMapping(value = "token/{token}/devices/{id}", method = RequestMethod.PUT)
    public void updateDevice(
            @PathVariable("token") String token,
            @PathVariable("id") int id,
            @RequestBody String json,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        JSONObject resp = new JSONObject();
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        Session session = sessionService.findSession(token);

        if (session != null) {
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
                
                device.setMon(deviceUpdate.getBoolean("mon"));
                device.setTue(deviceUpdate.getBoolean("tue"));
                device.setWed(deviceUpdate.getBoolean("wed"));
                device.setThu(deviceUpdate.getBoolean("thu"));
                device.setFri(deviceUpdate.getBoolean("fri"));
                device.setSat(deviceUpdate.getBoolean("sat"));
                device.setSun(deviceUpdate.getBoolean("sun"));

                int campaignId = deviceUpdate.getInt("campaignId");
                DevicesCampaigns devicesCampaigns = userService.findDeviceCampaignByCampaignId(device.getId(), campaignId);

                if (devicesCampaigns == null ) {
                    
                    AdCampaigns newAd = userService.findCampaignByCampaignId(campaignId);
                    
                    boolean timeIntersection = false;
                    for (AdCampaigns ad: userService.findCampaignByDeviceId(device.getId())) {
                        if (checkTimeIntersection(newAd, ad)) {
                            timeIntersection = true;
                            
                            break;
                        }
                    }
                    
                    if (!timeIntersection) {
                        devicesCampaigns = new DevicesCampaigns();

                        devicesCampaigns.setDeviceId(device.getId());
                        devicesCampaigns.setAdCampaignsId(campaignId);
                        devicesCampaigns.setUpdatedDt(new Date());

                        userService.addDeviceAdCampaign(devicesCampaigns);
                        
                        userService.updateDevice(device);
                    } else {
                        resp.put("ERROR", "time_intersection");
                    }
                } else {
                    devicesCampaigns.setAdCampaignsId(campaignId);
                    devicesCampaigns.setUpdatedDt(new Date());

                    userService.updateDeviceAdCampaign(devicesCampaigns);
                    
                    userService.updateDevice(device);
                }

                response.setStatus(HttpServletResponse.SC_OK);

                RequestUtils.printResult(resp.toString(), response);

            } else {
                RequestUtils.sendUnauthorized(response);
            }
        }
    }
    
    
    private boolean checkTimeIntersection(AdCampaigns campaign1, AdCampaigns campaign2) {
        try {
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
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }

        return false;
    }
    
    @RequestMapping(value = "token/{token}/devices/{id}/clearcache", method = RequestMethod.PUT)
    public void clearDeviceCache(
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

                device.setClearCache(true);
                
                userService.updateDevice(device);

                response.setStatus(HttpServletResponse.SC_OK);

                RequestUtils.printResult(resp.toString(), response);

            } else {
                RequestUtils.sendUnauthorized(response);
            }
        }
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
    public void createDevice(
            @PathVariable("token") String token,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        JSONObject resp = new JSONObject();
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        Session session = sessionService.findSession(token);

        if (session != null) {

            Devices device = new Devices();

            device.setClientId(session.getClientId());

            device.setOrientation(Devices.ORIENTATION_LANDSCAPE);
            device.setResolution(Devices.RESOLUTION_1920X1080);
            device.setFreeSpace(0);
            device.setLastDeviceRequestDt(new Date());
            device.setStatus(Devices.STATUS_USED);
            device.setAudioOut(Devices.AUDIO_OUT_HDMI);
            device.setUuid(UUID.randomUUID().toString().substring(0, 4));
            device.setDescription("");
            device.setNetworkData("");
            
            device.setWorkStartAt(parseTimeString("9:00"));
            device.setWorkEndAt(parseTimeString("20:00"));

            device.setMon(true);
            device.setTue(true);
            device.setWed(true);
            device.setThu(true);
            device.setFri(true);
            device.setSat(false);
            device.setSun(false);

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

            RequestUtils.printResult(resp.toString(), response);

        } else {
            RequestUtils.sendUnauthorized(response);
        }

    }
    
    
    @ExceptionHandler(Exception.class)
    public void handleAllException(Exception ex) {
        log.error(ex.getMessage(), ex);
    }
}

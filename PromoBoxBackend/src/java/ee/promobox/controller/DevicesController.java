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
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 *
 * @author Dan
 */
@Controller
public class DevicesController {

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

        if (d != null && d.getStatus() == 1) {
            JSONObject objectGiven = new JSONObject(json);

            d.setFreeSpace(objectGiven.getLong("freeSpace"));

            resp.put("lastUpdate", d.getLastDeviceRequestDt().getTime());
            resp.put("orientation", d.getOrientation());

            JSONObject campaign = new JSONObject();

            DevicesCampaigns dc = userService.findDeviceCampaignByDeviceId(d.getId());

            if (dc != null) {

                if (dc.getUpdatedDt().after(d.getLastDeviceRequestDt()) || objectGiven.has("force")) {

                    AdCampaigns ad = userService.findCampaignByCampaignId(dc.getAdCampaignsId());

                    campaign.put("campaignId", ad.getId());
                    campaign.put("campaignName", ad.getName());
                    campaign.put("campaignStatus", ad.getStatus());
                    campaign.put("clientId", ad.getClientId());
                    campaign.put("startDate", ad.getStart().getTime());
                    campaign.put("endDate", ad.getFinish().getTime());
                    campaign.put("sequence", ad.getSequence());
                    campaign.put("duration", ad.getDuration());
                    campaign.put("updateDate", ad.getUpdateDate().getTime());

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

                    resp.put("campaign", campaign);

                }

                d.setLastDeviceRequestDt(new Date());
                d.setStatus(Devices.STATUS_ONLINE);

                userService.updateDevice(d);

                response.setStatus(HttpServletResponse.SC_OK);

                RequestUtils.printResult(resp.toString(), response);

            }

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
                    jsonD.put("orientation", d.getOrientation());
                    jsonD.put("resolution", d.getResolution());
                    
                    AdCampaigns ac = userService.findCampaignByDeviceId(d.getId());
                    
                    if (ac!=null) {
                        jsonD.put("campaignId", ac.getId());
                    } else {
                        jsonD.put("campaignId", -1);
                    }

                    List<AdCampaigns> adCampaignses = userService.findUserAdCompaigns(session.getClientId());

                    JSONArray array = new JSONArray();

                    for (AdCampaigns a : adCampaignses) {
                        JSONObject aObj = new JSONObject();

                        aObj.put("id", a.getId());
                        aObj.put("name", a.getName());

                        if (ac != null) {
                            aObj.put("playing", a.getId() == ac.getId());
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
    
    @RequestMapping(value = "token/{token}/device/{id}", method = RequestMethod.DELETE)
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

    @RequestMapping(value = "token/{token}/device/{id}", method = RequestMethod.PUT)
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

                DevicesCampaigns devicesCampaigns = userService.findDeviceCampaignByDeviceId(device.getId());
                
                if (devicesCampaigns == null) {
                    
                    devicesCampaigns = new DevicesCampaigns();
                    
                    devicesCampaigns.setDeviceId(device.getId());
                    devicesCampaigns.setAdCampaignsId(deviceUpdate.getInt("campaignId"));
                    devicesCampaigns.setUpdatedDt(new Date());

                    userService.addDeviceAdCampaign(devicesCampaigns);
                } else {
                    devicesCampaigns.setAdCampaignsId(deviceUpdate.getInt("campaignId"));
                    devicesCampaigns.setUpdatedDt(new Date());
                    
                    userService.updateDeviceAdCampaign(devicesCampaigns);
                }

                userService.updateDevice(device);

                response.setStatus(HttpServletResponse.SC_OK);
                
                RequestUtils.printResult(resp.toString(), response);

            } else {
                RequestUtils.sendUnauthorized(response);
            }
        }

    }
}

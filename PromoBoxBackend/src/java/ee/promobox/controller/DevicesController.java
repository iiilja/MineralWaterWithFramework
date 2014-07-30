/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ee.promobox.controller;

import ee.promobox.entity.AdCampaigns;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

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
    public ModelAndView showCampaign(
            @PathVariable("uuid") String uuid,
            @RequestParam String json,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        JSONObject resp = RequestUtils.getErrorResponse();
        Devices d = userService.findDeviceByUuid(uuid);

        // if this device exists and status is active
        if (d != null && d.getStatus() == 1) {
            JSONObject objectGiven = new JSONObject(json);
            // update data about device
            d.setStatus(objectGiven.getInt("status"));
            d.setFreeSpace(objectGiven.getLong("freeSpace"));
            d.setDescription(objectGiven.getString("desc"));

            DevicesCampaigns dc = userService.findDeviceCampaignByDeviceId(d.getId());
            // if there is campaign associated with device
            if (dc != null) {
                // if campaign on device was updated after last request
                if (dc.getUpdatedDt().after(d.getLastDeviceRequestDt())) {
                    AdCampaigns ad = userService.findCampaignByCampaignId(dc.getAdCampaignsId());

                    resp.put("campaignName", ad.getName());
                    resp.put("campaignStatus", ad.getStatus());
                }
            }

            // update last request date               
            d.setLastDeviceRequestDt(new Date());
            userService.updateDevice(d);

            // if this line of code is reached, put OK in the response
            resp.put("response", RequestUtils.OK);
        }

        return RequestUtils.printResult(resp.toString(), response);
    }
    
    @RequestMapping(value = "token/{token}/devices", method=RequestMethod.GET)
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
                    jsonD.put("status", d.getStatus());
                    jsonD.put("space", d.getFreeSpace());
         
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
}

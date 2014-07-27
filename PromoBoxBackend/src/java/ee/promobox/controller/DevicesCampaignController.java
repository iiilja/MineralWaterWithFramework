/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ee.promobox.controller;

import ee.promobox.entity.AdCampaigns;
import ee.promobox.entity.Devices;
import ee.promobox.entity.DevicesCampaigns;
import ee.promobox.service.UserService;
import ee.promobox.util.RequestUtils;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 * @author Dan
 */
@Controller
public class DevicesCampaignController {

    @Autowired
    private UserService userService;

    @RequestMapping("/device/{uuid}/pull")
    public ModelAndView showCampaign(
            @PathVariable("uuid") String uuid,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        JSONObject resp = RequestUtils.getErrorResponse();
        Devices d = userService.findDeviceByUuid(uuid);

        // if this device exists and status is active
        if (d != null && d.getStatus() == 1) {
            DevicesCampaigns dc = userService.findDeviceCampaignByDeviceId(d.getId());

            // if there is campaign associated with device
            if (dc != null) {
                // if campaign on device was updated after last request
                if (dc.getUpdatedDt().after(dc.getLastDeviceRequestDt())) {
                    AdCampaigns ad = userService.findCampaignByCampaignId(dc.getAdCampaignsId());
                    
                    resp.put("uuid", d.getUuid());
                    resp.put("device_status", d.getStatus());
                    resp.put("device_desc", d.getDescription());

                    resp.put("campaign_name", ad.getName());
                    resp.put("campaign_status", ad.getStatus());

                    // update last request date               
                    dc.setLastDeviceRequestDt(new Date());
                    userService.updateDeviceAdCampaign(dc);

                    // if this line of code is reached, put OK in the response
                    resp.put("response", RequestUtils.OK);
                }
            }
        }

        return RequestUtils.printResult(resp.toString(), response);
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ee.promobox.controller;

import ee.promobox.entity.AdCampaigns;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 * @author Dan
 */
@Controller
public class CampaignsController {

    @Autowired
    private SessionService sessionService;

    @Autowired
    private UserService userService;

    @RequestMapping("campaigns/all")
    public ModelAndView showAllCampaigns(
            @RequestParam String token,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        JSONObject resp = RequestUtils.getErrorResponse();
        Session session = sessionService.findSession(token);

        if (session != null) {
            int clientId = session.getClientId();
            List<AdCampaigns> campaigns = userService.findUserAdCompaigns(clientId);

            if (!campaigns.isEmpty()) {
                // array for holding campaigns
                JSONArray campaignsArray = new JSONArray();
                // iterate trough the list of campaigns that belong to the client
                for (AdCampaigns campaign : campaigns) {
                    JSONObject jsonCampaign = new JSONObject();
                    jsonCampaign.put("id", campaign.getId());
                    jsonCampaign.put("name", campaign.getName());
                    jsonCampaign.put("active", campaign.getActive());

                    campaignsArray.put(jsonCampaign);
                }

                // put array of campaigns into response
                resp.put("campaigns", campaignsArray);
                // everything's fine, put ok in the response
                resp.put("response", RequestUtils.OK);
            }
        }

        return RequestUtils.printResult(resp.toString(), response);
    }

    @RequestMapping("campaigns/add")
    public ModelAndView addCampaign(
            @RequestParam String token,
            @RequestParam String name,
            @RequestParam boolean active,
            @RequestParam int sequence,
            @RequestParam long start,
            @RequestParam long finish,
            @RequestParam int duration,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        JSONObject resp = RequestUtils.getErrorResponse();
        Session session = sessionService.findSession(token);

        if (session != null) {
            AdCampaigns campaign = new AdCampaigns();
            campaign.setName(name);
            campaign.setClientId(session.getClientId());
            campaign.setActive(active);
            campaign.setStart(new Date(start));
            campaign.setFinish(new Date(finish));
            campaign.setSequence(sequence);
            campaign.setDuration(duration);

            userService.addCampaign(campaign);
            
            List<AdCampaigns> campaigns = userService.findUserAdCompaigns(session.getClientId());
            
            resp.put("response", RequestUtils.OK);
            resp.put("id", campaigns.get(campaigns.size() - 1).getId());
        }

        return RequestUtils.printResult(resp.toString(), response);
    }

    @RequestMapping("campaigns/update")
    public ModelAndView updateCampaign(
            @RequestParam String token,
            @RequestParam int id,
            @RequestParam String name,
            @RequestParam boolean active,
            @RequestParam int sequence,
            @RequestParam long start,
            @RequestParam long finish,
            @RequestParam int duration,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        JSONObject resp = RequestUtils.getErrorResponse();
        Session session = sessionService.findSession(token);

        // if session exists
        if (session != null) {
            int clientId = session.getClientId();
            
            if (userService.hasAccess(id, clientId)) {
                // create new adcampaigns object
                AdCampaigns campaign = new AdCampaigns();

                // fill all the fields with data provided by the client
                campaign.setId(id);
                campaign.setName(name);
                campaign.setClientId(clientId);
                campaign.setActive(active);
                campaign.setSequence(sequence);
                campaign.setStart(new Date(start));
                campaign.setFinish(new Date(finish));
                campaign.setDuration(duration);
                userService.updateCampaign(campaign);

                // if this line of code is reached, put OK in the response
                resp.put("response", RequestUtils.OK);
            }
        }

        return RequestUtils.printResult(resp.toString(), response);
    }

}

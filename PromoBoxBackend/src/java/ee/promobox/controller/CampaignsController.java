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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 * @author Dan
 */
@Controller
public class CampaignsController {
    
    private final static Logger log = LoggerFactory.getLogger(
            CampaignsController.class);

    @Autowired
    private SessionService sessionService;

    @Autowired
    private UserService userService;
    
    
    @RequestMapping("token/{token}/campaign/{campaignId}")
    public ModelAndView showCampaign(
            @PathVariable("token") String token,
            @PathVariable("campaignId") int campaignId,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        JSONObject resp = RequestUtils.getErrorResponse();
        Session session = sessionService.findSession(token);

        if (session != null) {
            int clientId = session.getClientId();
            AdCampaigns campaign = userService.findCampaignByIdAndClientId(campaignId, clientId);

            if (campaign != null) {

                resp.put("id", campaign.getId());
                resp.put("name", campaign.getName());
                resp.put("status", campaign.getStatus());
                resp.put("clientId", campaign.getClientId());
                resp.put("duration", campaign.getDuration());
                resp.put("finish", campaign.getFinish() == null ? null : campaign.getFinish().getTime());
                resp.put("sequence", campaign.getSequence());
                resp.put("start", campaign.getStart() == null ? null : campaign.getStart().getTime());

            }

            // everything's fine, put ok in the response
            resp.put("response", RequestUtils.OK);
        }
        

        return RequestUtils.printResult(resp.toString(), response);
    }

    @RequestMapping("token/{token}/campaigns")
    public ModelAndView showAllCampaigns(
            @PathVariable("token") String token,
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
                    jsonCampaign.put("status", campaign.getStatus());

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

    @RequestMapping("campaigns/create")
    public ModelAndView createCampaign(
            @RequestParam String token,
            @RequestParam String name,
            @RequestParam String json,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        JSONObject resp = RequestUtils.getErrorResponse();
        Session session = sessionService.findSession(token);

        if (session != null) {

            JSONObject objectGiven = new JSONObject(json);

            AdCampaigns campaign = new AdCampaigns();
            campaign.setName(name);
            campaign.setClientId(session.getClientId());
            campaign.setStatus(objectGiven.getInt("status"));
            campaign.setSequence(objectGiven.getInt("sequence"));
            campaign.setStart(new Date(objectGiven.getLong("start")));
            campaign.setFinish(new Date(objectGiven.getLong("finish")));
            campaign.setDuration(objectGiven.getInt("duration"));

            userService.addCampaign(campaign);
            
            resp.put("response", RequestUtils.OK);
            resp.put("id", campaign.getId());
        }

        return RequestUtils.printResult(resp.toString(), response);
    }

    @RequestMapping("campaigns/update/{id}")
    public ModelAndView updateCampaign(
            @RequestParam String token,
            @PathVariable("id") int id,
            @RequestParam String name,
            @RequestParam String json,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        JSONObject resp = RequestUtils.getErrorResponse();
        Session session = sessionService.findSession(token);

        // if session exists
        if (session != null) {
            int clientId = session.getClientId();

            AdCampaigns campaign = userService.findCampaignByIdAndClientId(id, clientId);

            if (campaign != null) {

                JSONObject objectGiven = new JSONObject(json);

                // fill all the fields with data provided by the client
                campaign.setName(name);
                campaign.setClientId(clientId);
                campaign.setStatus(objectGiven.getInt("status"));
                campaign.setSequence(objectGiven.getInt("sequence"));
                campaign.setStart(new Date(objectGiven.getLong("start")));
                campaign.setFinish(new Date(objectGiven.getLong("finish")));
                campaign.setDuration(objectGiven.getInt("duration"));
                userService.updateCampaign(campaign);

                // if this line of code is reached, put OK in the response
                resp.put("response", RequestUtils.OK);
            }
        }

        return RequestUtils.printResult(resp.toString(), response);
    }

}

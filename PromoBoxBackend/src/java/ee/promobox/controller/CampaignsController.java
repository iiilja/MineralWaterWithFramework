/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ee.promobox.controller;

import ee.promobox.entity.AdCampaigns;
import ee.promobox.entity.Files;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

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

    @RequestMapping(value = "token/{token}/campaigns/{campaignId}", method = RequestMethod.GET)
    public void showCampaign(
            @PathVariable("token") String token,
            @PathVariable("campaignId") int campaignId,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        JSONObject resp = new JSONObject();
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        // find session
        Session session = sessionService.findSession(token);

        // if session is not empty
        if (session != null) {
            int clientId = session.getClientId();
            AdCampaigns campaign = userService.findCampaignByIdAndClientId(campaignId, clientId);

            // if campaign isnt empty
            if (campaign != null) {

                resp.put("id", campaign.getId());
                resp.put("name", campaign.getName());
                resp.put("status", campaign.getStatus());
                resp.put("clientId", campaign.getClientId());
                resp.put("duration", campaign.getDuration());
                resp.put("finish", campaign.getFinish() == null ? null : campaign.getFinish().getTime());
                resp.put("sequence", campaign.getSequence());
                resp.put("start", campaign.getStart() == null ? null : campaign.getStart().getTime());

                // put information about files associated with campaign
                List<Files> campaignFiles = userService.findUsersCampaignFiles(campaignId, clientId);
                resp.put("files", FilesController.getFilesInformation(campaignFiles));

                response.setStatus(HttpServletResponse.SC_OK);
                RequestUtils.printResult(resp.toString(), response);
            }

        } else {
            RequestUtils.sendUnauthorized(response);
        }

    }

    @RequestMapping(value = "token/{token}/campaigns", method = RequestMethod.GET)
    public void showAllCampaigns(
            @PathVariable("token") String token,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        JSONObject resp = new JSONObject();
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

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

                response.setStatus(HttpServletResponse.SC_OK);
                RequestUtils.printResult(resp.toString(), response);
            }
        } else {
            RequestUtils.sendUnauthorized(response);
        }

    }

    @RequestMapping(value = "token/{token}/campaigns", method = RequestMethod.POST)
    public void createCampaign(
            @PathVariable("token") String token,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        JSONObject resp = new JSONObject();
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        Session session = sessionService.findSession(token);

        if (session != null) {

            AdCampaigns campaign = new AdCampaigns();

            campaign.setName("New campaign");
            campaign.setClientId(session.getClientId());
            campaign.setStatus(AdCampaigns.STATUS_CREATED);
            campaign.setSequence(1);
            campaign.setStart(new Date());
            campaign.setFinish(new Date());
            campaign.setDuration(1);
            campaign.setUpdateDate(new Date());

            userService.addCampaign(campaign);

            response.setStatus(HttpServletResponse.SC_OK);
            resp.put("id", campaign.getId());

            RequestUtils.printResult(resp.toString(), response);

        } else {
            RequestUtils.sendUnauthorized(response);
        }

    }

    @RequestMapping(value = "token/{token}/campaigns/{id}", method = RequestMethod.DELETE)
    public void deleteCampaign(
            @PathVariable("token") String token,
            @PathVariable("id") int id,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        JSONObject resp = new JSONObject();
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        Session session = sessionService.findSession(token);

        if (session != null) {

            AdCampaigns camp = userService.findCampaignByIdAndClientId(id, session.getClientId());
            camp.setStatus(AdCampaigns.STATUS_AHRCHIVED);

            userService.updateCampaign(camp);

            response.setStatus(HttpServletResponse.SC_OK);
            RequestUtils.printResult(resp.toString(), response);

        } else {
            RequestUtils.sendUnauthorized(response);
        }

    }

    @RequestMapping(value = "token/{token}/campaigns/{id}", method = RequestMethod.PUT)
    public void updateCampaign(
            @PathVariable("token") String token,
            @PathVariable("id") int id,
            @RequestBody String json,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        JSONObject resp = new JSONObject();
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        Session session = sessionService.findSession(token);

        // if session exists
        if (session != null) {

            int clientId = session.getClientId();
            AdCampaigns campaign = userService.findCampaignByIdAndClientId(id, clientId);

            if (campaign != null) {

                JSONObject objectGiven = new JSONObject(json);

                // fill all the fields with data provided by the client
                campaign.setName(objectGiven.getString("name"));
                campaign.setClientId(clientId);
                campaign.setStatus(AdCampaigns.STATUS_PREPARED);
                campaign.setSequence(objectGiven.getInt("sequence"));
                campaign.setStart(new Date(objectGiven.getLong("start")));
                campaign.setFinish(new Date(objectGiven.getLong("finish")));
                campaign.setDuration(objectGiven.getInt("duration"));
                campaign.setUpdateDate(new Date());

                userService.updateCampaign(campaign);

                response.setStatus(HttpServletResponse.SC_OK);
                RequestUtils.printResult(resp.toString(), response);
            }
        } else {
            RequestUtils.sendUnauthorized(response);
        }

    }

}

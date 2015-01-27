/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ee.promobox.controller;

import ee.promobox.entity.AdCampaigns;
import ee.promobox.entity.CampaignsFiles;
import ee.promobox.entity.Devices;
import ee.promobox.service.Session;
import ee.promobox.service.SessionService;
import ee.promobox.service.UserService;
import ee.promobox.util.RequestUtils;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
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
                resp.put("countFiles", campaign.getCountFiles());
                resp.put("countImages", campaign.getCountImages());
                resp.put("countAudios", campaign.getCountAudios());
                resp.put("countVideos", campaign.getCountVideos());
                resp.put("audioLength", campaign.getAudioLength());
                resp.put("videoLength", campaign.getVideoLength());

                try {
                    JSONObject workTimeData = new JSONObject(campaign.getWorkTimeData());

                    resp.put("days", workTimeData.getJSONArray("days"));
                    resp.put("hours", workTimeData.getJSONArray("hours"));

                } catch (Exception ex) {
                    log.error(ex.getMessage(), ex);
                }

                // put information about files associated with campaign
                List<CampaignsFiles> campaignFiles = userService.findUsersCampaignFiles(campaignId, clientId);
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
                	
                	Date now = new Date();
                	if (campaign.getStart().before(now) && 
                			campaign.getFinish().after(now)) {
                		campaign.setStatus(AdCampaigns.STATUS_PUBLISHED);
                	} else {
                		campaign.setStatus(AdCampaigns.STATUS_UNPUBLISHED);
                	}
                	
                    JSONObject jsonCampaign = new JSONObject();

                    jsonCampaign.put("id", campaign.getId());
                    jsonCampaign.put("name", campaign.getName());
                    jsonCampaign.put("status", campaign.getStatus());
                    jsonCampaign.put("finish", campaign.getFinish() == null ? null : campaign.getFinish().getTime());
                    jsonCampaign.put("start", campaign.getStart() == null ? null : campaign.getStart().getTime());
                    jsonCampaign.put("countFiles", campaign.getCountFiles());
                    jsonCampaign.put("countImages", campaign.getCountImages());
                    jsonCampaign.put("countAudios", campaign.getCountAudios());
                    jsonCampaign.put("countVideos", campaign.getCountVideos());
                    jsonCampaign.put("audioLength", campaign.getAudioLength());
                    jsonCampaign.put("videoLength", campaign.getVideoLength());

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
            
            Date today = new Date();
            
            Calendar start = GregorianCalendar.getInstance();
            start.setTime(today);
            start.set(Calendar.HOUR_OF_DAY, 0);
            start.set(Calendar.MINUTE, 0);
            start.set(Calendar.SECOND, 0);
            start.set(Calendar.MILLISECOND, 0);
            
            Calendar finish = GregorianCalendar.getInstance();
            finish.setTime(today);
            finish.set(Calendar.YEAR, 2099);
            finish.set(Calendar.HOUR_OF_DAY, 23);
            finish.set(Calendar.MINUTE, 30);
            finish.set(Calendar.SECOND, 0);
            finish.set(Calendar.MILLISECOND, 0);
            
            Date createdDate = new Date();

            campaign.setName("New campaign");
            campaign.setClientId(session.getClientId());
            campaign.setStatus(AdCampaigns.STATUS_PUBLISHED);
            campaign.setSequence(1);
            campaign.setStart(start.getTime());
            campaign.setFinish(finish.getTime());
            campaign.setDuration(30);
            campaign.setCreatedDate(createdDate);
            campaign.setUpdateDate(createdDate);

            JSONObject workTimeJson = new JSONObject();

            JSONArray days = new JSONArray();

            days.put("mo").put("tu").put("we").put("th").put("fr").put("sa").put("su");

            JSONArray hours = new JSONArray();

            for (int i = 0; i < 24; i++) {
                hours.put("" + i);
            }

            workTimeJson.put("days", days);
            workTimeJson.put("hours", hours);

            campaign.setWorkTimeData(workTimeJson.toString());

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
            
            List<CampaignsFiles> files = userService.findCampaignFiles(camp.getId());
            
            for (CampaignsFiles f: files) {
                
                f.setStatus(CampaignsFiles.STATUS_ARCHIVED);
                f.setUpdatedDt(new Date());
                
                userService.updateCampaignFile(f);

            }

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
                campaign.setStatus(objectGiven.getInt("status"));
                campaign.setSequence(objectGiven.getInt("sequence"));
                campaign.setStart(new Date(objectGiven.getLong("start")));
                campaign.setFinish(new Date(objectGiven.getLong("finish")));
                campaign.setDuration(objectGiven.getInt("duration"));

                JSONObject workTimeData = new JSONObject();

                if (objectGiven.has("days") && objectGiven.has("hours")) {

                    workTimeData.put("days", objectGiven.getJSONArray("days"));
                    workTimeData.put("hours", objectGiven.getJSONArray("hours"));

                    campaign.setWorkTimeData(workTimeData.toString());
                }
                
                // Check time intersection
                boolean timeIntersection = false;
                for (Devices d: userService.findDevicesByCampaing(id)) {
                	for (AdCampaigns c: userService.findCampaignByDeviceId(d.getId())) {
                		if (c.getId() == (int) campaign.getId() || c.getStatus() != AdCampaigns.STATUS_PUBLISHED) {
                			continue;
                		}
                		
                		timeIntersection = DevicesController.checkTimeIntersection(campaign, c);
                		
                		if (timeIntersection) break;
                	}
                }

                if (!timeIntersection) {
	                campaign.setUpdateDate(new Date());
	
	                userService.updateCampaign(campaign);
                } else {
                	resp.put("ERROR", "time_intersection");
                }

                response.setStatus(HttpServletResponse.SC_OK);
                RequestUtils.printResult(resp.toString(), response);
            }
        } else {
            RequestUtils.sendUnauthorized(response);
        }

    }
    
    @RequestMapping(value = "token/{token}/campaigns/{id}/nextFile/{file}", method = RequestMethod.PUT)
    public void clearDeviceCache(
            @PathVariable("token") String token,
            @PathVariable("id") int id,
            @PathVariable("file") int fileId,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        JSONObject resp = new JSONObject();
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        Session session = sessionService.findSession(token);

        if (session != null) {
            int clientId = session.getClientId();
            

            List<Devices> devices = userService.findDevicesByCampaignId(id, clientId);

            if (!devices.isEmpty()) {
                for (Devices device: devices) {
                    device.setNextFile(fileId);

                    userService.updateDevice(device);
                }
            } else {
                resp.put("error", "no_device");
            }
            
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

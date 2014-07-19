/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ee.promobox.controller;

import ee.promobox.service.Session;
import ee.promobox.service.SessionService;
import ee.promobox.util.RequestUitls;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
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
    
    @RequestMapping("campaign/save")
    public ModelAndView saveCampaign(
            @RequestParam String token,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        JSONObject resp = RequestUitls.getErrorResponse();
        
        Session session = sessionService.findSession(token);
        
        if (session != null) {
            resp.put("response", RequestUitls.OK);
        } 
        
        return RequestUitls.printResult(resp.toString(), response);
    }
}

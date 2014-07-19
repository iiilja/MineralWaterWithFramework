/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ee.promobox.controller;

import ee.promobox.entity.AdCampaigns;
import ee.promobox.entity.Devices;
import ee.promobox.entity.Users;
import ee.promobox.service.Session;
import ee.promobox.service.SessionService;
import ee.promobox.service.UserService;
import ee.promobox.util.RequestUtils;
import java.io.Writer;
import java.util.Date;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 * @author Maxim
 */
@Controller
public class UserController {

    private final static Logger log = LoggerFactory.getLogger(
            UserController.class);

    @Autowired
    private UserService userService;
    
    @Autowired
    private SessionService sessionService;

    @RequestMapping("/index")
    public ModelAndView indexHandler(
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        JSONObject json = new JSONObject();
        json.put("response", "OK");
        JSONArray userAr = new JSONArray();

        List<Users> list = userService.findAllUsers();

        for (Users u : list) {

            JSONObject jsonUser = new JSONObject();
            jsonUser.put("firstname", u.getFirstname());
            jsonUser.put("surname", u.getSurname());

            userAr.put(jsonUser);

        }

        json.put("users", userAr);

        return RequestUtils.printResult(json.toString(), response);

    }
    
    
    @RequestMapping("/user/login")
    public ModelAndView userLoginHandler(
            @RequestParam String email,
            @RequestParam String password,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        JSONObject resp = RequestUtils.getErrorResponse();
        
        Users user = userService.findUserByEmailAndPassword(email, password);
        
        if (user!=null) {
            resp.put("response", RequestUtils.OK);
            
            Session session = new  Session();
            
            session.setIp(request.getRemoteAddr());
            session.setUserId(user.getId());
            session.setClientId(user.getClientId());
            session.setCreatedDate(new Date());
            session.setUuid(UUID.randomUUID().toString().replaceAll("-", ""));
            
            sessionService.addSession(session);
            
            resp.put("token", session.getUuid());
        }

        return RequestUtils.printResult(resp.toString(), response);

    }
    
    
    @RequestMapping("/user/data")
    public ModelAndView userDataHandler(
            @RequestParam String token,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        JSONObject resp = RequestUtils.getErrorResponse();
        
        Session session = sessionService.findSession(token);
        
        if (session!=null) {
            resp.put("response", RequestUtils.OK);
            
            List<AdCampaigns> campaigns = userService.findUserAdCompaigns(session.getClientId());
            List<Devices> devices = userService.findUserDevieces(session.getClientId());
            
            JSONArray arCamp = new JSONArray();
            
            for (AdCampaigns a: campaigns) {
                JSONObject obj = new JSONObject();
                
                obj.put("id", a.getId());
                obj.put("name", a.getName());
                
                arCamp.put(obj);
            }
                                    
            resp.put("campaigns", arCamp);
            
            JSONArray devs = new JSONArray();
            
            for (Devices d: devices) {
                JSONObject obj = new JSONObject();
                
                obj.put("id", d.getId());
                obj.put("status", d.getStatus());
                obj.put("uuid", d.getUuid());
                
                devs.put(obj);
            }
            
            resp.put("devices", devs);

        }

        return RequestUtils.printResult(resp.toString(), response);
    }


}

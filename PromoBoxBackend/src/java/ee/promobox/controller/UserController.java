/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ee.promobox.controller;

import ee.promobox.entity.AdCampaigns;
import ee.promobox.entity.Clients;
import ee.promobox.entity.Devices;
import ee.promobox.entity.Users;
import ee.promobox.service.Session;
import ee.promobox.service.SessionService;
import ee.promobox.service.UserService;
import ee.promobox.util.RequestUtils;

import java.net.URLDecoder;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
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
    

    @RequestMapping(value="/user/register", method=RequestMethod.POST)
    public ModelAndView userRegisterHandler(
    		@RequestBody String json,
    		HttpServletRequest request,
    		HttpServletResponse responce) throws Exception {
    	
    	json = URLDecoder.decode(json, "UTF-8");
    	
    	JSONObject objectGiven = new JSONObject(json);
    	JSONObject resp = RequestUtils.getErrorResponse();
    	resp.put("response", RequestUtils.ERROR);
    	
    	String email = objectGiven.getString("email");
    	if (!EmailValidator.getInstance().isValid(email)) { 
    		resp.put("reason", "invalidEmail");
    		
    	} else if (userService.findUserByEmail(email) != null) {
    		resp.put("reason", "emailExist");
    	} else {
    		Clients client = new Clients();
    		client.setCompanyName(objectGiven.getString("companyName"));
    		userService.addClient(client);
    		
    		Users user = new Users();
    		user.setFirstname(objectGiven.getString("firstname"));
    		user.setSurname(objectGiven.getString("surname"));
    		user.setEmail(email);
    		user.setPassword(genereateRandomPass());
    		user.setClientId(client.getId());
    		user.setCreatedDt(new Date());
    		user.setActive(false);
    		user.setAdmin(true);
    		userService.addUser(user);
    		
    		resp.put("response", RequestUtils.OK);
    	}

    	return RequestUtils.printResult(resp.toString(), responce);
    }
    
    @RequestMapping(value = "token/{token}/users", method = RequestMethod.GET)
    public @ResponseBody String listUsers(
            @PathVariable("token") String token,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {
    	
    	JSONObject resp = new JSONObject();
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        Session session = sessionService.findSession(token);

        if (session != null) {
        	if (session.isAdmin()) {
        		
        		JSONArray usersArray = new JSONArray();
        		for (Users u: userService.findUsersByClientId(session.getClientId())) {
        			JSONObject userJson = new JSONObject();
        			userJson.put("id", u.getId());
        			userJson.put("firstname", u.getFirstname());
        			userJson.put("surname", u.getSurname());
        			userJson.put("email", u.getEmail());
        			
        			usersArray.put(userJson);
        		}
        		
        		resp.put("users", usersArray);
        		resp.put("response", RequestUtils.OK);
        	}
        }
        
        return resp.toString();
    }
    
    @RequestMapping(value = "token/{token}/users", method = RequestMethod.POST)
    public void createUser(
            @PathVariable("token") String token,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {
    	
    	JSONObject resp = new JSONObject();
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        Session session = sessionService.findSession(token);

        if (session != null) {
        	if (session.isAdmin()) {
        		int clientId = session.getClientId();
        		
        		Users user = new Users();
        		user.setFirstname("");
        		user.setSurname("");
        		user.setEmail("");
        		user.setPassword(genereateRandomPass());
        		user.setClientId(clientId);
        		user.setCreatedDt(new Date());
        		user.setActive(true);
        		user.setAdmin(true);
        		userService.addUser(user);
        		
        		resp.put("response", RequestUtils.OK);
        	}
        	
        } else {
            RequestUtils.sendUnauthorized(response);
        }
    }
    
    @RequestMapping(value = "token/{token}/users/{id}", method = RequestMethod.PUT)
    public void updateUser(
    		@PathVariable("id") int userId,
            @PathVariable("token") String token,
            @RequestBody String json,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {
    	
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        JSONObject resp = RequestUtils.getErrorResponse();
        Session session = sessionService.findSession(token);

        if (session != null) {
        	if (session.isAdmin()) {
        		
        		JSONObject objectGiven = new JSONObject(json);
            	
            	resp.put("response", RequestUtils.ERROR);
            	
            	String email = objectGiven.getString("email");
            	if (!EmailValidator.getInstance().isValid(email)) { 
            		resp.put("reason", "invalidEmail");
            		
            	} else if (userService.findUserByEmail(email) != null) {
            		resp.put("reason", "emailExist");
            	} else {
	        		int clientId = session.getClientId();
	        		
	        		Users user = userService.findUserById(userId);
	        		user.setFirstname(objectGiven.getString("firstname"));
	        		user.setSurname(objectGiven.getString("surname"));
	        		user.setEmail(email);
	        		
	        		user.setClientId(clientId);
	        		//user.setUsername(username);
	        		user.setActive(true);
	        		user.setAdmin(false);
	        		
	        		if (objectGiven.has("password")) {
	        			String password = objectGiven.getString("password");
	        			if (StringUtils.trimToNull(password) != null) {
	        				user.setPassword(password);
	        			}
	        		}
	        		
	        		userService.updateUser(user);
	        		
	        		resp.put("response", RequestUtils.OK);
            	}
        	}
        	
        } else {
            RequestUtils.sendUnauthorized(response);
        }
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
            session.setAdmin(user.getAdmin());
            session.setClientId(user.getClientId());
            session.setCreatedDate(new Date());
            session.setUuid(UUID.randomUUID().toString().replaceAll("-", ""));
            
            sessionService.addSession(session);
            
            resp.put("token", session.getUuid());
        }

        return RequestUtils.printResult(resp.toString(), response);

    }
    
    
    @RequestMapping("/user/data/{token}")
    public void userDataHandler(
            @PathVariable("token") String token,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        JSONObject resp = new JSONObject();
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        
        Session session = sessionService.findSession(token);
        
        if (session!=null) {
            resp.put("response", RequestUtils.OK);
            
            Clients client = userService.findClientById(session.getClientId());
            if (client != null) {
                resp.put("compName", client.getCompanyName());
            }
            
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
            
            response.setStatus(HttpServletResponse.SC_OK);
            RequestUtils.printResult(resp.toString(), response);

        } else {
            RequestUtils.sendUnauthorized(response);
        }

        
    }
    
    private static String genereateRandomPass() {
    	return RandomStringUtils.random(8, true, true);
    }


}

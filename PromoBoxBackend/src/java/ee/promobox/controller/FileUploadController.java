/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ee.promobox.controller;

/**
 *
 * @author Dan
 */
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

import ee.promobox.entity.FileUpload;

@Controller
public class FileUploadController {

    @Autowired
    private SessionService sessionService;

    @Autowired
    private UserService userService;
    
    @RequestMapping("files/upload")
    public ModelAndView uploadFile(
            @RequestParam String token,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        return null;
    }
}

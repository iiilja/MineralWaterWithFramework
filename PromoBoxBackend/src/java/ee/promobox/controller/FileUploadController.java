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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;


@Controller
public class FileUploadController {

    @Autowired
    private SessionService sessionService;

    @Autowired
    private UserService userService;
    
    private final static Logger log = LoggerFactory.getLogger(
            FileUploadController.class);
    
    @RequestMapping("files/upload")
    public ModelAndView uploadFile(
            /*
            @RequestParam String token,*/
            @ModelAttribute FileUploadCommand command,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        
        //TODO: Add check for token and session
        
        MultipartFile multipartFile = command.getFile();
        
        log.info("Filename: " + multipartFile.getOriginalFilename());
        log.info("Filesize: " + multipartFile.getSize());
        
        JSONObject resp = RequestUtils.getErrorResponse();
        resp.put("response", "OK");
        
        return RequestUtils.printResult(resp.toString(), response);
    }
}

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
import ee.promobox.entity.Files;
import ee.promobox.service.Session;
import ee.promobox.service.SessionService;
import ee.promobox.service.UserService;
import ee.promobox.util.FileUtils;
import ee.promobox.util.RequestUtils;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
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

    private final static String FILE_DIRECTORY = "C:\\Users\\Dan\\Desktop\\KioskFiles\\";
    private final static String TEMP = FILE_DIRECTORY + "TEMP\\";

    @RequestMapping("files/upload")
    public ModelAndView uploadFile(
            @RequestParam String token,
            @ModelAttribute FileUploadCommand command,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        JSONObject resp = RequestUtils.getErrorResponse();
        Session session = sessionService.findSession(token);

        if (session != null) {
            MultipartFile multipartFile = command.getFile();
            
            if (!multipartFile.isEmpty()) {
                // output data about the file into console
                log.info("Filename: " + multipartFile.getOriginalFilename());
                log.info("Filesize: " + multipartFile.getSize());
                
                // define path for users directory
                String userFilePath =  FILE_DIRECTORY + session.getClientId() + "\\";
                
                // if users folder doesnt exist, create one
                File userFolder = new File(userFilePath);
                
                if (!userFolder.exists()) {
                    userFolder.mkdir();
                }
                
                // get data about the file
                String fileName = multipartFile.getOriginalFilename();
                String fileType = fileName.split("\\.")[1];
                long fileSize = multipartFile.getSize();
                
                // upload file to the temp folder on server
                File physicalFile = new File(TEMP + fileName);

                byte[] bytes = multipartFile.getBytes();
                BufferedOutputStream stream = new BufferedOutputStream(
                        new FileOutputStream(physicalFile));
                stream.write(bytes);
                stream.close();   
                
                // populate database with data about file
                Files databaseFile = new Files();
                databaseFile.setFileType(FileUtils.determineFileTypeNumber(fileType));
                databaseFile.setPath(userFilePath);
                databaseFile.setCreatedDt(new Date(System.currentTimeMillis()));
                databaseFile.setSize((int)fileSize);
                userService.addFile(databaseFile); 
                
                databaseFile.setFilename(databaseFile.getId() + "." + fileType);

                
                // move file to the users folder and rename it to its DB id
                physicalFile.renameTo(new File(userFilePath + databaseFile.getId() + "." + fileType));

                resp.put("response", "OK");
            }
        }

        return RequestUtils.printResult(resp.toString(), response);
    }
}

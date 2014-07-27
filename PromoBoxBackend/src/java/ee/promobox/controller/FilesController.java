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
import ee.promobox.KioskConfig;
import ee.promobox.entity.AdCampaigns;
import ee.promobox.entity.CampaignsFiles;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class FilesController {

    @Autowired
    private SessionService sessionService;

    @Autowired
    private UserService userService;

    @Autowired
    private KioskConfig config;

    private final static Logger log = LoggerFactory.getLogger(
            FilesController.class);

    @RequestMapping(value = "token/{token}/files/{id}", method = RequestMethod.GET)
    public ModelAndView showCampaignFiles(
            @PathVariable("token") String token,
            @PathVariable("id") int campaignId,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        JSONObject resp = RequestUtils.getErrorResponse();
        Session session = sessionService.findSession(token);

        if (session != null) {
            List<Files> campaignFiles = userService.findUsersCampaignFiles(campaignId, session.getClientId());

            if (!campaignFiles.isEmpty()) {
                JSONArray jsonCampaignFiles = new JSONArray();
                for (Files file : campaignFiles) {
                    JSONObject jsonCampaignFile = new JSONObject();
                    jsonCampaignFile.put("id", file.getId());
                    jsonCampaignFile.put("name", file.getFilename());
                    jsonCampaignFile.put("created", file.getCreatedDt());

                    jsonCampaignFiles.put(jsonCampaignFile);
                }

                resp.put("campaignfiles", jsonCampaignFiles);
                resp.put("response", RequestUtils.OK);
            }
        }

        return RequestUtils.printResult(resp.toString(), response);
    }

    @RequestMapping(value = "token/{token}/files/{id}", method = RequestMethod.POST)
    public ModelAndView uploadFile(
            @PathVariable("token") String token,
            @PathVariable("id") int campaignId,
            @ModelAttribute FileUploadCommand command,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        JSONObject resp = RequestUtils.getErrorResponse();
        Session session = sessionService.findSession(token);

        if (session != null) {
            AdCampaigns campaign = userService.findCampaignByIdAndClientId(campaignId, session.getClientId());

            // if this campaign belongs to user
            if (campaign != null) {
                MultipartFile multipartFile = command.getFile();

                if (!multipartFile.isEmpty()) {
                    // output data about the file into console
                    log.info("Filename: " + multipartFile.getOriginalFilename());
                    log.info("Filesize: " + multipartFile.getSize());

                    // define path for users directory
                    String temporaryFolder = config.getDataDir() + "TEMP\\";
                    String userFilePath = config.getDataDir() + session.getClientId() + File.separator;
                    // if users folder doesnt exist, create one
                    File userFolder = new File(userFilePath);

                    if (!userFolder.exists()) {
                        userFolder.mkdir();
                    }

                    // get data about the file
                    String fileName = multipartFile.getOriginalFilename();
                    String fileType = fileName.split("\\.")[1];
                    int fileTypeNumber = FileUtils.determineFileTypeNumber(fileType);
                    // divide by 1024 so that we get result in kilobytes
                    long fileSize = multipartFile.getSize();

                    // upload file to the temp folder on server
                    File physicalFile = new File(temporaryFolder + fileName);

                    byte[] bytes = multipartFile.getBytes();

                    try (BufferedOutputStream stream = new BufferedOutputStream(
                            new FileOutputStream(physicalFile))) {
                        stream.write(bytes);
                    } catch (Exception ex) {
                        return RequestUtils.printResult(resp.toString(), response);
                    }

                    // populate file table with data about file
                    Files databaseFile = new Files();
                    databaseFile.setFilename(fileName);
                    databaseFile.setFileType(fileTypeNumber);
                    databaseFile.setPath(userFilePath);
                    databaseFile.setCreatedDt(new Date(System.currentTimeMillis()));
                    databaseFile.setSize(fileSize);
                    userService.addFile(databaseFile);

                    // populate campaign files table with data about file
                    CampaignsFiles campaignFile = new CampaignsFiles();
                    campaignFile.setClientId(session.getClientId());
                    campaignFile.setAdCampaignsId(campaignId);
                    campaignFile.setFileId(databaseFile.getId());
                    campaignFile.setFileType(fileTypeNumber);
                    campaignFile.setOrderId(null);
                    campaignFile.setStatus(CampaignsFiles.STATUS_ACTIVE);
                    userService.addCampaignFile(campaignFile);

                    // move file to the users folder and rename it to its DB id
                    physicalFile.renameTo(new File(userFilePath + databaseFile.getId() + "." + fileType));

                    resp.put("response", "OK");
                }
            }
        }

        return RequestUtils.printResult(resp.toString(), response);
    }

    @RequestMapping(value = "token/{token}/files/archive/{id}", method = RequestMethod.PUT)
    public ModelAndView archiveCampaignFiles(
            @PathVariable("token") String token,
            @PathVariable("id") int fileId,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        JSONObject resp = RequestUtils.getErrorResponse();
        Session session = sessionService.findSession(token);

        if (session != null) {
            CampaignsFiles campaignsFile = userService.findCampaignFile(fileId, session.getClientId());

            if (campaignsFile != null) {
                campaignsFile.setStatus(CampaignsFiles.STATUS_ARCHIVED);

                userService.updateCampaignFile(campaignsFile);

                resp.put("response", RequestUtils.OK);
            }
        }

        return RequestUtils.printResult(resp.toString(), response);
    }

    public static JSONArray getFilesInformation(List<Files> campaignFiles) throws Exception {
        
        JSONArray fileInformation = new JSONArray();
        
        for (Files file : campaignFiles) {
            JSONObject jsonCampaignFile = new JSONObject();
            jsonCampaignFile.put("id", file.getId());
            jsonCampaignFile.put("name", file.getFilename());
            jsonCampaignFile.put("created", file.getCreatedDt());

            fileInformation.put(jsonCampaignFile);
        }

        return fileInformation;
    } 
}

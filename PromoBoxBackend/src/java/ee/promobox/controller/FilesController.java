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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class FilesController {

    @Autowired
    private SessionService sessionService;

    @Autowired
    private UserService userService;

    private final static Logger log = LoggerFactory.getLogger(
            FilesController.class);

    private final static String FILE_DIRECTORY = "C:\\Users\\Dan\\Desktop\\KioskFiles\\";
    private final static String TEMP = FILE_DIRECTORY + "TEMP\\";

    @RequestMapping("files/upload")
    public ModelAndView uploadFile(
            @RequestParam String token,
            @RequestParam int campaignId,
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
                    String userFilePath = FILE_DIRECTORY + session.getClientId() + "\\";
                    session.getClientId();
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
                    long fileSize = multipartFile.getSize() / 1024;

                    // upload file to the temp folder on server
                    File physicalFile = new File(TEMP + fileName);

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
                    databaseFile.setSize((int) fileSize);
                    userService.addFile(databaseFile);

                    // populate campaign files table with data about file
                    CampaignsFiles campaignFile = new CampaignsFiles();
                    campaignFile.setClientId(session.getClientId());
                    campaignFile.setAdCampaignsId(campaignId);
                    campaignFile.setFileId(databaseFile.getId());
                    campaignFile.setFileType(fileTypeNumber);
                    campaignFile.setOrderId(null);
                    userService.addCampaignFile(campaignFile);

                    // move file to the users folder and rename it to its DB id
                    physicalFile.renameTo(new File(userFilePath + databaseFile.getId() + "." + fileType));

                    resp.put("response", "OK");
                }
            }
        }

        return RequestUtils.printResult(resp.toString(), response);
    }

    @RequestMapping("files/show")
    public ModelAndView showCampaignFiles(
            @RequestParam String token,
            @RequestParam int campaignId,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        JSONObject resp = RequestUtils.getErrorResponse();
        Session session = sessionService.findSession(token);

        if (session != null) {
            List<Files> campaignFiles = userService.findUsersCampaignFiles(campaignId, session.getClientId());

            if (campaignFiles != null) {
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
}

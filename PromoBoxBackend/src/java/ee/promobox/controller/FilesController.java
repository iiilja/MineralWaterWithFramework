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
import ee.promobox.jms.FileDto;
import ee.promobox.service.Session;
import ee.promobox.service.SessionService;
import ee.promobox.service.UserService;
import ee.promobox.util.FileUtils;
import ee.promobox.util.ImageOP;
import ee.promobox.util.RequestUtils;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import javax.jms.Destination;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
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

    @Autowired
    @Qualifier("fileDestination")
    private Destination fileDestination;
    @Autowired
    private JmsTemplate jmsTemplate;

    private final static Logger log = LoggerFactory.getLogger(
            FilesController.class);

    @RequestMapping("queue-example")
    public ModelAndView showCampaignFiles(
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        FileDto fileDto = new FileDto();
        fileDto.setId(10);
        jmsTemplate.convertAndSend(fileDestination, fileDto);

        return null;
    }

    @RequestMapping(value = "token/{token}/campaigns/{id}/files", method = RequestMethod.GET)
    public void showCampaignFiles(
            @PathVariable("token") String token,
            @PathVariable("id") int campaignId,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        JSONObject resp = new JSONObject();
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

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

                response.setStatus(HttpServletResponse.SC_OK);
                RequestUtils.printResult(resp.toString(), response);
            }
        } else {
            RequestUtils.sendUnauthorized(response);
        }
    }

    @RequestMapping(value = "token/{token}/campaigns/{id}/files", method = RequestMethod.POST)
    public void uploadFile(
            @PathVariable("token") String token,
            @PathVariable("id") int campaignId,
            @ModelAttribute FileUploadCommand command,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        JSONObject resp = new JSONObject();
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

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
                    String temporaryFolder = config.getDataDir() + "TEMP" + File.separator;
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

                    // making sure that filetype is legal
                    if (fileTypeNumber != 0) {
                        long fileSize = multipartFile.getSize();

                        // upload file to the temp folder on server
                        File physicalFile = new File(temporaryFolder + fileName);

                        byte[] bytes = multipartFile.getBytes();

                        try (BufferedOutputStream stream = new BufferedOutputStream(
                                new FileOutputStream(physicalFile))) {
                            stream.write(bytes);
                        } catch (Exception ex) {
                            log.error(ex.getMessage(), ex);
                            RequestUtils.printResult(resp.toString(), response);
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
                        // assign new name for file in database
                        databaseFile.setFilename(databaseFile.getId() + "." + fileType);
                        userService.updateFile(databaseFile);
                        

                        FileDto fileDto = new FileDto();
                        fileDto.setId(databaseFile.getId());
                        jmsTemplate.convertAndSend(fileDestination, fileDto);
                        
                        // now when we are almost done, lets convert file to appropriate file format
                        FileUtils.convertFile(new File(userFilePath + databaseFile.getId() + "." + fileType));

                        response.setStatus(HttpServletResponse.SC_OK);
                        RequestUtils.printResult(resp.toString(), response);
                    }
                }
            }
        } else {
            RequestUtils.sendUnauthorized(response);
        }

    }

    @RequestMapping(value = "token/{token}/files/archive/{id}", method = RequestMethod.PUT)
    public void archiveCampaignFiles(
            @PathVariable("token") String token,
            @PathVariable("id") int fileId,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        JSONObject resp = new JSONObject();
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        Session session = sessionService.findSession(token);

        if (session != null) {
            CampaignsFiles campaignsFile = userService.findCampaignFile(fileId, session.getClientId());

            if (campaignsFile != null) {
                campaignsFile.setStatus(CampaignsFiles.STATUS_ARCHIVED);

                userService.updateCampaignFile(campaignsFile);

                response.setStatus(HttpServletResponse.SC_OK);
                RequestUtils.printResult(resp.toString(), response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } else {
            RequestUtils.sendUnauthorized(response);
        }
    }

    @RequestMapping("files/{id}")
    public void getFile(
            @PathVariable("id") int id,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        JSONObject resp = new JSONObject();
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        Files dbFile = userService.findFileById(id);

        // if this file exists
        if (dbFile != null) {

            // set proper headers for testing
            response.setHeader("Content-disposition", "attachment;filename=" + dbFile.getFilename());
            response.setHeader("Content-type", "application/octet-stream");
            response.setHeader("Content-length", dbFile.getSize().toString());

            // open file
            File file = new File(dbFile.getPath() + dbFile.getFilename());

            FileInputStream fileInputStream = new FileInputStream(file);
            OutputStream outputStream = response.getOutputStream();

            IOUtils.copy(fileInputStream, outputStream);

            IOUtils.closeQuietly(fileInputStream);
            IOUtils.closeQuietly(outputStream);

            response.setStatus(HttpServletResponse.SC_OK);
            RequestUtils.printResult(resp.toString(), response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
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

    @RequestMapping(value = "convert", method = RequestMethod.GET)
    public void convertTester(
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        ImageOP converter = new ImageOP("C:\\Program Files\\ImageMagick-6.8.9-Q16\\convert.exe");
        converter.input(new File(config.getDataDir() + "input.png"));
        converter.resize(1920, 1080);
        converter.processToFile(new File(config.getDataDir() + "output.png"));
    }
}

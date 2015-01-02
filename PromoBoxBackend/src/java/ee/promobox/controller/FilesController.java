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
import ee.promobox.entity.Devices;
import ee.promobox.entity.Files;
import ee.promobox.jms.ClientThreadPool;
import ee.promobox.jms.FileDto;
import ee.promobox.jms.FileDtoConsumer;
import ee.promobox.jms.ThreadPool;
import ee.promobox.service.FileService;
import ee.promobox.jms.FileDtoProducer;
import ee.promobox.service.Session;
import ee.promobox.service.SessionService;
import ee.promobox.service.UserService;
import ee.promobox.util.FileTypeUtils;
import ee.promobox.util.RequestUtils;

import java.beans.DesignMode;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.jms.Destination;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class FilesController {

    @Autowired
    private SessionService sessionService;

    @Autowired
    private UserService userService;
    
    @Autowired
    private FileService fileService;

    @Autowired
    private KioskConfig config;

    @Autowired
    private ClientThreadPool clientThreadPool;
    
    private final static int AVERAGE_AUDIO_BITRATE = 128 * 1024;
    private final static int AVERAGE_VIDEO_BITRATE = 198 * 1024;

    private final static Logger log = LoggerFactory.getLogger(
            FilesController.class);
    
    
    @Scheduled(cron = "00 00 2 * * ?")
    public void moveArchivedCampaignFiles() throws Exception {
        for (AdCampaigns ac: userService.findCampaignsArchiveCandidates()) {
            for (Files f: userService.findCampaignFiles(ac.getId())) {
                int clientId = f.getClientId();
                int fileId = f.getId();

                File rawFile = fileService.getRawFile(clientId, fileId);
                File outputFile = fileService.getOutputFile(clientId, fileId);
                File outputPortFile = fileService.getOutputPortFile(clientId, fileId);
                File thumbFile = fileService.getThumbFile(clientId, fileId);
                
                
                moveFile(rawFile, clientId);
                moveFile(outputFile, clientId);
                moveFile(outputPortFile, clientId);
                moveFile(thumbFile, clientId);
            }
            
            ac.setFilesArchived(true);
            userService.updateCampaign(ac);
        }
    }
    
    
    private void moveFile(File f, int clientId) {
        if (f.exists()) {
            try {
                f.renameTo(new File(fileService.getArchiveClientFolder(clientId), f.getName()));
            } catch(Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        }
    }
    
    @RequestMapping(value = "token/{token}/campaigns/{id}/files/order", method = RequestMethod.PUT)
    public void saveFilesOrder(
            @PathVariable("token") String token,
            @PathVariable("id") int campaignId,
            @RequestBody String json,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        
        JSONObject resp = new JSONObject();
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        Session session = sessionService.findSession(token);

        if (session != null) {
            AdCampaigns campaign = userService.findCampaignByIdAndClientId(campaignId, session.getClientId());

            // if this campaign belongs to user
            if (campaign != null) {
                JSONObject objectGiven = new JSONObject(json);
                
                JSONArray filesOrderArray = objectGiven.getJSONArray("filesOrder");
                for (int i = 0; i < filesOrderArray.length(); i++) {
                    int fileId = filesOrderArray.getJSONObject(i).getInt("id");
                    int orderId = filesOrderArray.getJSONObject(i).getInt("orderId");
                    
                    userService.updateCampaignFileOrder(fileId, orderId, campaignId, session.getClientId());
                }
                
                response.setStatus(HttpServletResponse.SC_OK);
                RequestUtils.printResult(resp.toString(), response);
            }
        } else {
            RequestUtils.sendUnauthorized(response);
        }
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
            List<CampaignsFiles> campaignFiles = userService.findUsersCampaignFiles(campaignId, session.getClientId());

            if (!campaignFiles.isEmpty()) {
                JSONArray jsonCampaignFiles = new JSONArray();
                
                for (CampaignsFiles file : campaignFiles) {
                    JSONObject jsonCampaignFile = new JSONObject();
                    
                    jsonCampaignFile.put("id", file.getId());
                    jsonCampaignFile.put("orderId", file.getOrderId());
                    jsonCampaignFile.put("name", file.getFilename());
                    jsonCampaignFile.put("ext", "." + FilenameUtils.getExtension(file.getFilename()));
                    jsonCampaignFile.put("status", file.getStatus());
                    jsonCampaignFile.put("created", file.getCreatedDt());
                    jsonCampaignFile.put("fileType", file.getFileType());

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
            HttpServletResponse response) throws IOException{

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
                    // if users folder doesnt exist, create one
                    File userFolder = fileService.getClientFolder(session.getClientId());

                    if (!userFolder.exists()) {
                        userFolder.mkdir();
                    }

                    // get data about the file
                    String fileName = multipartFile.getOriginalFilename();
                    String fileType = FilenameUtils.getExtension(fileName);
                    
                    int fileTypeNumber = FileTypeUtils.determineFileTypeNumber(fileType);

                    // making sure that filetype is legal
                    if (fileTypeNumber != FileTypeUtils.INVALID_FILE_TYPE) {
                        long fileSize = multipartFile.getSize();

                        File physicalFile = new File(temporaryFolder + fileName);
                                                
                        try {
                            
                            BufferedOutputStream outputStream = new BufferedOutputStream(
                                new FileOutputStream(physicalFile));
                            
                            IOUtils.copy(multipartFile.getInputStream(), outputStream);
                            
                            IOUtils.closeQuietly(outputStream);
                            
                        } catch (Exception ex) {
                            log.error(ex.getMessage(), ex);
                        }

                        if (physicalFile.exists() && physicalFile.length() > 0) {
                            Files databaseFile = new Files();

                            databaseFile.setFilename(fileName);
                            databaseFile.setFileType(fileTypeNumber);
                            databaseFile.setPath(userFolder.getCanonicalPath());
                            databaseFile.setCreatedDt(new Date(System.currentTimeMillis()));
                            databaseFile.setSize(fileSize);
                            databaseFile.setClientId(session.getClientId());
                            databaseFile.setContentLength(0L);
                            if (fileTypeNumber == FileTypeUtils.FILE_TYPE_AUDIO) {
                                databaseFile.setContentLength(fileSize / AVERAGE_AUDIO_BITRATE);
                            } else if (fileTypeNumber == FileTypeUtils.FILE_TYPE_VIDEO) {
                                databaseFile.setContentLength(fileSize / AVERAGE_VIDEO_BITRATE);
                            }

                            userService.addFile(databaseFile);

                            // populate campaign files table with data about file
                            CampaignsFiles campaignFile = new CampaignsFiles();

                            campaignFile.setClientId(session.getClientId());
                            campaignFile.setAdCampaignsId(campaignId);
                            campaignFile.setFileId(databaseFile.getId());
                            campaignFile.setFileType(fileTypeNumber);
                            campaignFile.setOrderId(databaseFile.getId());
                            campaignFile.setStatus(CampaignsFiles.STATUS_UPLOADED);
                            campaignFile.setCreatedDt(new Date());
                            campaignFile.setFilename(fileName);

                            userService.addCampaignFile(campaignFile);
                            
                            campaignFile.setOrderId(campaignFile.getId());

                            campaign.setUpdateDate(new Date());

                            userService.updateCampaign(campaign);

                            File f = fileService.getRawFile(session.getClientId(), databaseFile.getId());

                            if (!physicalFile.renameTo(f)) {
                                log.error("Error rename file");
                            }

                            FileDto fileDto = new FileDto(campaignFile.getId(),
                                    session.getClientId(), fileTypeNumber, fileType);

                            log.info("convert");
                            FileDtoProducer producer = new FileDtoProducer(fileDto);
                            FileDtoConsumer consumer = new FileDtoConsumer(session.getClientId(), config, userService, fileService);
                            
                            ThreadPool threadPool = clientThreadPool.getClientThreadPool(session.getClientId());
                            threadPool.execute(consumer);
                            threadPool.execute(producer);

                            response.setStatus(HttpServletResponse.SC_OK);
                            
                            RequestUtils.printResult(resp.toString(), response);
                        }

                    } else {
                        log.error("Invalid file type: " + fileType);
                    }
                }
            }
        } else {
            RequestUtils.sendUnauthorized(response);
        }

    }
    
    @RequestMapping(value = "token/{token}/campaigns/{id}/files/{file}/rotate/{angle}", method = RequestMethod.PUT)
    public void rotwteFile(
            @PathVariable("token") String token,
            @PathVariable("id") int campaignId,
            @PathVariable("file") int fileId,
            @PathVariable("angle") int angle,
            HttpServletRequest request,
            HttpServletResponse response){

        JSONObject resp = new JSONObject();
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        Session session = sessionService.findSession(token);

        if (session != null) {
            AdCampaigns campaign = userService.findCampaignByIdAndClientId(campaignId, session.getClientId());

            // if this campaign belongs to user
            if (campaign != null) {
                CampaignsFiles cFile = userService.findCampaignFileById(fileId);
                Files databaseFile = userService.findFileById(cFile.getFileId());
                
                angle = databaseFile.getAngle() + angle;
                if (angle > 360) {
                    angle -= 360;
                }
                databaseFile.setAngle(angle);
                userService.updateFile(databaseFile);

                String fileType = FilenameUtils.getExtension(databaseFile.getFilename());
                
                FileDto fileDto = new FileDto(cFile.getId(), session.getClientId(), databaseFile.getFileType(), fileType);
                fileDto.setRotate(angle);
                
                cFile.setStatus(CampaignsFiles.STATUS_CONVERTING);
                userService.updateCampaignFile(cFile);
                
                FileDtoProducer producer = new FileDtoProducer(fileDto);
                FileDtoConsumer consumer = new FileDtoConsumer(session.getClientId(), config, userService, fileService);
                
                ThreadPool threadPool = clientThreadPool.getClientThreadPool(session.getClientId());
                threadPool.execute(consumer);
                threadPool.execute(producer);
                

                response.setStatus(HttpServletResponse.SC_OK);

                RequestUtils.printResult(resp.toString(), response);
            }
        } else {
            RequestUtils.sendUnauthorized(response);
        }

    }
    
    @RequestMapping(value = "token/{token}/files/status", method = RequestMethod.GET)
    public void getFilesStatus(
            @PathVariable("token") String token,
            @RequestParam List<Integer> files,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        
        JSONObject resp = new JSONObject();
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        Session session = sessionService.findSession(token);

        if (session != null) {
            JSONArray filesArray = new JSONArray();
            for (CampaignsFiles campaignsFile: userService.findCampaignFileByIds(files)) {
                JSONObject fileObj = new JSONObject();
                fileObj.put("id", campaignsFile.getId());
                fileObj.put("status", campaignsFile.getStatus());
                
                filesArray.put(fileObj);
            }
            resp.put("files", filesArray);
            
            response.setStatus(HttpServletResponse.SC_OK);
            RequestUtils.printResult(resp.toString(), response);
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
            Files dbFile = userService.findFileById(campaignsFile.getFileId());
            
            if (campaignsFile != null && dbFile != null) {
                
                campaignsFile.setStatus(CampaignsFiles.STATUS_ARCHIVED);

                userService.updateCampaignFile(campaignsFile);

                AdCampaigns campaign = userService.findCampaignByCampaignId(campaignsFile.getAdCampaignsId());
                campaign.setUpdateDate(new Date());
                campaign.setCountFiles(campaign.getCountFiles() - 1);
                if (dbFile.getFileType() == FileTypeUtils.FILE_TYPE_AUDIO) {
                    campaign.setCountAudios(campaign.getCountAudios() - 1);
                    campaign.setAudioLength(campaign.getAudioLength() - dbFile.getContentLength());
                } else if (dbFile.getFileType() == FileTypeUtils.FILE_TYPE_IMAGE) {
                    campaign.setCountImages(campaign.getCountImages() - 1);
                    
                } else if (dbFile.getFileType() == FileTypeUtils.FILE_TYPE_VIDEO) {
                    campaign.setCountVideos(campaign.getCountVideos() - 1);
                    campaign.setVideoLength(campaign.getVideoLength() - dbFile.getContentLength());
                }

                userService.updateCampaign(campaign);

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
            @RequestParam(required = false) Integer orient,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        CampaignsFiles dbFile = userService.findCampaignFileById(id);

        if (dbFile != null && dbFile.getStatus() == CampaignsFiles.STATUS_ACTIVE) {
            response.setStatus(HttpServletResponse.SC_OK);

            if (dbFile.getFileType() == FileTypeUtils.FILE_TYPE_VIDEO) {
                response.setContentType("video/webm");
            } else if (dbFile.getFileType() == FileTypeUtils.FILE_TYPE_AUDIO) {
                response.setContentType("audio/mpeg");
            } else if (dbFile.getFileType() == FileTypeUtils.FILE_TYPE_IMAGE) {
                response.setContentType("image/png");
            }
                        
            
            File file = fileService.getOutputFile(dbFile.getClientId(), dbFile.getFileId());
                                    
            if (orient!=null && orient == Devices.ORIENTATION_PORTRAIT_EMULATION) {
                File filePort = fileService.getOutputPortFile(dbFile.getClientId(), dbFile.getFileId());
                
                if (filePort.exists()) {
                    file = filePort;
                }
            }
            
            response.setContentLength((int)file.length());

            FileInputStream fileInputStream = new FileInputStream(file);
            OutputStream outputStream = response.getOutputStream();

            IOUtils.copy(fileInputStream, outputStream);

            IOUtils.closeQuietly(fileInputStream);
            IOUtils.closeQuietly(outputStream);

        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    
    @RequestMapping("files/thumb/{id}")
    public void getFileThumb(
            @PathVariable("id") int id,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        CampaignsFiles dbFile = userService.findCampaignFileById(id);

        // if this file exists
        if (dbFile != null) {
            response.setStatus(HttpServletResponse.SC_OK);

            File file = null;
            OutputStream outputStream = response.getOutputStream();

            if (dbFile.getFileType() != FileTypeUtils.FILE_TYPE_AUDIO 
                    && !(dbFile.getStatus() == CampaignsFiles.STATUS_UPLOADED || dbFile.getStatus() == CampaignsFiles.STATUS_CONVERTING)) {
                file = fileService.getThumbFile(dbFile.getClientId(), dbFile.getFileId());

                FileInputStream fileInputStream = new FileInputStream(file);
                
                IOUtils.copy(fileInputStream, outputStream);
                
                IOUtils.closeQuietly(fileInputStream);
            } else if (dbFile.getFileType() == FileTypeUtils.FILE_TYPE_AUDIO 
                    && !(dbFile.getStatus() == CampaignsFiles.STATUS_UPLOADED || dbFile.getStatus() == CampaignsFiles.STATUS_CONVERTING)) {
                InputStream is = getClass().getClassLoader().getResourceAsStream("ee/promobox/assets/play.png");
                IOUtils.copy(is, outputStream);
                IOUtils.closeQuietly(is);
            } else {
                response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
                
                InputStream is = getClass().getClassLoader().getResourceAsStream("ee/promobox/assets/converting.png");
                IOUtils.copy(is, outputStream);
                IOUtils.closeQuietly(is);
            }

            IOUtils.closeQuietly(outputStream);

        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    public static JSONArray getFilesInformation(List<CampaignsFiles> campaignFiles) throws Exception {

        JSONArray fileInformation = new JSONArray();

        for (CampaignsFiles file : campaignFiles) {
            JSONObject jsonCampaignFile = new JSONObject();
            
            jsonCampaignFile.put("id", file.getId());
            jsonCampaignFile.put("fileId",file.getFileId());
            jsonCampaignFile.put("orderId", file.getOrderId());
            jsonCampaignFile.put("name", file.getFilename());
            jsonCampaignFile.put("fileType", file.getFileType());
            jsonCampaignFile.put("created", file.getCreatedDt());
            jsonCampaignFile.put("status", file.getStatus());
            jsonCampaignFile.put("ext", "." + FilenameUtils.getExtension(file.getFilename()));

            fileInformation.put(jsonCampaignFile);
        }

        return fileInformation;
    }


}

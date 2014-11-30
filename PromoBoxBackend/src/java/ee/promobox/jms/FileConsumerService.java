/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ee.promobox.jms;

import ee.promobox.KioskConfig;
import ee.promobox.entity.AdCampaigns;
import ee.promobox.entity.CampaignsFiles;
import ee.promobox.entity.Files;
import ee.promobox.service.FileService;
import ee.promobox.service.UserService;
import ee.promobox.util.FileTypeUtils;
import ee.promobox.util.ImageOP;
import ee.promobox.util.VideoOP;
import java.io.File;
import java.util.Date;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.listener.adapter.MessageListenerAdapter;

/**
 *
 * @author vitalispiridonov
 */
public class FileConsumerService extends MessageListenerAdapter {
    
    @Autowired
    private KioskConfig config;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private FileService fileService;
    
    public static Log log = LogFactory.getLog(FileConsumerService.class);
    
    public void handleMessage(FileDto fileDto) {
        log.info("Converting file:");
        
        log.info("File id: " + fileDto.getId());
        log.info("File extention: " + fileDto.getExtention());
        
        boolean result = convertFile(fileDto);
        
        CampaignsFiles cFile = userService.findCampaignFileById(fileDto.getId());
        
        if (result) {
            cFile.setStatus(CampaignsFiles.STATUS_ACTIVE);
            
            File file = fileService.getOutputFile(fileDto.getClientId(), fileDto.getId());
            cFile.setSize((int)file.length());
            
            Files dbFile = userService.findFileById(fileDto.getId());
            
            userService.updateCampaignFile(cFile);
            
            AdCampaigns camp = userService.findCampaignByCampaignId(cFile.getAdCampaignsId());
            camp.setUpdateDate(new Date());
            
            camp.setCountFiles(camp.getCountFiles() + 1);
            if (fileDto.getFileType() == FileTypeUtils.FILE_TYPE_IMAGE) {
                camp.setCountImages(camp.getCountImages() + 1);
            } else if (fileDto.getFileType() == FileTypeUtils.FILE_TYPE_AUDIO) {
                camp.setCountAudios(camp.getCountAudios() + 1);
                camp.setAudioLength(camp.getAudioLength() + dbFile.getContentLength());
            } else if (fileDto.getFileType() == FileTypeUtils.FILE_TYPE_VIDEO) {
                camp.setCountVideos(camp.getCountVideos() + 1);
                camp.setVideoLength(camp.getVideoLength() + dbFile.getContentLength());
            }
            
            userService.updateCampaign(camp);
        }
    }
    
    public boolean convertFile(FileDto f) {
        String type = f.getExtention();
        
        boolean result = false;

        switch (type.toUpperCase()) {
            case "JPG":
                result = convertImage(f);
                break;
            case "JPEG":
                result = convertImage(f);
                break;
            case "PNG":
                result = convertImage(f);
                break;
            case "BMP":
                result = convertImage(f);
                break;
            case "MP3":
                result = copyFile(f);
                break;
            case "MP4":
                result = convertVideo(f);
                break;
            case "M2TS":
                result = convertVideo(f);
                break;
            case "AAC":
                result = copyFile(f);
                break;
            case "AVI":
                result = convertVideo(f);
                break;
            case "MOV":
                result = convertVideo(f);
                break;
            case "WMV":
                result = convertVideo(f);
                break;
            case "PDF":
                result = convertPdf(f);
                break;
            default:
                break;
        }
        
        return result;
    }
    
    
    private boolean convertPdf(FileDto f) {
        int clientId = f.getClientId();
        int fileId = f.getId();
        
        File rawFile = fileService.getRawFile(clientId, fileId);
        File outputFile = fileService.getOutputFile(clientId, fileId);
        File outputPortFile = fileService.getOutputPortFile(clientId, fileId);
        File thumbFile = fileService.getThumbFile(clientId, fileId);
        
        ImageOP imageConvert = new ImageOP(config.getImageMagick());

        imageConvert.density(300);
        imageConvert.flatten();
        imageConvert.input(rawFile);
        imageConvert.page(0);
        imageConvert.background("white");
        imageConvert.resize(1920, 1920);
        imageConvert.rotate(f.getRotate());
        
                
        imageConvert.outputFormat("png");
        
        if (imageConvert.processToFile(outputFile)) {
            
            imageConvert = new ImageOP(config.getImageMagick());

            imageConvert.input(rawFile);            
            
            imageConvert.rotate(270 + f.getRotate());
            
            imageConvert.outputFormat("png");

            imageConvert.processToFile(outputPortFile);

            imageConvert = new ImageOP(config.getImageMagick());

            imageConvert.input(outputFile);

            imageConvert.resize(250, 250);

            imageConvert.background("white");
            imageConvert.gravity("center");
            imageConvert.extent("250x250");
            imageConvert.rotate(f.getRotate());

            imageConvert.processToFile(thumbFile);
            
            return true;
        }

        return false;
    }
    
    
    private boolean copyFile(FileDto f) {
        int clientId = f.getClientId();
        int fileId = f.getId();
        
        File rawFile = fileService.getRawFile(clientId, fileId);
        File outputFile = fileService.getOutputFile(clientId, fileId);

        try {
            FileUtils.copyFile(rawFile, outputFile);
        } catch(Exception ex) {
            log.error(ex.getMessage(), ex);
            return false;
        }
        
        return true;
    }

    private boolean convertImage(FileDto f) {
        int clientId = f.getClientId();
        int fileId = f.getId();
        
        File rawFile = fileService.getRawFile(clientId, fileId);
        File outputFile = fileService.getOutputFile(clientId, fileId);
        File outputPortFile = fileService.getOutputPortFile(clientId, fileId);
        File thumbFile = fileService.getThumbFile(clientId, fileId);
        
        ImageOP imageConvert = new ImageOP(config.getImageMagick());

        imageConvert.input(rawFile);
        imageConvert.outputFormat("png");
        imageConvert.resize(1920, 1920);
        imageConvert.rotate(f.getRotate());

        imageConvert.processToFile(outputFile);
        
        
        
        
        imageConvert = new ImageOP(config.getImageMagick());

        imageConvert.input(rawFile);
        imageConvert.outputFormat("png");
        imageConvert.rotate(270 + f.getRotate());

        imageConvert.processToFile(outputPortFile);

        imageConvert = new ImageOP(config.getImageMagick());

        imageConvert.input(rawFile);
        imageConvert.outputFormat("png");
        imageConvert.resize(250, 250);
        imageConvert.rotate(f.getRotate());

        imageConvert.background("white");
        imageConvert.gravity("center");
        imageConvert.extent("250x250");

        return imageConvert.processToFile(thumbFile);
    }

    private boolean convertVideo(FileDto f) {
        int clientId = f.getClientId();
        int fileId = f.getId();
        
        File rawFile = fileService.getRawFile(clientId, fileId);
        File outputFile = fileService.getOutputFile(clientId, fileId);
        File thumbFile = fileService.getThumbFile(clientId, fileId);
        
        VideoOP videoConvert = new VideoOP(config.getAvconv());
        
        videoConvert.input(rawFile);
        videoConvert.thumbnail();
        videoConvert.scale("500:-1");
        videoConvert.format("image2");
        
        videoConvert.processToFile(thumbFile);
        if (f.getRotate() != 0) {
            ImageOP imageConvert = new ImageOP(config.getImageMagick());

            imageConvert.input(thumbFile);
            imageConvert.outputFormat("png");
            imageConvert.resize(250, 250);
            imageConvert.rotate(f.getRotate());

            imageConvert.background("white");
            imageConvert.gravity("center");
            imageConvert.extent("250x250");

            imageConvert.processToFile(thumbFile);
        } 
        
        ImageOP imageConvert = new ImageOP(config.getImageMagick());

        imageConvert.input(thumbFile);
        imageConvert.outputFormat("png");
        imageConvert.resize(250, 250);

        imageConvert.background("white");
        imageConvert.gravity("center");
        imageConvert.extent("250x250");

        imageConvert.processToFile(thumbFile);
        
        boolean result = false;
        videoConvert = new VideoOP(config.getAvconv());
        
        if (f.getRotate() == 0) {
            videoConvert.input(rawFile)
                    .codecVideo("libvpx")
                    .scale("-1:720")
                    .bitrateVideo("1M")
                    .maxrate("1M")
                    .format("webm");

            result = videoConvert.processToFile(outputFile);
        
        } else {
            videoConvert = new VideoOP(config.getAvconv());
                videoConvert.input(rawFile)
                .codecVideo("libvpx")
                .bitrateVideo("1M")
                .maxrate("1M")
                .format("webm")
                .overwrite();
                
                
            if (f.getRotate() == 90) {
                videoConvert.vf("scale=-1:720", "transpose=1");
            } else if (f.getRotate() == 180) { 
                videoConvert.vf("scale=-1:720", "transpose=1,transpose=1");
            } else if (f.getRotate() == 270) {
                videoConvert.vf("scale=-1:720", "transpose=2");
            }
            
            result = videoConvert.processToFile(outputFile);
        }

        return result;
    }
    
}

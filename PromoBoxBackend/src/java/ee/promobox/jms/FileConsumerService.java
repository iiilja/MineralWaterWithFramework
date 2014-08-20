/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ee.promobox.jms;

import ee.promobox.KioskConfig;
import ee.promobox.entity.CampaignsFiles;
import ee.promobox.service.UserService;
import ee.promobox.util.ImageOP;
import ee.promobox.util.VideoOP;
import java.io.File;
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
    
    public static Log log = LogFactory.getLog(FileConsumerService.class);
    
    public void handleMessage(FileDto fileDto) {
        log.info("Converting file:");
        
        log.info("File id: " + fileDto.getId());
        log.info("File extention: " + fileDto.getExtention());
        
        boolean result = convertFile(fileDto);
        
        CampaignsFiles cFile = userService.findCampaignFileById(fileDto.getId());
        
        if (result) {
            cFile.setStatus(CampaignsFiles.STATUS_ACTIVE);
            
            File file = new File(fileDto.getFile().getParent() + File.separator + fileDto.getId() + "_output");
            cFile.setSize((int)file.length());
            
            userService.updateCampaignFile(cFile);
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
            case "MP3":
                result = copyFile(f);
                break;
            case "MP4":
                result = convertVideo(f);
                break;
            case "m2ts":
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
            case "PDF":
                result = convertPdf(f);
                break;
            default:
                break;
        }
        
        return result;
    }
    
    
    private boolean convertPdf(FileDto f) {
        ImageOP imageConvert = new ImageOP(config.getImageMagick());

        imageConvert.input(f.getFile());

        imageConvert.density(300);
        imageConvert.resize(25, null, true);

        if (imageConvert.processToFile(new File(f.getFile().getParent() + File.separator + f.getId() + "_output"))) {

            imageConvert = new ImageOP(config.getImageMagick());

            imageConvert.input(new File(f.getFile().getParent() + File.separator + f.getId() + "_output"));

            imageConvert.resize(250, 250);

            imageConvert.background("white");
            imageConvert.gravity("center");
            imageConvert.extent("250x250");

            imageConvert.processToFile(new File(f.getFile().getParent() + File.separator + f.getId() + "_thumb"));
            
            return true;
        }

        return false;
    }
    
    
    private boolean copyFile(FileDto f) {
        File output = new File(f.getFile().getParent() + File.separator + f.getId() + "_output");
        
        try {
            FileUtils.copyFile(f.getFile(), output);
        } catch(Exception ex) {
            log.error(ex.getMessage(), ex);
            return false;
        }
        
        return true;
    }

    private boolean convertImage(FileDto f) {
        ImageOP imageConvert = new ImageOP(config.getImageMagick());

        imageConvert.input(f.getFile());
        imageConvert.resize(1920, 1080);

        imageConvert.processToFile(new File(f.getFile().getParent() + File.separator + f.getId() + "_output"));

        imageConvert = new ImageOP(config.getImageMagick());

        imageConvert.input(f.getFile());

        imageConvert.resize(250, 250);

        imageConvert.background("white");
        imageConvert.gravity("center");
        imageConvert.extent("250x250");

        return imageConvert.processToFile(new File(f.getFile().getParent() + File.separator + f.getId() + "_thumb"));
    }

    private boolean convertVideo(FileDto f) {
        VideoOP videoConvert = new VideoOP(config.getAvconv());

        videoConvert.input(f.getFile())
                .codecVideo("libx264")
                .scale("-1:720")
                .preset("slow")
                .crf(19)
                .codecAudio("mp3")
                .format("mp4")
                .bitrateAudio("128k");

        return videoConvert.processToFile(new File(f.getFile().getParent() + File.separator + f.getId() + "_output"));
    }
    
}

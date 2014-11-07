/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ee.promobox.jms;

import ee.promobox.KioskConfig;
import ee.promobox.entity.AdCampaigns;
import ee.promobox.entity.CampaignsFiles;
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
            
            AdCampaigns camp = userService.findCampaignByCampaignId(cFile.getAdCampaignsId());
            camp.setUpdateDate(new Date());
            
            camp.setCountFiles(camp.getCountFiles() + 1);
            if (fileDto.getFileType() == FileTypeUtils.FILE_TYPE_IMAGE) {
                camp.setCountImages(camp.getCountImages());
            } else if (fileDto.getFileType() == FileTypeUtils.FILE_TYPE_AUDIO) {
                camp.setCountAudios(camp.getCountAudios() + 1);
            } else if (fileDto.getFileType() == FileTypeUtils.FILE_TYPE_VIDEO) {
                camp.setCountVideos(camp.getCountVideos() + 1);
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
        ImageOP imageConvert = new ImageOP(config.getImageMagick());

        imageConvert.density(300);
        imageConvert.flatten();
        imageConvert.input(f.getFile());
        imageConvert.page(0);
        imageConvert.background("white");
        imageConvert.resize(1920, 1920);
        
                
        imageConvert.outputFormat("png");
        
        if (imageConvert.processToFile(new File(f.getFile().getParent() + File.separator + f.getId() + "_output"))) {
            
            imageConvert = new ImageOP(config.getImageMagick());

            imageConvert.input(new File(f.getFile().getParent() + File.separator + f.getId() + "_output"));            
            
            imageConvert.rotate(270);
            
            imageConvert.outputFormat("png");

            imageConvert.processToFile(new File(f.getFile().getParent() + File.separator + f.getId() + "_output_port"));

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
        imageConvert.outputFormat("png");
        imageConvert.resize(1920, 1920);

        imageConvert.processToFile(new File(f.getFile().getParent() + File.separator + f.getId() + "_output"));
        
        imageConvert = new ImageOP(config.getImageMagick());

        imageConvert.input(f.getFile());
        imageConvert.outputFormat("png");
        imageConvert.rotate(270);

        imageConvert.processToFile(new File(f.getFile().getParent() + File.separator + f.getId() + "_output_port"));

        imageConvert = new ImageOP(config.getImageMagick());

        imageConvert.input(f.getFile());
        imageConvert.outputFormat("png");
        imageConvert.resize(250, 250);

        imageConvert.background("white");
        imageConvert.gravity("center");
        imageConvert.extent("250x250");

        return imageConvert.processToFile(new File(f.getFile().getParent() + File.separator + f.getId() + "_thumb"));
    }

    private boolean convertVideo(FileDto f) {
        VideoOP videoConvert = new VideoOP(config.getAvconv());
        
        videoConvert.input(f.getFile());
        videoConvert.thumbnail();
        videoConvert.scale("500:-1");
        videoConvert.format("image2");
        
        videoConvert.processToFile(new File(f.getFile().getParent() + File.separator + f.getId() + "_thumb"));
        
        
        ImageOP imageConvert = new ImageOP(config.getImageMagick());

        imageConvert.input(new File(f.getFile().getParent() + File.separator + f.getId() + "_thumb"));
        imageConvert.outputFormat("png");
        imageConvert.resize(250, 250);

        imageConvert.background("white");
        imageConvert.gravity("center");
        imageConvert.extent("250x250");

        imageConvert.processToFile(new File(f.getFile().getParent() + File.separator + f.getId() + "_thumb"));
        
        videoConvert = new VideoOP(config.getAvconv());
        videoConvert.input(f.getFile())
                .codecVideo("libvpx")
                .scale("-1:720")
                .bitrateVideo("1M")
                .maxrate("1M")
                .format("webm");

        return videoConvert.processToFile(new File(f.getFile().getParent() + File.separator + f.getId() + "_output"));
    }
    
}

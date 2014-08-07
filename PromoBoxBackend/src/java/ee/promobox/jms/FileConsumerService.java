/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ee.promobox.jms;

import ee.promobox.KioskConfig;
import ee.promobox.util.ImageOP;
import ee.promobox.util.VideoOP;
import java.io.File;
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
    
    public static Log log = LogFactory.getLog(FileConsumerService.class);
    
    public void handleMessage(FileDto fileDto) {
        log.info("Convert file here");
        
        log.info("File id: " + fileDto.getId());
        log.info("File extention: " + fileDto.getExtention());
        
        convertFile(fileDto);
    }
    
    public void convertFile(FileDto f) {
        String type = f.getExtention();
        
        ImageOP imageConvert = null;  
        VideoOP videoConvert = null;
        switch (type.toUpperCase()) {
            case "JPG":
            case "JPEG":
            case "PNG":
                imageConvert = new ImageOP(config.getImageMagick());
 
                imageConvert.input(f.getFile());
                imageConvert.resize(1920, 1080);

                imageConvert.processToFile(new File(f.getFile().getAbsolutePath() + "_output"));
                
                imageConvert = new ImageOP(config.getImageMagick());

                imageConvert.input(f.getFile());
                imageConvert.resize(320, 320);

                imageConvert.processToFile(new File(f.getFile().getAbsolutePath() + "_thumb"));
                break;
            case "MP3":
                break;
            case "MP4":
                break;
            case "AAC":
                break;
            case "AVI":
            case "MOV":
                videoConvert = new VideoOP(config.getAvconv());
                videoConvert.input(f.getFile())
                        .codecVideo("libx264")
                        .scale("-1:720")
                        .preset("slow")
                        .crf(19)
                        .codecAudio("libvo_aacenc")
                        .bitrateAudio("128k");
                
                videoConvert.processToFile(new File(f.getFile().getAbsolutePath() + "_output"));
                        
                
                break;
            case "PDF":
                imageConvert = new ImageOP(config.getImageMagick());

                imageConvert.input(f.getFile());
                
                imageConvert.density(300);
                imageConvert.resize(25, null, true);

                imageConvert.processToFile(new File(f.getFile().getAbsolutePath() + "_output"));
                break;
            default:
                break;
        }
    }
    
}

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
                convertImage(f);
                break;
            case "JPEG":
                convertImage(f);
                break;
            case "PNG":
                convertImage(f);
                break;
            case "MP3":
                break;
            case "MP4":
                convertVideo(f);
                break;
            case "m2ts":
                convertVideo(f);
                break;
            case "AAC":
                break;
            case "AVI":
                convertVideo(f);
                break;
            case "MOV":
                convertVideo(f);
                break;
            case "PDF":
                imageConvert = new ImageOP(config.getImageMagick());

                imageConvert.input(f.getFile());
                
                imageConvert.density(300);
                imageConvert.resize(25, null, true);

                imageConvert.processToFile(new File(f.getFile().getAbsolutePath() + "_output"));

                imageConvert = new ImageOP(config.getImageMagick());

                imageConvert.input(new File(f.getFile().getAbsolutePath() + "_output"));

                imageConvert.resize(250, 250);

                imageConvert.backgraund("black");
                imageConvert.gravity("center");
                imageConvert.extent("250x250");

                imageConvert.processToFile(new File(f.getFile().getAbsolutePath() + "_thumb"));
                break;
            default:
                break;
        }
    }

    private void convertImage(FileDto f) {
        ImageOP imageConvert = new ImageOP(config.getImageMagick());

        imageConvert.input(f.getFile());
        imageConvert.resize(1920, 1080);

        imageConvert.processToFile(new File(f.getFile().getAbsolutePath() + "_output"));

        imageConvert = new ImageOP(config.getImageMagick());

        imageConvert.input(f.getFile());

        imageConvert.resize(250, 250);

        imageConvert.backgraund("black");
        imageConvert.gravity("center");
        imageConvert.extent("250x250");

        imageConvert.processToFile(new File(f.getFile().getAbsolutePath() + "_thumb"));
    }

    private void convertVideo(FileDto f) {
        VideoOP videoConvert = new VideoOP(config.getAvconv());

        videoConvert.input(f.getFile())
                .codecVideo("libx264")
                .scale("-1:720")
                .preset("slow")
                .crf(19)
                .codecAudio("libvo_aacenc")
                .bitrateAudio("128k");

        videoConvert.processToFile(new File(f.getFile().getAbsolutePath() + "_output"));
    }
    
}

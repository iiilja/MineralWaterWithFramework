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
import org.apache.commons.io.FilenameUtils;
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
        
        convertFile(fileDto.getFile());
    }
    
    public void convertFile(File f) {
        String type = FilenameUtils.getExtension(f.getName());
        
        ImageOP imageConvert = null;  
        VideoOP videoConvert = null;
        switch (type.toUpperCase()) {
            case "JPG":
            case "JPEG":
            case "PNG":
                imageConvert = new ImageOP(config.getImageMagick());
                // input file into conveter
                imageConvert.input(f);
                imageConvert.resize(1920, 1080);
                // give correct name to file for output
                imageConvert.processToFile(new File(f.getAbsolutePath().replace(type, "_output.png")));
                
                
                imageConvert = new ImageOP(config.getImageMagick());
                // input file into conveter
                imageConvert.input(f);
                imageConvert.resize(320, 320);
                // give correct name to file for output
                imageConvert.processToFile(new File(f.getAbsolutePath().replace(type, "_thumb.png")));
                break;
            case "MP3":
                break;
            case "MP4":
                break;
            case "AAC":
                break;
            case "AVI":
            case "MOV":
                type = FilenameUtils.getExtension(f.getName());
                
                videoConvert = new VideoOP(config.getAvconv());
                videoConvert.input(f)
                        .codecVideo("libx264")
                        .scale("-1:720")
                        .preset("slow")
                        .crf(19)
                        .codecAudio("libvo_aacenc")
                        .bitrateAudio("128k");
                
                videoConvert.processToFile(new File(f.getAbsolutePath().replace(type, "_output.mp4")));
                        
                
                break;
            case "PDF":
                imageConvert = new ImageOP(config.getImageMagick());
                // input file into conveter
                imageConvert.input(f);
                
                imageConvert.density(300);
                imageConvert.resize(25, null, true);
                // give correct name to file for output
                imageConvert.processToFile(new File(f.getAbsolutePath().replace(".pdf", "_output.png")));
                break;
            default:
                break;
        }
    }
    
}

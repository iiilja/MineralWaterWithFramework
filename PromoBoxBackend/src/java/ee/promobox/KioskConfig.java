/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ee.promobox;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 *
 * @author vitalispiridonov
 */
@Configuration
@PropertySource("WEB-INF/cfg/production.properties")
public class KioskConfig {

    @Value("${dataDir}")
    private String dataDir;
    
    @Value("${imageMagick}")
    private String imageMagick;
    
    @Value("${avconv}")
    private String avconv;

    public String getDataDir() {
        return dataDir;
    }

    public void setDataDir(String dataDir) {
        this.dataDir = dataDir;
    }

    public String getImageMagick() {
        return imageMagick;
    }

    public void setImageMagick(String imageMagick) {
        this.imageMagick = imageMagick;
    }

    public String getAvconv() {
        return avconv;
    }

    public void setAvconv(String avconv) {
        this.avconv = avconv;
    }
    
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

}

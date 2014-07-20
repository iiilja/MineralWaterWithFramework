/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ee.promobox.controller;

import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author Dan
 */
public class FileUploadCommand {

    private MultipartFile file;

    //getter and setter methods
    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }
    
}

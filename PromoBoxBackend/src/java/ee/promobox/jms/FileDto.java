/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ee.promobox.jms;

import java.io.File;
import java.io.Serializable;

/**
 *
 * @author vitalispiridonov
 */
public class FileDto implements Serializable {
    
    private int id;
    private File file;
    private String extention;

    public FileDto(int id, File file, String extention) {
        this.id = id;
        this.file = file;
        this.extention = extention;
    }
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    /**
     * @return the extention
     */
    public String getExtention() {
        return extention;
    }

    /**
     * @param extention the extention to set
     */
    public void setExtention(String extention) {
        this.extention = extention;
    }

}

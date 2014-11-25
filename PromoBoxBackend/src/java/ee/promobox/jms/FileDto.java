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
    private int fileType;
    private File file;
    private String extention;
    private int rotate;

    public FileDto(int id, int fileType, File file, String extention) {
        this.id = id;
        this.file = file;
        this.fileType = fileType;
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

    public int getFileType() {
        return fileType;
    }

    public void setFileType(int fileType) {
        this.fileType = fileType;
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

    public int getRotate() {
        return rotate;
    }

    public void setRotate(int rotate) {
        this.rotate = rotate;
    }
    
}

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
    
    private int fileId;
    private int campaignFileId;
    private int clientId;
    private int fileType;
    private String extention;
    private int rotate;

    public FileDto(int campaignFileId, int clientId, int fileType, String extention) {
        this.campaignFileId = campaignFileId;
        this.clientId = clientId;
        this.fileType = fileType;
        this.extention = extention;
    }
    
    public int getCampaignFileId() {
        return campaignFileId;
    }

    public void setCampaignFileId(int campaignFileId) {
        this.campaignFileId = campaignFileId;
    }

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
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

    public int getFileId() {
        return fileId;
    }

    public void setFileId(int fileId) {
        this.fileId = fileId;
    }
    
}

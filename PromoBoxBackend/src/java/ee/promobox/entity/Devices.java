/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ee.promobox.entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author MaximDorofeev
 */
@Entity
@Table(name = "devices")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Devices.findAll", query = "SELECT d FROM Devices d"),
    @NamedQuery(name = "Devices.findById", query = "SELECT d FROM Devices d WHERE d.id = :id"),
    @NamedQuery(name = "Devices.findByUuid", query = "SELECT d FROM Devices d WHERE d.uuid = :uuid"),
    @NamedQuery(name = "Devices.findByClientId", query = "SELECT d FROM Devices d WHERE d.clientId = :clientId"),
    @NamedQuery(name = "Devices.findByStatus", query = "SELECT d FROM Devices d WHERE d.status = :status")})
public class Devices implements Serializable {

    public static final int ORIENTATION_LANDSCAPE = 1;
    public static final int ORIENTATION_PORTRAIT = 2;
    public static final int ORIENTATION_PORTRAIT_EMULATION = 3;

    public static final int RESOLUTION_1920X1080 = 1;
    public static final int RESOLUTION_1366X768 = 2;
    public static final int RESOLUTION_1280X1024 = 3;
    public static final int RESOLUTION_1280X800 = 4;
    public static final int RESOLUTION_1024X768 = 5;
    
    
    public static final int STATUS_CREATED = 0;
    public static final int STATUS_USED = 1;
    public static final int STATUS_ONLINE = 2;
    public static final int STATUS_OFFLINE = 3;
    public static final int STATUS_AHRCHIVED = 4;
    
    public static final int AUDIO_OUT_HDMI = 1;

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Column(name = "uuid")
    private String uuid;
    @Column(name = "client_id")
    private Integer clientId;
    @Column(name = "status")
    private Integer status;
    @Column(name = "description")
    private String description;
    
    @Column(name = "network_data")
    private String networkData;

    @Column(name = "orientation")
    private Integer orientation;

    @Column(name = "resolution")
    private Integer resolution;
    
    @Column(name = "audio_out")
    private Integer audioOut;
    
    @Column(name = "work_start_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date workStartAt;
    
    @Column(name = "work_end_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date workEndAt;
    
    @Column(name = "mon")
    private boolean mon;
    
    @Column(name = "tue")
    private boolean tue;
    
    @Column(name = "wed")
    private boolean wed;
    
    @Column(name = "thu")
    private boolean thu;
    
    @Column(name = "fri")
    private boolean fri;
    
    @Column(name = "sat")
    private boolean sat;
    
    @Column(name = "sun")
    private boolean sun;

    @Column(name = "last_device_request_dt")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastDeviceRequestDt;
    
    @Column(name = "free_space")
    private long freeSpace;
    
    @Column(name = "current_file_id")
    private Integer currentFileId;
    
    @Column(name = "loading_campaing_id")
    private Integer loadingCampaingId;
    
    @Column(name = "loading_compaing_progress")
    private Integer loadingCampaingProgress; 

    public Devices() {
    }

    public Devices(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Integer getClientId() {
        return clientId;
    }

    public void setClientId(Integer clientId) {
        this.clientId = clientId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getFreeSpace() {
        return freeSpace;
    }

    public void setFreeSpace(long freeSpace) {
        this.freeSpace = freeSpace;
    }

    public Date getLastDeviceRequestDt() {
        return lastDeviceRequestDt;
    }

    public void setLastDeviceRequestDt(Date lastDeviceRequestDt) {
        this.lastDeviceRequestDt = lastDeviceRequestDt;
    }

    public Integer getAudioOut() {
        return audioOut;
    }

    public void setAudioOut(Integer audioOut) {
        this.audioOut = audioOut;
    }

    public Date getWorkStartAt() {
        return workStartAt;
    }

    public void setWorkStartAt(Date workStartAt) {
        this.workStartAt = workStartAt;
    }

    public Date getWorkEndAt() {
        return workEndAt;
    }

    public void setWorkEndAt(Date workEndAt) {
        this.workEndAt = workEndAt;
    }

    public boolean isMon() {
        return mon;
    }

    public void setMon(boolean mon) {
        this.mon = mon;
    }

    public boolean isTue() {
        return tue;
    }

    public void setTue(boolean tue) {
        this.tue = tue;
    }

    public boolean isWed() {
        return wed;
    }

    public void setWed(boolean wed) {
        this.wed = wed;
    }

    public boolean isThu() {
        return thu;
    }

    public void setThu(boolean thu) {
        this.thu = thu;
    }

    public boolean isFri() {
        return fri;
    }

    public void setFri(boolean fri) {
        this.fri = fri;
    }

    public boolean isSat() {
        return sat;
    }

    public void setSat(boolean sat) {
        this.sat = sat;
    }

    public boolean isSun() {
        return sun;
    }

    public void setSun(boolean sun) {
        this.sun = sun;
    }

    public Integer getCurrentFileId() {
        return currentFileId;
    }

    public void setCurrentFileId(Integer currentFileId) {
        this.currentFileId = currentFileId;
    }

    public Integer getLoadingCampaingId() {
        return loadingCampaingId;
    }

    public void setLoadingCampaingId(Integer loadingCampaingId) {
        this.loadingCampaingId = loadingCampaingId;
    }

    public Integer getLoadingCampaingProgress() {
        return loadingCampaingProgress;
    }

    public void setLoadingCampaingProgress(Integer loadingCampaingProgress) {
        this.loadingCampaingProgress = loadingCampaingProgress;
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Devices)) {
            return false;
        }
        Devices other = (Devices) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ee.promobox.entity.Devices[ id=" + id + " ]";
    }

    public Integer getOrientation() {
        return orientation;
    }

    public void setOrientation(Integer orientation) {
        this.orientation = orientation;
    }

    public Integer getResolution() {
        return resolution;
    }

    public void setResolution(Integer resolution) {
        this.resolution = resolution;
    }

    /**
     * @return the networkData
     */
    public String getNetworkData() {
        return networkData;
    }

    /**
     * @param networkData the networkData to set
     */
    public void setNetworkData(String networkData) {
        this.networkData = networkData;
    }
}

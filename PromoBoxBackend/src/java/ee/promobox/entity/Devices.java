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

    @Column(name = "last_device_request_dt")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastDeviceRequestDt;
    @Column(name = "free_space")
    private long freeSpace;

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

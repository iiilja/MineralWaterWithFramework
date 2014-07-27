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
@Table(name = "devices_campaigns")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "DevicesCampaigns.findAll", query = "SELECT d FROM DevicesCampaigns d"),
    @NamedQuery(name = "DevicesCampaigns.findById", query = "SELECT d FROM DevicesCampaigns d WHERE d.id = :id"),
    @NamedQuery(name = "DevicesCampaigns.findByAdCampaignsId", query = "SELECT d FROM DevicesCampaigns d WHERE d.adCampaignsId = :adCampaignsId"),
    @NamedQuery(name = "DevicesCampaigns.findByDeviceId", query = "SELECT d FROM DevicesCampaigns d WHERE d.deviceId = :deviceId")})
public class DevicesCampaigns implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Column(name = "ad_campaigns_id")
    private Integer adCampaignsId;
    @Column(name = "device_id")
    private Integer deviceId;
    @Column(name = "updated_dt")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedDt;
    @Column(name = "last_device_request_dt")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastDeviceRequestDt;

    public DevicesCampaigns() {
    }

    public DevicesCampaigns(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getAdCampaignsId() {
        return adCampaignsId;
    }

    public void setAdCampaignsId(Integer adCampaignsId) {
        this.adCampaignsId = adCampaignsId;
    }

    public Integer getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Integer deviceId) {
        this.deviceId = deviceId;
    }
    
    public Date getUpdatedDt() {
        return updatedDt;
    }
    
    public void setUpdatedDt(Date updatedDt) {
        this.updatedDt = updatedDt;
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
        if (!(object instanceof DevicesCampaigns)) {
            return false;
        }
        DevicesCampaigns other = (DevicesCampaigns) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ee.promobox.entity.DevicesCampaigns[ id=" + id + " ]";
    }
    
}

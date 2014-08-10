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
import javax.persistence.Lob;
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
@Table(name = "ad_campaigns")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "AdCampaigns.findAll", query = "SELECT a FROM AdCampaigns a"),
    @NamedQuery(name = "AdCampaigns.findById", query = "SELECT a FROM AdCampaigns a WHERE a.id = :id"),
    @NamedQuery(name = "AdCampaigns.findByName", query = "SELECT a FROM AdCampaigns a WHERE a.name = :name"),
    @NamedQuery(name = "AdCampaigns.findByClientId", query = "SELECT a FROM AdCampaigns a WHERE a.clientId = :clientId"),
    @NamedQuery(name = "AdCampaigns.findByStatus", query = "SELECT a FROM AdCampaigns a WHERE a.status = :active")})
public class AdCampaigns implements Serializable {
    
    
    public static final int STATUS_CREATED = 0;
    public static final int STATUS_PREPARED = 1;
    public static final int STATUS_PUBLISHED = 2;
    public static final int STATUS_UNPUBLISHED = 3;
    public static final int STATUS_AHRCHIVED = 4;

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Column(name = "name")
    private String name;
    @Column(name = "client_id")
    private Integer clientId;
    @Column(name = "status")
    private int status;
    @Column(name = "sequence")
    private int sequence;
    @Column(name = "start")
    @Temporal(TemporalType.TIMESTAMP)
    private Date start;
    @Column(name = "finish")
    @Temporal(TemporalType.TIMESTAMP)
    private Date finish;
    @Column(name = "duration")
    private int duration;
    @Column(name = "update_dt")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateDate;

    public AdCampaigns() {
    }

    public AdCampaigns(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getClientId() {
        return clientId;
    }

    public void setClientId(Integer clientId) {
        this.clientId = clientId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
    
    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getFinish() {
        return finish;
    }

    public void setFinish(Date finish) {
        this.finish = finish;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
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
        if (!(object instanceof AdCampaigns)) {
            return false;
        }
        AdCampaigns other = (AdCampaigns) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ee.promobox.entity.AdCampaigns[ id=" + id + " ]";
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }
}

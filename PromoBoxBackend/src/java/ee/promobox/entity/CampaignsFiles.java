/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ee.promobox.entity;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author MaximDorofeev
 */
@Entity
@Table(name = "campaigns_files")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CampaignsFiles.findAll", query = "SELECT c FROM CampaignsFiles c"),
    @NamedQuery(name = "CampaignsFiles.findById", query = "SELECT c FROM CampaignsFiles c WHERE c.id = :id"),
    @NamedQuery(name = "CampaignsFiles.findByAdCampaignsId", query = "SELECT c FROM CampaignsFiles c WHERE c.adCampaignsId = :adCampaignsId"),
    @NamedQuery(name = "CampaignsFiles.findByClientId", query = "SELECT c FROM CampaignsFiles c WHERE c.clientId = :clientId"),
    @NamedQuery(name = "CampaignsFiles.findByFileId", query = "SELECT c FROM CampaignsFiles c WHERE c.fileId = :fileId"),
    @NamedQuery(name = "CampaignsFiles.findByFileType", query = "SELECT c FROM CampaignsFiles c WHERE c.fileType = :fileType"),
    @NamedQuery(name = "CampaignsFiles.findByOrderId", query = "SELECT c FROM CampaignsFiles c WHERE c.orderId = :orderId")})
public class CampaignsFiles implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Column(name = "ad_campaigns_id")
    private Integer adCampaignsId;
    @Column(name = "client_id")
    private Integer clientId;
    @Column(name = "file_id")
    private Integer fileId;
    @Column(name = "file_type")
    private Integer fileType;
    @Column(name = "order_id")
    private Integer orderId;

    public CampaignsFiles() {
    }

    public CampaignsFiles(Integer id) {
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

    public Integer getClientId() {
        return clientId;
    }

    public void setClientId(Integer clientId) {
        this.clientId = clientId;
    }

    public Integer getFileId() {
        return fileId;
    }

    public void setFileId(Integer fileId) {
        this.fileId = fileId;
    }

    public Integer getFileType() {
        return fileType;
    }

    public void setFileType(Integer fileType) {
        this.fileType = fileType;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
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
        if (!(object instanceof CampaignsFiles)) {
            return false;
        }
        CampaignsFiles other = (CampaignsFiles) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ee.promobox.entity.CampaignsFiles[ id=" + id + " ]";
    }
    
}

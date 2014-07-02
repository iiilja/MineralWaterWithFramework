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
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
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
    @NamedQuery(name = "AdCampaigns.findByActive", query = "SELECT a FROM AdCampaigns a WHERE a.active = :active")})
public class AdCampaigns implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Column(name = "name")
    private String name;
    @Lob
    @Column(name = "params")
    private Object params;
    @Column(name = "client_id")
    private Integer clientId;
    @Column(name = "active")
    private Boolean active;

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

    public Object getParams() {
        return params;
    }

    public void setParams(Object params) {
        this.params = params;
    }

    public Integer getClientId() {
        return clientId;
    }

    public void setClientId(Integer clientId) {
        this.clientId = clientId;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
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
    
}

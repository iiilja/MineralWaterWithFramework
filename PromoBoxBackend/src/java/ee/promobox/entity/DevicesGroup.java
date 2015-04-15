/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ee.promobox.entity;

import java.io.Serializable;
import java.util.Collection;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 *
 * @author ilja
 */
@Entity
@Table(name = "devices_group")
@NamedQueries({
    @NamedQuery(name = "DevicesGroup.findAll",                  query = "SELECT d FROM DevicesGroup d"),
    @NamedQuery(name = "DevicesGroup.findById",                 query = "SELECT d FROM DevicesGroup d WHERE d.id = :id"),
    @NamedQuery(name = "DevicesGroup.findByClientId",           query = "SELECT d FROM DevicesGroup d WHERE d.clientId = :clientId"),
    @NamedQuery(name = "DevicesGroup.findByName",               query = "SELECT d FROM DevicesGroup d WHERE d.name = :name")})
public class DevicesGroup implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "client_id")
    private int clientId;
    @Basic(optional = false)
    @Column(name = "name")
    private String name;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "devicesGroup")
    private Collection<DevicesGroupDevices> devicesGroupDevicesCollection;

    public DevicesGroup() {
    }


    public DevicesGroup(int clientId, String name) {
        this.id = 0;
        this.clientId = clientId;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Collection<DevicesGroupDevices> getDevicesGroupDevicesCollection() {
        return devicesGroupDevicesCollection;
    }

    public void setDevicesGroupDevicesCollection(Collection<DevicesGroupDevices> devicesGroupDevicesCollection) {
        this.devicesGroupDevicesCollection = devicesGroupDevicesCollection;
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
        if (!(object instanceof DevicesGroup)) {
            return false;
        }
        DevicesGroup other = (DevicesGroup) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ee.promobox.entity.DevicesGroup[ id=" + id + " ]";
    }
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ee.promobox.entity;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 *
 * @author ilja
 */
@Entity
@Table(name = "devices_group_devices")
@NamedQueries({
    @NamedQuery(name = "DevicesGroupDevices.findAll", query = "SELECT d FROM DevicesGroupDevices d"),
    @NamedQuery(name = "DevicesGroupDevices.findByGroupId", query = "SELECT d FROM DevicesGroupDevices d WHERE d.devicesGroupDevicesPK.groupId = :groupId"),
    @NamedQuery(name = "DevicesGroupDevices.findByDeviceId", query = "SELECT d FROM DevicesGroupDevices d WHERE d.devicesGroupDevicesPK.deviceId = :deviceId")})
public class DevicesGroupDevices implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected DevicesGroupDevicesPK devicesGroupDevicesPK;
    @JoinColumn(name = "group_id", referencedColumnName = "id", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private DevicesGroup devicesGroup;

    public DevicesGroupDevices() {
    }

    public DevicesGroupDevices(DevicesGroupDevicesPK devicesGroupDevicesPK) {
        this.devicesGroupDevicesPK = devicesGroupDevicesPK;
    }

    public DevicesGroupDevices(int groupId, int deviceId) {
        this.devicesGroupDevicesPK = new DevicesGroupDevicesPK(groupId, deviceId);
    }

    public int getGroupId() {
        return devicesGroupDevicesPK.getGroupId();
    }

    public int getDeviceId() {
        return devicesGroupDevicesPK.getDeviceId();
    }

    public DevicesGroup getDevicesGroup() {
        return devicesGroup;
    }

    public void setDevicesGroup(DevicesGroup devicesGroup) {
        this.devicesGroup = devicesGroup;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (devicesGroupDevicesPK != null ? devicesGroupDevicesPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof DevicesGroupDevices)) {
            return false;
        }
        DevicesGroupDevices other = (DevicesGroupDevices) object;
        if ((this.devicesGroupDevicesPK == null && other.devicesGroupDevicesPK != null) || (this.devicesGroupDevicesPK != null && !this.devicesGroupDevicesPK.equals(other.devicesGroupDevicesPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ee.promobox.entity.DevicesGroupDevices[ devicesGroupDevicesPK=" + devicesGroupDevicesPK + " ]";
    }

}

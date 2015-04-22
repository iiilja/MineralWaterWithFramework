/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ee.promobox.entity;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ilja
 */
@Entity
@Table(name = "devices_group_devices")
@NamedQueries({
    @NamedQuery(name = "DevicesGroupDevices.findAll", query = "SELECT d FROM DevicesGroupDevices d"),
    @NamedQuery(name = "DevicesGroupDevices.findByGroupId", query = "SELECT d FROM DevicesGroupDevices d WHERE d.devicesGroupDevicesPK.groupId = :groupId"),
    @NamedQuery(name = "DevicesGroupDevices.findByDeviceId", query = "SELECT d FROM DevicesGroupDevices d WHERE d.devicesGroupDevicesPK.deviceId = :deviceId"),
    @NamedQuery(name = "DevicesGroupDevices.findByDeviceName", query = "SELECT d FROM DevicesGroupDevices d WHERE d.deviceName = :deviceName")})
public class DevicesGroupDevices implements Serializable {
    
    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected DevicesGroupDevicesPK devicesGroupDevicesPK;
    @Basic(optional = false)
    @Column(name = "device_name")
    private String deviceName;

    public DevicesGroupDevices() {
    }

    public DevicesGroupDevices(int groupId, int deviceId , String deviceName) {
        this.devicesGroupDevicesPK = new DevicesGroupDevicesPK(groupId, deviceId);
        this.deviceName = deviceName;
    }

    public int getGroupId() {
        return devicesGroupDevicesPK.getGroupId();
    }

    public int getDeviceId() {
        return devicesGroupDevicesPK.getDeviceId();
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
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

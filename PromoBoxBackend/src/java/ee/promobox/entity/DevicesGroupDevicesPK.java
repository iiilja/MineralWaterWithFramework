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

/**
 *
 * @author ilja
 */
@Embeddable
    public class DevicesGroupDevicesPK implements Serializable {

        @Basic(optional = false)
        @Column(name = "group_id")
        private int groupId;
        @Basic(optional = false)
        @Column(name = "device_id")
        private int deviceId;

        public DevicesGroupDevicesPK() {
        }

        public DevicesGroupDevicesPK(int groupId, int deviceId) {
            this.groupId = groupId;
            this.deviceId = deviceId;
        }

        public int getGroupId() {
            return groupId;
        }

        public void setGroupId(int groupId) {
            this.groupId = groupId;
        }

        public int getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(int deviceId) {
            this.deviceId = deviceId;
        }

        @Override
        public int hashCode() {
            int hash = 0;
            hash += (int) groupId;
            hash += (int) deviceId;
            return hash;
        }

        @Override
        public boolean equals(Object object) {
            // TODO: Warning - this method won't work in the case the id fields are not set
            if (!(object instanceof DevicesGroupDevicesPK)) {
                return false;
            }
            DevicesGroupDevicesPK other = (DevicesGroupDevicesPK) object;
            if (this.groupId != other.groupId) {
                return false;
            }
            if (this.deviceId != other.deviceId) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "ee.promobox.entity.DevicesGroupDevicesPK[ groupId=" + groupId + ", deviceId=" + deviceId + " ]";
        }

    }

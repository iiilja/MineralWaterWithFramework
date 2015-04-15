/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ee.promobox.service;

import ee.promobox.entity.DevicesGroup;
import ee.promobox.entity.DevicesGroupDevices;
import java.util.List;

/**
 *
 * @author ilja
 */
public interface DevicesGroupService {
        
    public void createDeviceGroup(DevicesGroup devicesGroup);
    public List<DevicesGroup> findGroupsByClientId(int clientId);
    public DevicesGroup findGroupByClientAndGroupId(int clientId, int groupId);
    
    public void addDeviceToDeviceGroup(DevicesGroupDevices device);
    public List<DevicesGroupDevices> findDevicesByGroupId(int groupId);
    public void removeDeviceFromDeviceGroup(int deviceId, int groupId);
    
    public void deleteEntity(Object entity);
}

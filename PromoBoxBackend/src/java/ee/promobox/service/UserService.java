/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ee.promobox.service;

import ee.promobox.entity.AdCampaigns;
import ee.promobox.entity.CampaignsFiles;
import ee.promobox.entity.Devices;
import ee.promobox.entity.DevicesCampaigns;
import ee.promobox.entity.Files;
import ee.promobox.entity.Users;
import java.util.List;

/**
 *
 * @author MaximDorofeev
 */
public interface UserService {

    public List<Users> findAllUsers();

    public Users findUserByEmailAndPassword(String email, String password);

    public List<AdCampaigns> findUserAdCompaigns(int clientId);

    public List<Devices> findUserDevieces(int clientId);

    public List<Files> findCampaignFiles(int campgaignId);
    
    public CampaignsFiles findCampaignFile(int fileId, int clientId);
    
    public List<CampaignsFiles> findUsersCampaignFiles(int campaignId, int clientId);
    
    public AdCampaigns findCampaignByIdAndClientId(int id, int clientId);
    
    public void addDevice(Devices devices);
    
    // public AdCampaigns findCampaignByUuid(String uuid);
    
    public Devices findDeviceByUuid(String uuid);
    
    public List<Devices> findAllDevices();
    
    public List<DevicesCampaigns> findDeviceCampaignsByDeviceId(int deviceId);
    
    public DevicesCampaigns findLastUpdatedDeviceCampaign(int deviceId);
    
    public DevicesCampaigns findDeviceCampaignByCampaignId(int deviceId, int campaignId);

    public List<AdCampaigns> findCampaignByDeviceId(int deviceId);
    
    public List<AdCampaigns> findCampaignsArchiveCandidates();
    
    public AdCampaigns findCampaignByCampaignId(int campaignId);
    
    public Files findFileById(int id);

    public void addCampaign(AdCampaigns campaign);

    public void updateCampaign(AdCampaigns campaign);

    public void addFile(Files file);

    public void addCampaignFile(CampaignsFiles file);

    public void updateCampaignFile(CampaignsFiles file);
    public void updateCampaignFileOrder(int fileId, int orderId, int campaignId, int clientId);
    
    public void updateDevice(Devices d);
    
    public void updateDeviceAdCampaign(DevicesCampaigns dc);
    
    public void updateFile(Files file);

    public Devices findDeviceByIdAndClientId(int id, int clientId);
    
    public void addDeviceAdCampaign(DevicesCampaigns dc);
    
    public CampaignsFiles findCampaignFileById(int fileId);
    
    public void deleteDeviceCampaign(int deviceId, int campaignId);
}

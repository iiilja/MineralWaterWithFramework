/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ee.promobox.service;

import ee.promobox.entity.AdCampaigns;
import ee.promobox.entity.CampaignsFiles;
import ee.promobox.entity.Clients;
import ee.promobox.entity.Devices;
import ee.promobox.entity.DevicesCampaigns;
import ee.promobox.entity.DevicesDisplays;
import ee.promobox.entity.ErrorLog;
import ee.promobox.entity.Files;
import ee.promobox.entity.Users;
import ee.promobox.entity.UsersCampaignsPermissions;
import ee.promobox.entity.UsersDevicesPermissions;
import ee.promobox.entity.Versions;

import java.util.List;

/**
 *
 * @author MaximDorofeev
 */
public interface UserService {

    public List<Users> findAllUsers();
    public List<Users> findUsersByClientId(int clientId);

    public Users findUserByEmailAndPassword(String email, String password);
    public Users findUserByEmail(String email, String exclude);
    public Users findUserById(int id);
    
    public Clients findClientById(int clientId);

    public List<AdCampaigns> findUserAdCompaigns(int clientId);
    public List<AdCampaigns> findUserAdCompaigns(int clientId, int userId);

    public List<Devices> findUserDevieces(int clientId);
    public List<Devices> findUserDevieces(int clientId, int userId);
    
    public List<Devices> findDevicesByCampaing(int campaignId);

    public List<CampaignsFiles> findAllFiles();
    
    public List<CampaignsFiles> findCampaignFiles(int campgaignId);
    
    public CampaignsFiles findCampaignFile(int fileId, int clientId);
    
    public List<CampaignsFiles> findUsersCampaignFiles(int campaignId, int clientId);
    
    public CampaignsFiles findFileByIdAndPage(int fileId, int page);
    
    public AdCampaigns findCampaignByIdAndClientId(int campaign, int clientId);
    
    public void addDevice(Devices devices);
    
    public String findDeviceUuid();
    
    // public AdCampaigns findCampaignByUuid(String uuid);
    
    public Devices findDeviceByUuid(String uuid);
    
    public List<Devices> findAllDevices();
    
    public List<DevicesCampaigns> findDeviceCampaignsByDeviceId(int deviceId);
    
    public DevicesCampaigns findLastUpdatedDeviceCampaign(int deviceId);
    
    public DevicesCampaigns findDeviceCampaignByCampaignId(int deviceId, int campaignId);

    public List<AdCampaigns> findCampaignByDeviceId(int deviceId);
    
    public List<CampaignsFiles> findFilesArchiveCandidates();
    
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
    
    public List<Devices> findDevicesByCampaignId(int id, int clientId);
    
    public List<DevicesDisplays> findDevicesDisplays(int deviceId);
    
    public void addDeviceAdCampaign(DevicesCampaigns dc);
    
    public CampaignsFiles findCampaignFileById(int fileId);
    
    public List<CampaignsFiles> findCampaignFileByIds(List<Integer> fileIds);
    
    public void deleteDeviceCampaign(int deviceId, int campaignId);
    
    public void addErrorLog(ErrorLog errorLog);
    
    public void addClient(Clients client);
    public void updateClient(Clients client);
    public void addUser(Users user);
    public void updateUser(Users user);
    
    public void addUsersDevicesPermissions(UsersDevicesPermissions permissions);
    public void updateUsersDevicesPermissions(UsersDevicesPermissions permissions);
    public void deleteUsersDevicesPermissions(int userId, int deviceId);
    public UsersDevicesPermissions findUsersDevicesPermissions(int userId, int deviceId);
    public List<UsersDevicesPermissions> findUsersDevicesPermissionsByClientId(int clientId);
    
    public void addUsersCampaignsPermissions(UsersCampaignsPermissions permissions);
    public void updateUsersCampaignsPermissions(UsersCampaignsPermissions permissions);
    public void deleteUsersCampaignsPermissions(int userId, int campaignId);
    public UsersCampaignsPermissions findUsersCampaignsPermissions(int userId, int campaignId);
    public List<UsersCampaignsPermissions> findUsersCampaignsPermissionsByClientId(int clientId);
    
    public Versions findCurrentVersion();
}

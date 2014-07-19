/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ee.promobox.service;

import ee.promobox.entity.AdCampaigns;
import ee.promobox.entity.CampaignsFiles;
import ee.promobox.entity.Devices;
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
    
    public boolean hasAccess(int id, int clientId);

    public void addCampaign(AdCampaigns campaign);

    public void updateCampaign(AdCampaigns campaign);

    public void addFile(Files file);

    public void addCampaignFile(CampaignsFiles file);

    public void updateCampaignFile(CampaignsFiles file);
}

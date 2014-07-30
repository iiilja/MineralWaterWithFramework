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
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author MaximDorofeev
 */
@Transactional
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private SessionFactory sessionFactory;

    public List<Users> findAllUsers() {
        Session session = sessionFactory.getCurrentSession();
        List<Users> list = session.createQuery("from Users").list();

        return list;
    }

    public Users findUserByEmailAndPassword(String email, String password) {
        Session session = sessionFactory.getCurrentSession();

        Query q = session.createQuery("from Users where email = :email and password = :password");
        q.setParameter("email", email);
        q.setParameter("password", password);

        return (Users) q.uniqueResult();
    }

    public List<AdCampaigns> findUserAdCompaigns(int clientId) {
        Session session = sessionFactory.getCurrentSession();

        Query q = session.createQuery("from AdCampaigns where clientId = :clientId");
        q.setParameter("clientId", clientId);

        return q.list();
    }

    public List<Devices> findUserDevieces(int clientId) {
        Session session = sessionFactory.getCurrentSession();

        Query q = session.createQuery("from Devices where clientId = :clientId");
        q.setParameter("clientId", clientId);

        return q.list();
    }

    public CampaignsFiles findCampaignFile(int fileId, int clientId) {
        Session session = sessionFactory.getCurrentSession();

        Query q = session.createQuery("from CampaignsFiles where fileId  = :fileId and clientId = :clientId");
        q.setParameter("fileId", fileId);
        q.setParameter("clientId", clientId);

        return (CampaignsFiles) q.uniqueResult();

    }

    public List<Files> findCampaignFiles(int campgaignId) {
        Session session = sessionFactory.getCurrentSession();

        Query q = session.createQuery("select f from Files f, CampaignsFiles cf where cf.campgainId = :campaignId and f.id = cf.fileId");
        q.setParameter("campaignId", campgaignId);

        return q.list();
    }

    public List<Files> findUsersCampaignFiles(int campaignId, int clientId) {
        Session session = sessionFactory.getCurrentSession();

        Query q = session.createQuery("SELECT f FROM Files f, CampaignsFiles cf "
                + "WHERE cf.clientId = :clientId AND cf.adCampaignsId = :campaignId AND cf.status = 0 AND f.id = cf.fileId");
        q.setParameter("clientId", clientId);
        q.setParameter("campaignId", campaignId);

        return q.list();
    }

    public AdCampaigns findCampaignByIdAndClientId(int id, int clientId) {
        Session session = sessionFactory.getCurrentSession();

        Query q = session.createQuery("FROM AdCampaigns WHERE id = :id AND clientId = :clientId");
        q.setParameter("id", id);
        q.setParameter("clientId", clientId);

        return (AdCampaigns) q.uniqueResult();
    }

    /*
     public AdCampaigns findCampaignByUuid(String uuid) {
     Session session = sessionFactory.getCurrentSession();
        
     Query q = session.createQuery("SELECT ad FROM AdCampaigns ad, Devices d, DevicesCampaigns dc WHERE d.uuid = :uuid AND d.id = dc.deviceId AND dc.adCampaignsId = ad.id");
     q.setParameter("uuid", uuid);
        
     return (AdCampaigns) q.uniqueResult();
     }
     */
    public Devices findDeviceByUuid(String uuid) {
        Session session = sessionFactory.getCurrentSession();

        Query q = session.getNamedQuery("Devices.findByUuid").setString("uuid", uuid);
        return (Devices) q.uniqueResult();
    }

    public DevicesCampaigns findDeviceCampaignByDeviceId(int deviceId) {
        Session session = sessionFactory.getCurrentSession();

        Query q = session.getNamedQuery("DevicesCampaigns.findByDeviceId").setInteger("deviceId", deviceId);
        return (DevicesCampaigns) q.uniqueResult();
    }

    public AdCampaigns findCampaignByCampaignId(int campaignId) {
        Session session = sessionFactory.getCurrentSession();
        
        Query q = session.getNamedQuery("AdCampaigns.findById").setInteger("id", campaignId);
        
        return (AdCampaigns) q.uniqueResult();
    }
    
    public Files findFileById(int id) {
        Session session = sessionFactory.getCurrentSession();
        
        Query q = session.getNamedQuery("Files.findById").setInteger("id", id);
        
        return (Files) q.uniqueResult();
    }

    public void addCampaign(AdCampaigns campaign) {
        Session session = sessionFactory.getCurrentSession();
        session.save(campaign);
        session.flush();
    }

    public void updateCampaign(AdCampaigns campaign) {
        Session session = sessionFactory.getCurrentSession();
        session.update(campaign);
        session.flush();
    }

    public void addFile(Files file) {
        Session session = sessionFactory.getCurrentSession();
        session.save(file);
        session.flush();
    }

    public void addCampaignFile(CampaignsFiles file) {
        Session session = sessionFactory.getCurrentSession();
        session.save(file);
        session.flush();
    }

    public void updateCampaignFile(CampaignsFiles file) {
        Session session = sessionFactory.getCurrentSession();
        session.update(file);
        session.flush();
    }
    
    public void updateDevice(Devices d) {
        Session session = sessionFactory.getCurrentSession();
        session.update(d);
        session.flush();
    }
    
    public void updateDeviceAdCampaign(DevicesCampaigns dc) {
        Session session = sessionFactory.getCurrentSession();
        session.update(dc);
        session.flush();
    }
    
    public void updateFile(Files file) {
        Session session = sessionFactory.getCurrentSession();
        session.update(file);
        session.flush();
    }

}

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

    public List<Files> findCampaignFiles(int campgaignId) {
        Session session = sessionFactory.getCurrentSession();

        Query q = session.createQuery("select f from Files f, CampaignsFiles cf where cf.campgainId = :campaignId and f.id = cf.fileId");

        q.setParameter("campaignId", campgaignId);

        return q.list();
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
}

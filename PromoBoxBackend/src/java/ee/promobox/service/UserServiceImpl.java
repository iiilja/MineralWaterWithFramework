/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ee.promobox.service;

import ee.promobox.entity.AdCampaigns;
import ee.promobox.entity.Devices;
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

    public List<AdCampaigns> findUserAdCompaigns(int userId) {
        Session session = sessionFactory.getCurrentSession();

        Query q = session.createQuery("from AdCampaings where userId = :userId");

        q.setParameter("userId", userId);

        return q.list();
    }
    
    public List<Devices> findUserDevieces(int userId) {
        Session session = sessionFactory.getCurrentSession();

        Query q = session.createQuery("from Devices where userId = :userId");

        q.setParameter("userId", userId);

        return q.list();
    }
}

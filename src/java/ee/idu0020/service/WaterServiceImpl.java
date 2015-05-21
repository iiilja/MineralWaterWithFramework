/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ee.idu0020.service;

import ee.idu0020.entity.Water;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Dmitri
 */
@Transactional
@Service
public class WaterServiceImpl implements WaterService{
    
    @Autowired
    private SessionFactory sessionFactory;
    
    
    @Override
    public List<Water> findAll(){
        Session session = sessionFactory.getCurrentSession();
        return session.getNamedQuery("Water.findAll").list();
    }
    
    @Override
    public Water findById(int id){
        Session session = sessionFactory.getCurrentSession();
        return (Water) session.getNamedQuery("Water.findById").setParameter("id", id).uniqueResult();
    }
    
    @Override
    public void update(Water shirt){
        Session session = sessionFactory.getCurrentSession();
        session.update(shirt);
        session.flush();
    }
    
    @Override
    public void delete(Water shirt){
        Session session = sessionFactory.getCurrentSession();
        session.delete(shirt);
        session.flush();
    }
}

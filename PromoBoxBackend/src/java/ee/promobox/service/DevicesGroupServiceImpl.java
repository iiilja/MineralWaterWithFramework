package ee.promobox.service;

import ee.promobox.entity.DevicesGroup;
import ee.promobox.entity.DevicesGroupDevices;
import java.util.List;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author ilja
 */
@Transactional
public class DevicesGroupServiceImpl implements DevicesGroupService{
    
    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public void createDeviceGroup(DevicesGroup devicesGroup) {
        org.hibernate.Session session = sessionFactory.getCurrentSession();
        session.save(devicesGroup);
        session.flush();
    }

    @Override
    public void addDeviceToDeviceGroup(DevicesGroupDevices device) {
        org.hibernate.Session session = sessionFactory.getCurrentSession();
        session.save(device);
        session.flush();
    }

    @Override
    public void removeDeviceFromDeviceGroup(int deviceId, int groupId) {
        org.hibernate.Session session = sessionFactory.getCurrentSession();
        Query q = session.createQuery("DELETE FROM DevicesGroupDevices "
                + "WHERE devicesGroupDevicesPK.groupId = :groupId AND devicesGroupDevicesPK.deviceId = :deviceId");
        q.setParameter("groupId", groupId);
        q.setParameter("deviceId", deviceId);
        q.executeUpdate();
    }

    @Override
    public List<DevicesGroup> findGroupsByClientId(int clientId) {
        org.hibernate.Session session = sessionFactory.getCurrentSession();
        Query q = session.getNamedQuery("DevicesGroup.findByClientId");
        q.setParameter("clientId", clientId);
        return q.list();
    }
    
    
    @Override
    public DevicesGroup findGroupByClientAndGroupId(int clientId, int groupId) {
        org.hibernate.Session session = sessionFactory.getCurrentSession();
        Query q = session.createQuery("FROM DevicesGroup d WHERE d.clientId = :clientId AND d.id = :id");
        q.setParameter("id", groupId);
        q.setParameter("clientId", clientId);
        return (DevicesGroup) q.uniqueResult();
    }

    @Override
    public List<DevicesGroupDevices> findDevicesByGroupId(int groupId) {
        org.hibernate.Session session = sessionFactory.getCurrentSession();
        Query q = session.getNamedQuery("DevicesGroupDevices.findByGroupId");
        q.setParameter("groupId", groupId);
        return q.list();
    }

    @Override
    public void deleteEntity(Object entity) {
        sessionFactory.getCurrentSession().delete(entity);
        sessionFactory.getCurrentSession().flush();
    }

    
}

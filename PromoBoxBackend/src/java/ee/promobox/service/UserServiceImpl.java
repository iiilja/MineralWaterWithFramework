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

import java.util.Calendar;
import java.util.GregorianCalendar;
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
    
    @Override
    public List<Users> findUsersByClientId(int clientId) {
    	Session session = sessionFactory.getCurrentSession();

        Query q = session.createQuery("from Users where clientId = :clientId AND active IS TRUE");
        q.setParameter("clientId", clientId);

        return q.list();
    }

    public Users findUserByEmailAndPassword(String email, String password) {
        Session session = sessionFactory.getCurrentSession();

        Query q = session.createQuery("from Users where lower(email) = lower(:email) and password = :password AND active IS TRUE");
        q.setParameter("email", email);
        q.setParameter("password", password);

        return (Users) q.uniqueResult();
    }
    
    @Override
    public Users findUserByEmail(String email, String exclude) {
    	Session session = sessionFactory.getCurrentSession();

        Query q = session.createQuery("from Users where lower(email) = lower(:email) AND email <> :excludeEmail AND active IS TRUE");
        q.setParameter("email", email);
        q.setParameter("excludeEmail", exclude);

        return (Users) q.uniqueResult();
    }
    
    @Override
    public Users findUserById(int id) {
    	Session session = sessionFactory.getCurrentSession();

        Query q = session.createQuery("from Users where id = :id");
        q.setParameter("id", id);

        return (Users) q.uniqueResult();
    }

    @Override
    public Clients findClientById(int clientId) {
        Session session = sessionFactory.getCurrentSession();

        Query q = session.getNamedQuery("Clients.findById");
        q.setParameter("id", clientId);

        return (Clients) q.uniqueResult();
    }
    
    

    public List<AdCampaigns> findUserAdCompaigns(int clientId) {
        Session session = sessionFactory.getCurrentSession();

        Query q = session.createQuery("from AdCampaigns where clientId = :clientId AND status > 0 AND status < 4 Order by createdDate DESC, id DESC");
        q.setParameter("clientId", clientId);

        return q.list();
    }
    
    public List<AdCampaigns> findUserAdCompaigns(int clientId, int userId) {
        Session session = sessionFactory.getCurrentSession();

        Query q = session.createQuery("from AdCampaigns c where c.clientId = :clientId AND c.status > 0 AND c.status < 4 AND "
        		+ " EXISTS (SELECT 1 FROM UsersCampaignsPermissions p WHERE p.campaignId = c.id AND p.userId = :userId AND p.permissionRead IS TRUE) "
        		+ " ORDER BY c.createdDate DESC, c.id DESC");
        q.setParameter("clientId", clientId);
        q.setParameter("userId", userId);

        return q.list();
    }

    public List<Devices> findUserDevieces(int clientId) {
        Session session = sessionFactory.getCurrentSession();

        Query q = session.createQuery("from Devices where clientId = :clientId AND status < 4 "
        		+ " ORDER BY createdDt DESC, id DESC");
        q.setParameter("clientId", clientId);

        return q.list();
    }
    
    public List<Devices> findUserDevieces(int clientId, int userId) {
        Session session = sessionFactory.getCurrentSession();

        Query q = session.createQuery("from Devices d where d.clientId = :clientId AND d.status < 4 AND "
        		+ " EXISTS (SELECT 1 FROM UsersDevicesPermissions p WHERE p.userId = :userId AND p.deviceId = d.id AND p.permissionRead IS TRUE) "
        		+ " ORDER BY d.createdDt DESC, d.id DESC");
        q.setParameter("clientId", clientId);
        q.setParameter("userId", userId);

        return q.list();
    }

    @Override
	public List<Devices> findDevicesByCampaing(int campaignId) {
    	Session session = sessionFactory.getCurrentSession();

        Query q = session.createQuery("SELECT d FROM Devices d, DevicesCampaigns dc  WHERE d.id = dc.deviceId "
        		+ " AND dc.adCampaignsId = :campaignId"
        		+ " AND d.status != :statusArchived");
        q.setParameter("campaignId", campaignId);
        q.setParameter("statusArchived", Devices.STATUS_AHRCHIVED);

        return q.list();
	}

	public CampaignsFiles findCampaignFile(int id, int clientId) {
        Session session = sessionFactory.getCurrentSession();

        Query q = session.createQuery("from CampaignsFiles where id  = :id and clientId = :clientId");
        q.setParameter("id", id);
        q.setParameter("clientId", clientId);

        return (CampaignsFiles) q.uniqueResult();

    }
    
    public CampaignsFiles findCampaignFileById(int id) {
        Session session = sessionFactory.getCurrentSession();

        Query q = session.createQuery("from CampaignsFiles where id  = :id");
        q.setParameter("id", id);

        return (CampaignsFiles) q.uniqueResult();

    }
    
    public List<CampaignsFiles> findCampaignFileByIds(List<Integer> ids) {
        Session session = sessionFactory.getCurrentSession();

        Query q = session.createQuery("from CampaignsFiles where id  IN (:ids)");
        q.setParameterList("ids", ids);

        return q.list();

    }
    
    

    @Override
	public List<CampaignsFiles> findAllFiles() {
        Session session = sessionFactory.getCurrentSession();

        Query q = session.createQuery("FROM CampaignsFiles");

        return q.list();
	}
    
    

	@Override
	public CampaignsFiles findFileByIdAndPage(int fileId, int page) {
		Session session = sessionFactory.getCurrentSession();

        Query q = session.createQuery("SELECT cf FROM CampaignsFiles cf WHERE cf.page = :page AND cf.fileId = :fileId");
        q.setParameter("fileId", fileId);
        q.setParameter("page", page);
        q.setMaxResults(1);

        return (CampaignsFiles) q.uniqueResult();
	}

	public List<CampaignsFiles> findCampaignFiles(int campaignId) {
        Session session = sessionFactory.getCurrentSession();

        Query q = session.createQuery("select cf from Files f, CampaignsFiles cf where cf.adCampaignsId = :campaignId and f.id = cf.fileId");
        q.setParameter("campaignId", campaignId);

        return q.list();
    }

    public List<CampaignsFiles> findUsersCampaignFiles(int campaignId, int clientId) {
        Session session = sessionFactory.getCurrentSession();

        Query q = session.createQuery("SELECT cf FROM CampaignsFiles cf "
                + "WHERE cf.clientId = :clientId AND cf.adCampaignsId = :campaignId AND cf.status != :status "
                + "ORDER BY orderId DESC");
        
        q.setParameter("clientId", clientId);
        q.setParameter("status", CampaignsFiles.STATUS_ARCHIVED);
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

    @Override
    public List<Devices> findAllDevices() {
        Session session = sessionFactory.getCurrentSession();
    
        Query q = session.getNamedQuery("Devices.findAll");
        
        return q.list();
    }
    
    

    public DevicesCampaigns findDeviceCampaignByCampaignId(int deviceId, int campaignId) {
        Session session = sessionFactory.getCurrentSession();

        Query q = session.getNamedQuery("DevicesCampaigns.findByAdCampaignsIdAndDeviceId")
                .setInteger("deviceId", deviceId)
                .setInteger("adCampaignsId", campaignId);
        return (DevicesCampaigns) q.uniqueResult();
    }
    
    public DevicesCampaigns findLastUpdatedDeviceCampaign(int deviceId) {
        Session session = sessionFactory.getCurrentSession();

        Query q = session.createQuery("FROM DevicesCampaigns WHERE deviceId = :deviceId ORDER BY updatedDt DESC")
                .setInteger("deviceId", deviceId)
                .setMaxResults(1);
        
        return (DevicesCampaigns) q.uniqueResult();
    }
    
    
    public List<DevicesCampaigns> findDeviceCampaignsByDeviceId(int deviceId) {
        Session session = sessionFactory.getCurrentSession();

        Query q = session.getNamedQuery("DevicesCampaigns.findByDeviceId")
                .setInteger("deviceId", deviceId);
        return  q.list();
    }

    public List<AdCampaigns> findCampaignByDeviceId(int deviceId) {
        Session session = sessionFactory.getCurrentSession();

        Query q = session.createQuery("select ad from AdCampaigns ad, DevicesCampaigns d where d.deviceId = :deviceId and ad.id = d.adCampaignsId")
                .setInteger("deviceId", deviceId);

        return q.list();
    }

    @Override
    public List<CampaignsFiles> findFilesArchiveCandidates() {
        Session session = sessionFactory.getCurrentSession();
        
        //Calendar cal = GregorianCalendar.getInstance();
        //cal.add(Calendar.DAY_OF_YEAR, -2);
        
        Query q = session.createQuery("FROM CampaignsFiles cf WHERE cf.status = :statusArchived")
                .setInteger("statusArchived", CampaignsFiles.STATUS_ARCHIVED);
        
        return q.list();
    }
    
    

    public AdCampaigns findCampaignByCampaignId(int campaignId) {
        Session session = sessionFactory.getCurrentSession();
        
        Query q = session.getNamedQuery("AdCampaigns.findById")
                .setInteger("id", campaignId);
        
        return (AdCampaigns) q.uniqueResult();
    }
    
    public Files findFileById(int id) {
        Session session = sessionFactory.getCurrentSession();
        
        Query q = session.getNamedQuery("Files.findById")
                .setInteger("id", id);
        
        return (Files) q.uniqueResult();
    }
    
    @Override
	public String findDeviceUuid() {
    	Session session = sessionFactory.getCurrentSession();
    	
    	String devUuid = "substr(CAST(uuid_in(CAST(md5(CAST(now() + CAST(s || ' minute' AS interval) AS text)) AS cstring)) AS TEXT), 0, 5)";
        
    	Query q = session.createSQLQuery(""
    			+ "SELECT " + devUuid +  " AS uuid "
    			+ "FROM generate_series(1, 10) AS s "
    			+ "WHERE NOT EXISTS (SELECT 1 FROM devices d WHERE d.uuid = " + devUuid + ")");
    	
    	List<String> uuids = q.list();
    	
    	if (!uuids.isEmpty()) {
    		return uuids.get(0);
    	}
    	
		return null;
	}

	public void addCampaign(AdCampaigns campaign) {
        Session session = sessionFactory.getCurrentSession();
        session.save(campaign);
        session.flush();
    }
    
    public void addDevice(Devices devices) {
        Session session = sessionFactory.getCurrentSession();
        session.save(devices);
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
    
    public void updateCampaignFileOrder(int fileId, int orderId, int campaignId, int clientId) {
        Session session = sessionFactory.getCurrentSession();

        Query q = session.createQuery("UPDATE CampaignsFiles SET orderId = :orderId "
                + "WHERE id = :id AND adCampaignsId = :campaignId AND clientId = :clientId");

        q.setParameter("id", fileId);
        q.setParameter("orderId", orderId);
        q.setParameter("campaignId", campaignId);
        q.setParameter("clientId", clientId);
        
        q.executeUpdate();
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
    
    public void addDeviceAdCampaign(DevicesCampaigns dc) {
        Session session = sessionFactory.getCurrentSession();
        session.save(dc);
        session.flush();
    }
    
    public void updateFile(Files file) {
        Session session = sessionFactory.getCurrentSession();
        session.update(file);
        session.flush();
    }

    @Override
    public Devices findDeviceByIdAndClientId(int id, int clientId) {
        Session session = sessionFactory.getCurrentSession();

        Query q = session.createQuery("FROM Devices WHERE id = :id AND clientId = :clientId");

        q.setParameter("id", id);
        q.setParameter("clientId", clientId);

        return (Devices) q.uniqueResult();
    }

    @Override
    public List<Devices> findDevicesByCampaignId(int campignId, int clientId) {
        Session session = sessionFactory.getCurrentSession();

        Query q = session.createQuery("FROM Devices WHERE currentCampaignId = :campignId AND clientId = :clientId");

        q.setParameter("campignId", campignId);
        q.setParameter("clientId", clientId);

        return q.list();
    }
    
    
    
    @Override
    public void deleteDeviceCampaign(int deviceId, int campaignId) {
        Session session = sessionFactory.getCurrentSession();

        Query q = session.createQuery("DELETE FROM DevicesCampaigns WHERE adCampaignsId = :campaignId AND deviceId = :deviceId");

        q.setParameter("deviceId", deviceId);
        q.setParameter("campaignId", campaignId);

        q.executeUpdate();
    }
    
    
    
    @Override
	public List<DevicesDisplays> findDevicesDisplays(int deviceId) {
    	Session session = sessionFactory.getCurrentSession();

    	Query q = session.createQuery("FROM DevicesDisplays WHERE deviceId = :deviceId");

        q.setParameter("deviceId", deviceId);
    	
		return q.list();
	}

	@Override
	public void addErrorLog(ErrorLog errorLog) {
		Session session = sessionFactory.getCurrentSession();
        session.save(errorLog);
        session.flush();
	}
    
    @Override
	public void addClient(Clients client) {
		Session session = sessionFactory.getCurrentSession();
        session.save(client);
        session.flush();
	}
    
    @Override
	public void updateClient(Clients client) {
		Session session = sessionFactory.getCurrentSession();
        session.update(client);
        session.flush();
	}
    
    @Override
   	public void addUser(Users user) {
   		Session session = sessionFactory.getCurrentSession();
   		session.save(user);
   		session.flush();
   	}
    
    @Override
   	public void updateUser(Users user) {
    	Session session = sessionFactory.getCurrentSession();
    	session.update(user);
    	session.flush();
   	}

	@Override
	public void addUsersDevicesPermissions(UsersDevicesPermissions permissions) {
		Session session = sessionFactory.getCurrentSession();
   		session.save(permissions);
   		session.flush();
		
	}

	@Override
	public void updateUsersDevicesPermissions(
			UsersDevicesPermissions permissions) {
		Session session = sessionFactory.getCurrentSession();
   		session.update(permissions);
   		session.flush();
		
	}
	
	
	@Override
	public void deleteUsersDevicesPermissions(int userId,
			int deviceId) {
		Session session = sessionFactory.getCurrentSession();

    	Query q = session.createQuery("DELETE FROM UsersDevicesPermissions WHERE deviceId = :deviceId AND userId = :userId");

    	q.setParameter("userId", userId);
        q.setParameter("deviceId", deviceId);
    	
		q.executeUpdate();
	}

	@Override
	public UsersDevicesPermissions findUsersDevicesPermissions(int userId, int deviceId) {
		Session session = sessionFactory.getCurrentSession();

    	Query q = session.createQuery("FROM UsersDevicesPermissions WHERE deviceId = :deviceId AND userId = :userId");

    	q.setParameter("userId", userId);
        q.setParameter("deviceId", deviceId);
    	
		return (UsersDevicesPermissions) q.uniqueResult();
	}
	
	@Override
	public List<UsersDevicesPermissions> findUsersDevicesPermissionsByClientId(int clientId) {
		Session session = sessionFactory.getCurrentSession();

    	Query q = session.createQuery("FROM UsersDevicesPermissions WHERE clientId = :clientId");

    	q.setParameter("clientId", clientId);
    	
		return q.list();
	}

	@Override
	public void addUsersCampaignsPermissions(
			UsersCampaignsPermissions permissions) {
		Session session = sessionFactory.getCurrentSession();
   		session.save(permissions);
   		session.flush();
		
	}

	@Override
	public void updateUsersCampaignsPermissions(
			UsersCampaignsPermissions permissions) {
		Session session = sessionFactory.getCurrentSession();
   		session.update(permissions);
   		session.flush();	
	}
	
	@Override
	public void deleteUsersCampaignsPermissions(
			int userId, int campaignId) {
		Session session = sessionFactory.getCurrentSession();
		
		Query q = session.createQuery("DELETE FROM UsersCampaignsPermissions WHERE campaignId = :campaignId AND userId = :userId");

    	q.setParameter("userId", userId);
        q.setParameter("campaignId", campaignId);
    	
		q.executeUpdate();
	}

	@Override
	public UsersCampaignsPermissions findUsersCampaignsPermissions(int userId,
			int campaignId) {
		Session session = sessionFactory.getCurrentSession();

    	Query q = session.createQuery("FROM UsersCampaignsPermissions WHERE campaignId = :campaignId AND userId = :userId");

    	q.setParameter("userId", userId);
        q.setParameter("campaignId", campaignId);
    	
		return (UsersCampaignsPermissions) q.uniqueResult();
	}
	
	@Override
	public List<UsersCampaignsPermissions> findUsersCampaignsPermissionsByClientId(int clientId) {
		Session session = sessionFactory.getCurrentSession();

    	Query q = session.createQuery("FROM UsersCampaignsPermissions WHERE clientId = :clientId");

    	q.setParameter("clientId", clientId);
    	
		return q.list();
	}

	@Override
	public Versions findCurrentVersion() {
		Session session = sessionFactory.getCurrentSession();

    	Query q = session.createQuery("FROM Versions WHERE isCurrent IS TRUE ORDER BY versionDt DESC");
    	q.setMaxResults(1);
    	
    	return (Versions) q.uniqueResult();
    	
	}

    
    
}

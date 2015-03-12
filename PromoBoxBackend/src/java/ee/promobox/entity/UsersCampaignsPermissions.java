package ee.promobox.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name = "users_campaigns_permissions")
@XmlRootElement
public class UsersCampaignsPermissions implements Serializable, Permissions {
	
	private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    
    @Column(name = "user_id")
    private Integer userId;
    
    @Column(name = "client_id")
    private Integer clientId;
    
    @Column(name = "campaign_id")
    private Integer campaignId;
    
    @Column(name = "permission_read", nullable=false)
    private boolean permissionRead;
    
    @Column(name = "permission_write", nullable=false)
    private boolean permissionWrite;
    
    @Column(name = "created_dt")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDt;
    
    @Column(name = "updated_dt")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedDt;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public Integer getClientId() {
		return clientId;
	}

	public void setClientId(Integer clientId) {
		this.clientId = clientId;
	}

	public Integer getCampaignId() {
		return campaignId;
	}

	public void setCampaignId(Integer campaignId) {
		this.campaignId = campaignId;
	}

	public boolean isPermissionRead() {
		return permissionRead;
	}

	public void setPermissionRead(boolean permissionRead) {
		this.permissionRead = permissionRead;
	}

	public boolean isPermissionWrite() {
		return permissionWrite;
	}

	public void setPermissionWrite(boolean permissionWrite) {
		this.permissionWrite = permissionWrite;
	}

	public Date getCreatedDt() {
		return createdDt;
	}

	public void setCreatedDt(Date createdDt) {
		this.createdDt = createdDt;
	}

	public Date getUpdatedDt() {
		return updatedDt;
	}

	public void setUpdatedDt(Date updatedDt) {
		this.updatedDt = updatedDt;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
    
    

}

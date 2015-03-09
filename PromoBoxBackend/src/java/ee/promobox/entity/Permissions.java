package ee.promobox.entity;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

public interface Permissions {
	

	public Integer getUserId();
	public boolean isPermissionRead();
	public boolean isPermissionWrite();
}

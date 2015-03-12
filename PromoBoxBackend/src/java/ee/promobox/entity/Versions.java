package ee.promobox.entity;

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
@Table(name = "versions")
@XmlRootElement
public class Versions {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable =  false)
    private Integer id;
	
	@Column(name="version", nullable = false)
	private String version;
	
	@Column(name = "version_dt", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date versionDt;
	
	@Column(name = "is_current", nullable = false)
	private boolean isCurrent;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public Date getVersionDt() {
		return versionDt;
	}

	public void setVersionDt(Date versionDt) {
		this.versionDt = versionDt;
	}

	public boolean isCurrent() {
		return isCurrent;
	}

	public void setCurrent(boolean isCurrent) {
		this.isCurrent = isCurrent;
	}
	
	
}

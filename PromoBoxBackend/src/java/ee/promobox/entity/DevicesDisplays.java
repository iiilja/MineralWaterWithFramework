package ee.promobox.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name = "devices_displays")
@XmlRootElement
public class DevicesDisplays implements Serializable {

	@Id
	@Column(name = "display_id", nullable = false)
	private int displayId;
	
	@Id
	@Column(name = "device_id", nullable = false)
	private int deviceId;
	
	@Column(name = "point1")
	private String point1;
	
	@Column(name = "point2")
	private String point2;
	
	@Column(name = "point3")
	private String point3;
	
	@Column(name = "point4")
	private String point4;

	public int getDisplayId() {
		return displayId;
	}

	public void setDisplayId(int displayId) {
		this.displayId = displayId;
	}

	public int getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(int deviceId) {
		this.deviceId = deviceId;
	}

	public String getPoint1() {
		return point1;
	}

	public void setPoint1(String point1) {
		this.point1 = point1;
	}

	public String getPoint2() {
		return point2;
	}

	public void setPoint2(String point2) {
		this.point2 = point2;
	}

	public String getPoint3() {
		return point3;
	}

	public void setPoint3(String point3) {
		this.point3 = point3;
	}

	public String getPoint4() {
		return point4;
	}

	public void setPoint4(String point4) {
		this.point4 = point4;
	}
	
}

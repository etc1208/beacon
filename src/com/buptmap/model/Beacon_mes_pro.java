package com.buptmap.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="beacon_mes_pro_info")

public class Beacon_mes_pro {
	private String mac_id;
	private String uuid;
	private String major;
	private String minor;
	private String mes_id;
	private String mes_title;
	private String mes_content;
	private String mes_status;
	private String project_id;
	private String pro_title;
	private String staff_id;
	
	@Id
	public String getMac_id() {
		return mac_id;
	}
	public void setMac_id(String mac_id) {
		this.mac_id = mac_id;
	}
	public String getMes_status() {
		return mes_status;
	}
	public void setMes_status(String mes_status) {
		this.mes_status = mes_status;
	}
	public String getProject_id() {
		return project_id;
	}
	public void setProject_id(String project_id) {
		this.project_id = project_id;
	}
	public String getMinor() {
		return minor;
	}
	public void setMinor(String minor) {
		this.minor = minor;
	}
	
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public String getMajor() {
		return major;
	}
	public void setMajor(String major) {
		this.major = major;
	}
	public String getMes_id() {
		return mes_id;
	}
	public void setMes_id(String mes_id) {
		this.mes_id = mes_id;
	}
	public String getMes_title() {
		return mes_title;
	}
	public void setMes_title(String mes_title) {
		this.mes_title = mes_title;
	}
	public String getMes_content() {
		return mes_content;
	}
	public void setMes_content(String mes_content) {
		this.mes_content = mes_content;
	}
	public String getPro_title() {
		return pro_title;
	}
	public void setPro_title(String pro_title) {
		this.pro_title = pro_title;
	}
	public String getStaff_id() {
		return staff_id;
	}
	public void setStaff_id(String staff_id) {
		this.staff_id = staff_id;
	}
	
	
}

package com.buptmap.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="tools_version")
public class ToolsVersion {
	private int id;
	private String identity;
	private int version;
	private String name;
	private String downloadAddr;
	private String otherInfo;
	
	@Id
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getIdentity() {
		return identity;
	}
	public void setIdentity(String identity) {
		this.identity = identity;
	}
	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDownloadAddr() {
		return downloadAddr;
	}
	public void setDownloadAddr(String downloadAddr) {
		this.downloadAddr = downloadAddr;
	}
	public String getOtherInfo() {
		return otherInfo;
	}
	public void setOtherInfo(String otherInfo) {
		this.otherInfo = otherInfo;
	}
	@Override
	public String toString() {
		return "ToolsVersion [id=" + id + ", identity=" + identity
				+ ", version=" + version + ", name=" + name + ", downloadAddr="
				+ downloadAddr + ", otherInfo=" + otherInfo + "]";
	}
 
}

package com.buptmap.DAO;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Resource;

import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;

import com.buptmap.model.Beacon;
import com.buptmap.model.Loginrecord;
import com.buptmap.model.Project;
import com.buptmap.model.Staff;
import com.buptmap.model.Vdev_mes_bind;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


@Component("beaconapiDao")
public class BeaconapiDAO {
	//1已配置未部署，2已部署, 3测试，4回收，5预部署
	/*public final static HashMap statusMap = new HashMap() {{    
		put("1", "已配置");    
		put("2", "已部署");    
		put("3", "测试");  
		put("4", "回收");  
		put("5", "预部署");  
		  
	}};*/
	private HibernateTemplate hibernateTemplate = null;
	private JSONArray jsonArray = null;
	private JSONObject jsonObject = null;
	
	public Map<String , Object> getSecretKey(String user_id,String pwd){
		Map<String , Object> map = new HashMap<String, Object>();
		List<Staff> result = new ArrayList<Staff>();
		result = hibernateTemplate.find("from Staff s where s.staff_id = '" + user_id + "'");
		
		try {
			if (result.size() == 1) {
				Staff staff = result.get(0);
				//System.out.println("-----数据库中密码："+staff.getPwd()+"---用户输入密码："+pwd+"--------------");
				if (staff.getPwd().equals(pwd)) {

					String key = UUID.randomUUID().toString();
					map.put("success", true);
					map.put("message", "生成秘钥成功");
					map.put("key", key);
					Loginrecord temprecord = new Loginrecord();
					Date now = new Date();
					String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(now.getTime() + 30 * 60 * 1000));
					temprecord.setTemp_id(key);
					temprecord.setTime(time);
					temprecord.setStaff_id(user_id);
					hibernateTemplate.save(temprecord);
				} else {
					map.put("success", false);
					map.put("message", "用户名或者密码错误");
				}
			} else if (result.size() == 0) {
				map.put("success", false);
				map.put("message", "用户名不存在");
			}
		} catch (Exception e) {
			map.put("success", false);
			map.put("message", e.getMessage());
			e.printStackTrace();
		}
		return map;
	}
	
	public JSONObject importEquipExcel(String filepath,String user_id){
		JSONObject resultObject = new JSONObject();
		List<Beacon> beaconList = new ArrayList<Beacon>();
		int insertNumber = 0;
		int updateNumber = 0;
		String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		try {
			Workbook book = Workbook.getWorkbook(new File(filepath));  
			Sheet sheet = book.getSheet(0);  
			int cols = sheet.getColumns();
			System.out.println("excel列数为："+cols);
			if(cols == 20){
				if(sheet.findCell("mac_id")!=null&&sheet.findCell("uuid")!=null&&sheet.findCell("major")!=null&&sheet.findCell("minor")!=null&&sheet.findCell("address")!=null&&sheet.findCell("address_type")!=null&&sheet.findCell("building")!=null&&sheet.findCell("floor")!=null&&sheet.findCell("coord_x")!=null&&sheet.findCell("coord_y")!=null&&sheet.findCell("coverage")!=null&&sheet.findCell("power")!=null&&sheet.findCell("frequency")!=null&&sheet.findCell("type")!=null&&sheet.findCell("firm")!=null&&sheet.findCell("create_time")!=null&&sheet.findCell("create_id")!=null&&sheet.findCell("last_modify_time")!=null&&sheet.findCell("status")!=null&&sheet.findCell("last_modify_id")!=null){
					for (int i = 1; i < sheet.getRows(); i++) {
			 			String mac_id = sheet.getCell(0,i).getContents()!=""?addColon(sheet.getCell(0,i).getContents()):"";
			 			String uuid = sheet.getCell(1,i).getContents()!=""?sheet.getCell(1,i).getContents():"";
			 			String major = sheet.getCell(2,i).getContents()!=""?sheet.getCell(2,i).getContents():"";
			 			String minor = sheet.getCell(3,i).getContents()!=""?sheet.getCell(3,i).getContents():"";
			 			String address = sheet.getCell(4,i).getContents()!=""?sheet.getCell(4,i).getContents():"";
			 			String address_type = sheet.getCell(5,i).getContents()!=""?sheet.getCell(5,i).getContents():"";
			 			String building = sheet.getCell(6,i).getContents()!=""?sheet.getCell(6,i).getContents():"";
			 			String floor = sheet.getCell(7,i).getContents()!=""?sheet.getCell(7,i).getContents():"";
			 			String coord_x = sheet.getCell(8,i).getContents()!=""?sheet.getCell(8,i).getContents():"";
			 			String coord_y = sheet.getCell(9,i).getContents()!=""?sheet.getCell(9,i).getContents():"";
			 			
			 			String power = sheet.getCell(11,i).getContents()!=""?formatPower(sheet.getCell(11,i).getContents()):"";
			 			String frequency = sheet.getCell(12,i).getContents()!=""?formatFrequency(sheet.getCell(12,i).getContents()):"";
			 			String type = sheet.getCell(13,i).getContents()!=""?sheet.getCell(13,i).getContents():"";
			 			String firm = sheet.getCell(14,i).getContents()!=""?sheet.getCell(14,i).getContents():"";
			 			
			 			String coverage = getCoverage(power, type, firm);
			 			
			 			String create_time = sheet.getCell(15,i).getContents()!=""?sheet.getCell(15,i).getContents():time;
			 			String create_id = sheet.getCell(16,i).getContents()!=""?sheet.getCell(16,i).getContents():user_id;
			 			String last_modify_time = sheet.getCell(17,i).getContents()!=""?sheet.getCell(17,i).getContents():time;
			 			String status = "已配置";
			 			String last_modify_id = create_id;
			 			
			 			beaconList = hibernateTemplate.find("from Beacon b where b.mac_id='" + mac_id + "'");					
						if (beaconList == null || beaconList.size() == 0) {//插入
							Beacon beacon = new Beacon();
							beacon.setMac_id(mac_id);
							beacon.setUuid(uuid);
							beacon.setMajor(major);
							beacon.setMinor(minor);
							beacon.setAddress(address);
							beacon.setAddress_type(address_type);
							beacon.setBuilding(building);
							beacon.setFloor(floor);
							beacon.setCoord_x(coord_x);
							beacon.setCoord_y(coord_y);
							beacon.setCoverage(coverage);
							beacon.setPower(power);
							beacon.setFrequency(frequency);
							beacon.setType(type);
							beacon.setFirm(firm);
							beacon.setCreate_time(create_time);
							beacon.setCreate_id(create_id);
							beacon.setLast_modify_time(last_modify_time);
							beacon.setStatus(status);
							beacon.setLast_modify_id(last_modify_id);
							hibernateTemplate.save(beacon);
							insertNumber++;
						}else{//覆盖
							Beacon beacon = beaconList.get(0);
							beacon.setUuid(uuid);
							beacon.setMajor(major);
							beacon.setMinor(minor);
							beacon.setAddress(address);
							beacon.setAddress_type(address_type);
							beacon.setBuilding(building);
							beacon.setFloor(floor);
							beacon.setCoord_x(coord_x);
							beacon.setCoord_y(coord_y);
							beacon.setCoverage(coverage);
							beacon.setPower(power);
							beacon.setFrequency(frequency);
							beacon.setType(type);
							beacon.setFirm(firm);
							beacon.setCreate_time(create_time);
							beacon.setCreate_id(create_id);
							beacon.setLast_modify_time(last_modify_time);
							beacon.setStatus(status);
							beacon.setLast_modify_id(last_modify_id);
							hibernateTemplate.update(beacon);
							updateNumber++;
						}
					}
					resultObject.put("message","导入配置信息excel成功.其中新增"+insertNumber+"条,覆盖"+updateNumber+"条");
					resultObject.put("success","true");
				}else{
					resultObject.put("message","excel所含列项不符合约定，请检查excel内容");
					resultObject.put("success",false);
				}
				
			}else{
				resultObject.put("message","excel所含列数不符合约定，请检查excel内容");
				resultObject.put("success",false);
			}			
			book.close();  			
			return resultObject;			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			resultObject.put("message", "导入配置信息excel失败"+e.getMessage());
			resultObject.put("success", false);
			return resultObject;
		}
	}
	
	//给mac_id加冒号
		public String addColon(String str) {
			str = str.replace(":",""); 
			char [] b = str.toCharArray();
			char[] bb = new char[17];
			bb[0] = b[0];
			bb[1] = b[1];
			bb[2] = ':';
			bb[3] = b[2];
			bb[4] = b[3];
			bb[5] = ':';
			bb[6] = b[4];
			bb[7] = b[5];
			bb[8] = ':';
			bb[9] = b[6];
			bb[10] = b[7];
			bb[11] = ':';
			bb[12] = b[8];
			bb[13] = b[9];
			bb[14] = ':';
			bb[15] = b[10];
			bb[16] = b[11];
			return new String(bb);
		}
		
		//格式化power字段
		public String formatPower(String power){
			if(power.indexOf("-30")>-1){
				return "最低（-30db）";
			}else if(power.indexOf("-20")>-1){
				return "非常低（-20db）";
			}else if(power.indexOf("-16")>-1){
				return "很低（-16db）";
			}else if(power.indexOf("-12")>-1){
				return "低（-12db）";
			}else if(power.indexOf("-8")>-1){
				return "高（-8db）";
			}else if(power.indexOf("-4")>-1){
				return "很高（-4db）";
			}else if(power.indexOf("0")>-1){
				return "非常高（0db）";
			}else if(power.indexOf("+4")>-1){
				return "最高（+4db）";
			}else{
				return power;
			}
		}
		
		public String getCoverage(String power, String type, String firm){
			if (firm.indexOf("讯通") > -1) {
				if(power.indexOf("-30")>-1){
					return "Mi_02";
				}else if(power.indexOf("-20")>-1){
					return "Mi_06";
				}else if(power.indexOf("-16")>-1){
					return "Mi_08";
				}else if(power.indexOf("-12")>-1){
					return "Mi_13";
				}else if(power.indexOf("-8")>-1){
					return "Mi_20";
				}else if(power.indexOf("-4")>-1){
					return "Mi_30";
				}else if(power.indexOf("0")>-1){
					return "Mi_38";
				}else if(power.indexOf("+4")>-1){
					return "Mi_43";
				}else{
					return "";
				}
			} else if (firm.indexOf("创新微") > -1) {
				if(power.indexOf("-30")>-1){
					return "Mi_02";
				}else if(power.indexOf("-20")>-1){
					return "Mi_07";
				}else if(power.indexOf("-16")>-1){
					return "Mi_10";
				}else if(power.indexOf("-12")>-1){
					return "Mi_15";
				}else if(power.indexOf("-8")>-1){
					return "Mi_22";
				}else if(power.indexOf("-4")>-1){
					return "Mi_28";
				}else if(power.indexOf("0")>-1){
					if(type.toLowerCase().indexOf("c7") > -1) {
						return "Mi_100";
					} else {
						return "Mi_50";
					}
				}else if(power.indexOf("+4")>-1){
					return "Mi_90";
				}else{
					return "";
				}
			} else if (firm.indexOf("TCL") > -1) {
				if(power.indexOf("-30")>-1){
					return "Mi_02";
				}else if(power.indexOf("-20")>-1){
					return "Mi_07";
				}else if(power.indexOf("-16")>-1){
					return "Mi_10";
				}else if(power.indexOf("-12")>-1){
					return "Mi_14";
				}else if(power.indexOf("-8")>-1){
					return "Mi_22";
				}else if(power.indexOf("-4")>-1){
					return "Mi_28";
				}else if(power.indexOf("0")>-1){
					return "Mi_36";
				}else if(power.indexOf("+4")>-1){
					return "Mi_50";
				}else{
					return "";
				}
			} else {
				return "";
			}
		}
		
		//格式化frequency字段
		public String formatFrequency(String frequency){
			if(frequency.indexOf("1s")>-1){
				return "最低（1s）";
			} else if (frequency.indexOf("800ms")>-1) {
				return "低（800ms）";
			} else if (frequency.indexOf("750ms")>-1) {
				return "低（750ms）";
			} else if (frequency.indexOf("500ms")>-1) {
				return "普通（500ms）";
			} else if (frequency.indexOf("300ms")>-1) {
				return "高（300ms）";
			} else if (frequency.indexOf("100ms")>-1) {
				return "最高（100ms）";
			} else {
				return frequency;
			}
		}
	
	/*public JSONObject importEquipExcel(String filepath,String user_id){
		JSONObject resultObject = new JSONObject();
		List<Beacon> beaconList = new ArrayList<Beacon>();
		int insertNumber = 0;
		int updateNumber = 0;
		String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		try {
			Workbook book = Workbook.getWorkbook(new File(filepath));  
			Sheet sheet = book.getSheet(0);  
			int cols = sheet.getColumns();
			System.out.println("excel列数为："+cols);
					for (int i = 1; i < sheet.getRows(); i++) {
			 			String mac_id = sheet.getCell(3,i).getContents()!=""?sheet.getCell(3,i).getContents():"";
			 			String address = sheet.getCell(0,i).getContents()!=""?sheet.getCell(0,i).getContents():"";
			 			String address_type = sheet.getCell(6,i).getContents()!=""?sheet.getCell(6,i).getContents():"";
			 			String coord_x = sheet.getCell(1,i).getContents()!=""?sheet.getCell(1,i).getContents():"";
			 			String coord_y = sheet.getCell(2,i).getContents()!=""?sheet.getCell(2,i).getContents():"";
			 			String create_time = TimeStamp2Date(sheet.getCell(4,i).getContents())!=""?TimeStamp2Date(sheet.getCell(4,i).getContents()):time;
			 			String create_id = "chengdu";
			 			String last_modify_time = TimeStamp2Date(sheet.getCell(5,i).getContents())!=""?TimeStamp2Date(sheet.getCell(5,i).getContents()):time;
			 			String status = sheet.getCell(7,i).getContents()!=""?sheet.getCell(7,i).getContents():"";
			 			String last_modify_id = "chengdu";
			 			
			 			beaconList = hibernateTemplate.find("from Beacon b where b.mac_id='" + mac_id + "'");					
						if (beaconList == null || beaconList.size() == 0) {//插入
							Beacon beacon = new Beacon();
							beacon.setMac_id(mac_id);
							beacon.setAddress(address);
							beacon.setAddress_type(address_type);
							beacon.setCoord_x(coord_x);
							beacon.setCoord_y(coord_y);
							beacon.setCreate_time(create_time);
							beacon.setCreate_id(create_id);
							beacon.setLast_modify_time(last_modify_time);
							beacon.setStatus(status);
							beacon.setLast_modify_id(last_modify_id);
							hibernateTemplate.save(beacon);
							insertNumber++;
						}else{//覆盖
							Beacon beacon = beaconList.get(0);
							beacon.setAddress(address);
							beacon.setAddress_type(address_type);
							beacon.setCoord_x(coord_x);
							beacon.setCoord_y(coord_y);
							beacon.setCreate_time(create_time);
							beacon.setCreate_id(create_id);
							beacon.setLast_modify_time(last_modify_time);
							beacon.setStatus(status);
							beacon.setLast_modify_id(last_modify_id);
							hibernateTemplate.update(beacon);
							updateNumber++;
						}
					}
					resultObject.put("message","导入配置信息excel成功.其中新增"+insertNumber+"条,覆盖"+updateNumber+"条");
					resultObject.put("success","true");						
			book.close();  			
			return resultObject;			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			resultObject.put("message", "导入配置信息excel失败"+e.getMessage());
			resultObject.put("success", false);
			return resultObject;
		}
	}*/
	
	/*public String TimeStamp2Date(String timestampString){  
		Long timestamp = Long.parseLong(timestampString)*1000;  
		String date = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(timestamp));  
		return date;  
	}*/
	
	public boolean deployAndConfig(JSONArray data, String user_id){
		List<Beacon> result = new ArrayList<Beacon>();
		String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		try {
			for (int i = 0; i < data.size(); i++) {
				JSONObject testObject = data.getJSONObject(i);
				
				result = hibernateTemplate.find("from Beacon b where b.mac_id = '" + testObject.getString("mac") + "'");
				if(result == null || result.size() == 0){
					Beacon tempBeacon = new Beacon();
					tempBeacon.setMac_id(testObject.getString("mac") == null ? "" : testObject.getString("mac"));
					tempBeacon.setMajor(testObject.getString("major") == null ? "" : testObject.getString("major"));
					tempBeacon.setMinor(testObject.getString("minor") == null ? "" : testObject.getString("minor"));
					tempBeacon.setLast_modify_time(date);
					tempBeacon.setUuid(testObject.getString("uuid") == null ? "" : testObject.getString("uuid"));
					tempBeacon.setBuilding(testObject.getString("building") == null ? "" : testObject.getString("building"));
					tempBeacon.setFloor(testObject.getString("floor") == null ? "" : testObject.getString("floor"));
					tempBeacon.setCoord_x(testObject.getString("coord_x") == null ? "" : testObject.getString("coord_x"));
					tempBeacon.setCoord_y(testObject.getString("coord_y") == null ? "" : testObject.getString("coord_y"));
					tempBeacon.setAddress(testObject.getString("address") == null ? "" : testObject.getString("address"));
					tempBeacon.setStatus(testObject.getString("status") == null ?  "" : testObject.getString("status"));
					tempBeacon.setLast_modify_id(user_id);
					tempBeacon.setAddress_type(testObject.getString("address_type") == null ? "" : testObject.getString("address_type"));
					hibernateTemplate.save(tempBeacon);
				}
				else {
					Beacon tempBeacon = result.get(0);
					tempBeacon.setMajor(testObject.getString("major") == null ? "" : testObject.getString("major"));
					tempBeacon.setMinor(testObject.getString("minor") == null ? "" : testObject.getString("minor"));
					tempBeacon.setLast_modify_time(date);
					tempBeacon.setUuid(testObject.getString("uuid") == null ? "" : testObject.getString("uuid"));
					tempBeacon.setBuilding(testObject.getString("building") == null ? "" : testObject.getString("building"));
					tempBeacon.setFloor(testObject.getString("floor") == null ? "" : testObject.getString("floor"));
					tempBeacon.setCoord_x(testObject.getString("coord_x") == null ? "" : testObject.getString("coord_x"));
					tempBeacon.setCoord_y(testObject.getString("coord_y") == null ? "" : testObject.getString("coord_y"));
					tempBeacon.setAddress(testObject.getString("address") == null ? "" : testObject.getString("address"));
					tempBeacon.setStatus(testObject.getString("status") == null ?  "" : testObject.getString("status"));
					tempBeacon.setAddress_type(testObject.getString("address_type") == null ? "" : testObject.getString("address_type"));
					tempBeacon.setLast_modify_id(user_id);
					hibernateTemplate.update(tempBeacon);
				}
				
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public JSONArray getProject(String user_id){
		List<Project>result=new ArrayList<Project>();
		jsonArray = new JSONArray();
		jsonObject = new JSONObject();
		try {
			result = hibernateTemplate.find("from Project p where p.staff_id='"+user_id+"'");
			if (result != null && result.size() > 0) {
				for (int i = 0; i < result.size(); i++) {
					Project temProject=result.get(i);
					jsonObject.put("project_id", temProject.getProject_id());
					jsonObject.put("title",temProject.getTitle());
					jsonObject.put("description", temProject.getDescription());
					jsonArray.add(jsonObject);
				}
			}
		} catch (Exception e) {
			
		}
		return jsonArray;
	}
	
	public String getMessageId(String uuid, String major, String minor) {
		String messageId = null;
		List<Vdev_mes_bind> vdev_mes_binds = new ArrayList<Vdev_mes_bind>();
		try {
			vdev_mes_binds = hibernateTemplate.find("from Vdev_mes_bind v where v.uuid='"+uuid+"' and v.major='"+major+"' and v.minor='"+minor+"'");
			if(vdev_mes_binds == null || vdev_mes_binds.size()==0){
				return null;
			}else{
				Vdev_mes_bind vdev_mes = vdev_mes_binds.get(0);
				messageId = vdev_mes.getMessage_id();
				return messageId;
			}
		} catch (Exception e) {
			System.out.println("调用getMessageId发生异常："+e.getMessage());
			e.printStackTrace();
			return null;
		}
	}
	
	
	public HibernateTemplate getHibernateTemplate() {
		return hibernateTemplate;
	}
	@Resource
	public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
		this.hibernateTemplate = hibernateTemplate;
	}
	
	public JSONArray getJsonArray() {
		return jsonArray;
	}
	public void setJsonArray(JSONArray jsonArray) {
		this.jsonArray = jsonArray;
	}
	public JSONObject getJsonObject() {
		return jsonObject;
	}
	public void setJsonObject(JSONObject jsonObject) {
		this.jsonObject = jsonObject;
	}
}

package com.buptmap.Service;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;



import java.io.File;
import java.util.HashMap;

import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.Resource;

import jxl.Sheet;
import jxl.Workbook;

import org.springframework.stereotype.Component;

import com.buptmap.DAO.BeaconapiDAO;
import com.buptmap.DAO.MessageDao;
import com.buptmap.DAO.StaffDao;
import com.buptmap.DAO.Staff_devDAO;
import com.buptmap.model.Beacon;


@Component("beaconapiService")
public class BeaconapiService {
	private BeaconapiDAO beaconapiDao;
	private MessageDao messageDao;
	private Staff_devDAO staff_devDao;
	private StaffDao staffDao;
	private UrlService urlService;
	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	
	public JSONObject importEquipExcel(String filePath, String user_id){
		lock.writeLock().lock();
		try{
			return beaconapiDao.importEquipExcel(filePath,user_id);
		}finally{
			lock.writeLock().unlock();
		}
		
	}
	
	public Map<String , Object> getSecretKey(String user_id,String pwd){
		lock.writeLock().lock();
		try{
			return beaconapiDao.getSecretKey(user_id, pwd);
		}finally{
			lock.writeLock().unlock();
		}
	}
	
	public boolean deployAndConfig(JSONArray data, String user_id){
		lock.writeLock().lock();
		try{
			return beaconapiDao.deployAndConfig(data, user_id);
		}finally{
			lock.writeLock().unlock();
		}
	}
	
	public JSONArray getProject(String user_id) {
		lock.writeLock().lock();
		try{
			return beaconapiDao.getProject(user_id);
		}finally{
			lock.writeLock().unlock();
		}
	}
	
	public Map<String,Object> operateMessage(String json, String logo) {
		Map<String, Object> map = new HashMap<String, Object>();
		//json = json.replaceAll("\\\\", "|");
		//System.out.println("修改后的json字符串："+json);
		logo = logo.replaceAll("\\\\", "\\\\\\\\");
		System.out.println("修正后的logo地址为："+logo);
		JSONObject o = JSONObject.fromObject(json);
		
		String userId = o.getString("userId")==null?"":o.getString("userId");
		String title = o.getString("title")==null?"":o.getString("title");
		String subTitle = o.getString("subTitle")==null?"":o.getString("subTitle");
		//String logo = o.getString("logo")==null?"":o.getString("logo").replace("|", "\\\\");
		String projectId = o.getString("projectId")==null?"":o.getString("projectId");
		String url = o.getString("url")==null?"":o.getString("url");
		String uuid = o.getString("uuid")==null?"":o.getString("uuid");
		String major = o.getString("major")==null?"":o.getString("major");
		String minor = o.getString("minor")==null?"":o.getString("minor");
		String other_info = o.getString("other_info")==null?"":o.getString("other_info");
		String messageId = beaconapiDao.getMessageId(uuid,major,minor);
		String jsonStr = null;
		
		if(messageId == null){
			//新增url流程
			System.out.println("------------调用接口新增url流程------------------");
			jsonStr = "[{\"parent_id\":\""+userId+"\",\"title\":\""+title+"\",\"name\":\""+subTitle+"\",";
			jsonStr += "\"content\":\""+url+"\",\"other_info\":\""+other_info+"\",\"session\":[{\"value\":\""+uuid+"\",";
			jsonStr += "\"majors\":[{\"value\":\""+major+"\",\"sections\":[{\"value0\":\""+minor+"\",\"value1\":\""+minor+"\"}]}]}],";
			jsonStr += "\"project_id\":\""+projectId+"\",\"logo\":\""+logo+"\"}]";	
			
			System.out.println("---新增url传递字符串为---"+jsonStr);
			try {
				map = urlService.addMessage(jsonStr);
			} catch (Exception e) {
				e.printStackTrace();
				map.put("success", false);
				map.put("message", e.getMessage());
			}
		}else{
			//修改url流程
			System.out.println("------------调用接口修改url流程------------------");
			jsonStr = "[{\"staff_id\":\""+userId+"\",\"url_id\":\""+messageId+"\",\"title\":\""+title+"\",";
			jsonStr += "\"name\":\""+subTitle+"\",\"content\":\""+url+"\",\"other_info\":\""+other_info+"\",\"project_id\":\""+projectId+"\",";
			jsonStr += "\"logo\":\""+logo+"\"}]";
		
			
			System.out.println("---修改url传递字符串为---"+jsonStr);
			
			try {
				map = urlService.editMessage(jsonStr);
			} catch (Exception e) {
				e.printStackTrace();
				map.put("success", false);
				map.put("message", e.getMessage());
			}			
		}
		return map;
		
	}
	
	public Map<String,Object> updateUrl() {
		Map<String, Object> map = new HashMap<String, Object>();
		String filepath = "E:\\programTools\\Tomcat7.0\\webapps\\beacon\\TemporaryFiles\\w.xls";
		String jsonStr = null;
		int count = 0;
		
		try {
			Workbook book = Workbook.getWorkbook(new File(filepath));  
			Sheet sheet = book.getSheet(0); 
			
			for (int i = 1; i < sheet.getRows(); i++) {
				jsonStr = null;
	 			String messageId = sheet.getCell(0,i).getContents();
	 			String title = sheet.getCell(1,i).getContents();
	 			String subTitle = sheet.getCell(2,i).getContents()!=""?sheet.getCell(2,i).getContents():"";
	 			String url = sheet.getCell(3,i).getContents();
	 			String projectId = sheet.getCell(4,i).getContents()!=""?sheet.getCell(4,i).getContents():"";
	 			String logo_url = sheet.getCell(5,i).getContents();
	 			String other_info = sheet.getCell(6,i).getContents()!=""?sheet.getCell(6,i).getContents():"";
	 			String userId = sheet.getCell(7,i).getContents()!=""?sheet.getCell(7,i).getContents():"";
	 			
	 			jsonStr = "[{\"staff_id\":\""+userId+"\",\"url_id\":\""+messageId+"\",\"title\":\""+title+"\",";
				jsonStr += "\"name\":\""+subTitle+"\",\"content\":\""+url+"\",\"other_info\":\""+other_info+"\",\"project_id\":\""+projectId+"\",";
				jsonStr += "\"logo_url\":\""+logo_url+"\"}]";
				
				System.out.println("-----jsonstr-----"+jsonStr);
				map = urlService.editMessage(jsonStr);
				count++;
			}
			System.out.println("~~~~~~~~~~共执行条数:"+count);
		} catch (Exception e) {
			e.printStackTrace();
			map.put("success", false);
			map.put("message", e.getMessage());
		}
		return map;
	} 

	public BeaconapiDAO getBeaconapiDao() {
		return beaconapiDao;
	}
	@Resource
	public void setBeaconapiDao(BeaconapiDAO beaconapiDao) {
		this.beaconapiDao = beaconapiDao;
	}

	public MessageDao getMessageDao() {
		return messageDao;
	}
	@Resource
	public void setMessageDao(MessageDao messageDao) {
		this.messageDao = messageDao;
	}

	public Staff_devDAO getStaff_devDao() {
		return staff_devDao;
	}
	@Resource
	public void setStaff_devDao(Staff_devDAO staff_devDao) {
		this.staff_devDao = staff_devDao;
	}

	public StaffDao getStaffDao() {
		return staffDao;
	}
	@Resource
	public void setStaffDao(StaffDao staffDao) {
		this.staffDao = staffDao;
	}

	public UrlService getUrlService() {
		return urlService;
	}
	@Resource
	public void setUrlService(UrlService urlService) {
		this.urlService = urlService;
	}
	
	
}

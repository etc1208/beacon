package com.buptmap.DAO;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.annotation.Resource;

import jxl.Sheet;
import jxl.Workbook;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;
import org.apache.commons.dbcp.BasicDataSource;//
import org.springframework.context.ApplicationContext;//
import org.springframework.context.support.ClassPathXmlApplicationContext;//



import com.buptmap.model.Staff;
import com.buptmap.model.Loginrecord;
import com.buptmap.model.Vdev_staff_bind;
import com.buptmap.model.Vdevice;
import com.buptmap.util.AddDevStaffBind;
import com.buptmap.util.EditDevStaffBind;
import com.buptmap.util.MD5Util;  

@Component("staffDao")
public class StaffDao {
	
//	public static String url = "jdbc:mysql://localhost:3306/ibeacon?useUnicode=true&amp;characterEncoding=utf8";//123.57.46.160
//	public static String username = "root";
//	public static String password = "0000";//M@pNext2014
	private HibernateTemplate hibernateTemplate = null;
	private JSONArray jsonArray = null;
	private JSONObject jsonObject = null;
	
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

	/*public JSONObject aaaa(){//手机端
		jsonArray = new JSONArray();
		jsonObject = new JSONObject();
		int i;
		Vdev_staff_bind temp = new Vdev_staff_bind();
		for(i=61114;i<=61693;i++){
			temp.setMajor("10001");
			temp.setMinor(String.valueOf(i));
			temp.setStaff_id("super");
			temp.setStatus("0");
			temp.setTime("2015-9-28 17:55:00");
			temp.setUuid("FDA50693-A4E2-4FB1-AFCF-C6EB07647825");
			hibernateTemplate.save(temp);
		}

		return jsonObject;
	}*/
	
	public JSONObject login(String id, String pwd){//手机端
		jsonArray = new JSONArray();
		jsonObject = new JSONObject();
		List<Staff> result = new ArrayList<Staff>();
		result = hibernateTemplate.find("from Staff s where s.staff_id = '" + id + "'");
		
		if (result.size() == 1) {
			Staff staff = result.get(0);
			//System.out.println("-----数据库中密码："+staff.getPwd()+"---用户输入密码："+pwd+"--------------");
			if (staff.getPwd().equalsIgnoreCase(pwd)) {
				
					String key = UUID.randomUUID().toString();
					jsonObject.put("success", true);
					jsonObject.put("message", "验证成功");
					jsonObject.put("key", key);
					Loginrecord temprecord = new Loginrecord();
					Date now = new Date();
					String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(now.getTime() + 28*60*1000 ));
						
					temprecord.setTemp_id(key);
					temprecord.setTime(time);
					temprecord.setStaff_id(staff.getStaff_id());
					
					hibernateTemplate.save(temprecord);
			
				
				//hibernateTemplate.bulkUpdate("update Staff s set s.key = '" + key +"' where s.staff_id = '" + id + "'");
			}
			else {
				jsonObject.put("success", false);
				jsonObject.put("message", "用户名或者密码错误");
			}
		}
		else if(result.size() == 0) {
			jsonObject.put("success", false);
			jsonObject.put("message", "用户名不存在");
			
		}

		return jsonObject;
	}


	public JSONObject log_in(String id, String pwd){
		jsonArray = new JSONArray();
		jsonObject = new JSONObject();
		List<Staff> result = new ArrayList<Staff>();
		result = hibernateTemplate.find("from Staff s where s.staff_id = '" + id + "'");
		
		if (result.size() == 1) {
			Staff staff = result.get(0);
			if (staff.getPwd().equalsIgnoreCase(pwd)) {
					jsonObject.put("success", true);
					jsonObject.put("staff_id", staff.getStaff_id());
					jsonObject.put("staff_name", staff.getStaff_name());
					jsonObject.put("manage", staff.getManage());
					jsonObject.put("state", staff.getType_id());
			
				
				//hibernateTemplate.bulkUpdate("update Staff s set s.key = '" + key +"' where s.staff_id = '" + id + "'");
			}
			else {
				jsonObject.put("success", false);
				jsonObject.put("message", "ID或者密码错误");
			}
		}
		else if(result.size() == 0) {
			jsonObject.put("success", false);
			jsonObject.put("message", "用户不存在");
			
		}

		return jsonObject;
	}


	public boolean verify(String id, String key){
		jsonArray = new JSONArray();
		jsonObject = new JSONObject();
		
		List<Loginrecord> result = new ArrayList<Loginrecord>();
		String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		result = hibernateTemplate.find("from Loginrecord s where s.staff_id = '" + id + "' and s.temp_id = '" + key + "' and s.time > '" + time +"'");
		System.out.println("验证key");
		if (result.size() == 1) {
			return true;
		}
		else  {
			return false;			
		}		
	}
	
	public JSONArray findall(String user_id){
		List<Staff> result = new ArrayList<Staff>();
		//List<Staff> result=new LinkedList<Staff>();
		jsonArray = new JSONArray();
		jsonObject = new JSONObject();
		if (user_id.equalsIgnoreCase("super")) {
			result = hibernateTemplate.find("from Staff s");
			if (result != null && result.size() > 0) {
				for (int i = 0; i < result.size(); i++) {
					Staff tempStaff = result.get(i);
					jsonObject.put("id", tempStaff.getStaff_id());
					jsonObject.put("name", tempStaff.getStaff_name());
					jsonObject.put("pwd", MD5Util.convertMD5(tempStaff.getTempid().toString()));
					//System.out.println(MD5Util.convertMD5(tempStaff.getTempid().toString()));
					jsonObject.put("manage", tempStaff.getManage());
					jsonObject.put("contact", tempStaff.getContact() == null ? "" : tempStaff.getContact());
					jsonObject.put("other", tempStaff.getOther_info() == null ? "" : tempStaff.getOther_info());
					jsonObject.put("session", tempStaff.getSessions() == null ? "" : tempStaff.getSessions());
					jsonObject.put("qq", tempStaff.getQq() == null ? "" : tempStaff.getQq());
					jsonObject.put("email", tempStaff.getEmail() == null ? "" : tempStaff.getEmail());
					jsonObject.put("wechat", tempStaff.getWeichat() == null ? "" : tempStaff.getWeichat());
					jsonObject.put("type_id", tempStaff.getType_id() == null ? "" : tempStaff.getType_id());
					jsonObject.put("parent_id", tempStaff.getParent_id() == null ? "" : tempStaff.getParent_id());
					jsonArray.add(jsonObject);
				}
				
			}
			return jsonArray;
		}
		else {
			result = hibernateTemplate.find("from Staff s where s.parent_id = '" + user_id + "'");
			if (result != null && result.size() > 0) {
				for (int i = 0; i < result.size(); i++) {
					Staff tempStaff = result.get(i);
					jsonObject.put("id", tempStaff.getStaff_id());
					jsonObject.put("name", tempStaff.getStaff_name());
					jsonObject.put("pwd", MD5Util.convertMD5(tempStaff.getTempid().toString()));
					//System.out.println(MD5Util.convertMD5(tempStaff.getTempid().toString()));
					jsonObject.put("manage", tempStaff.getManage());
					jsonObject.put("contact", tempStaff.getContact() == null ? "" : tempStaff.getContact());
					jsonObject.put("other", tempStaff.getOther_info() == null ? "" : tempStaff.getOther_info());
					jsonObject.put("session", tempStaff.getSessions() == null ? "" : tempStaff.getSessions());
					jsonObject.put("qq", tempStaff.getQq() == null ? "" : tempStaff.getQq());
					jsonObject.put("email", tempStaff.getEmail() == null ? "" : tempStaff.getEmail());
					jsonObject.put("wechat", tempStaff.getWeichat() == null ? "" : tempStaff.getWeichat());
					jsonObject.put("type_id", tempStaff.getType_id() == null ? "" : tempStaff.getType_id());
					jsonObject.put("parent_id", tempStaff.getParent_id() == null ? "" : tempStaff.getParent_id());
					
					jsonArray.add(jsonObject);
				}
				
			}
			return jsonArray;
		}
		
	}

	public JSONArray findone(String id){
			
		List<Staff> result = new ArrayList<Staff>();
		jsonArray = new JSONArray();
		jsonObject = new JSONObject();
		result = hibernateTemplate.find("from Staff s where s.staff_id = '" + id + "'");
		
		if (result != null && result.size() > 0) {
			for (int i = 0; i < result.size(); i++) {
				Staff tempStaff = result.get(i);
				jsonObject.put("id", tempStaff.getStaff_id());
				jsonObject.put("name", tempStaff.getStaff_name());
				jsonObject.put("pwd", MD5Util.convertMD5(tempStaff.getTempid().toString()));
				System.out.println(MD5Util.convertMD5(tempStaff.getTempid().toString()));
				jsonObject.put("manage", tempStaff.getManage());
				jsonObject.put("contact", tempStaff.getContact() == null ? "" : tempStaff.getContact());
				jsonObject.put("other", tempStaff.getOther_info() == null ? "" : tempStaff.getOther_info());
				jsonObject.put("session", tempStaff.getSessions() == null ? "" : tempStaff.getSessions().toString());
				jsonObject.put("device", findsessiondev(tempStaff.getSessions(),id));  //Lynn添加设备数量
				jsonObject.put("qq", tempStaff.getQq() == null ? "" : tempStaff.getQq());
				jsonObject.put("email", tempStaff.getEmail() == null ? "" : tempStaff.getEmail());
				jsonObject.put("wechat", tempStaff.getWeichat() == null ? "" : tempStaff.getWeichat());
				jsonObject.put("type_id", tempStaff.getType_id() == null ? "" : tempStaff.getType_id());
				jsonObject.put("parent_id", tempStaff.getParent_id() == null ? "" : tempStaff.getParent_id());
				
				jsonArray.add(jsonObject);
			}
			
		}
		return jsonArray;
	}

	/**根据staff的权限，返回每段权限内，拥有的设备
	 * @author buptLynn
	 * @method: findsessiondev() 
	 * @TODO:  
	 * @param jsonstr
	 * @return JSONArray
	 */
	public JSONArray findsessiondev(String session,String staff_id){		
		System.out.println("str:"+session);
		JSONArray jsonArray2 = new JSONArray();
		JSONObject jsonObject2 = new JSONObject();
		//解析权限session
		JSONArray sessionArray = JSONArray.fromObject(session);
		for (int i = 0; i < sessionArray.size(); i++) {
			JSONObject sessionObject = sessionArray.getJSONObject(i);
			String uuid = sessionObject.getString("value");
			JSONArray majorsArray = sessionObject.getJSONArray("majors");
			for (int j = 0; j < majorsArray.size(); j++) {
				JSONObject majorsObject = majorsArray.getJSONObject(j);
				String major = majorsObject.getString("value");
				JSONArray minorArray = majorsObject.getJSONArray("sections");
				for (int k = 0; k < minorArray.size(); k++) {
					JSONObject minorObject = minorArray.getJSONObject(k);
					String v0 = minorObject.getString("value0");
					String v1 = minorObject.getString("value1");
					List<Object[]>allList = hibernateTemplate.find("select status, count(*) from Beacon where uuid like '%"+uuid+"%' and major = '"+major+"' and minor >='"+v0+"' and minor <='"+v1+"' and (create_id='"+staff_id+"' or last_modify_id='"+staff_id+"') group by status");
					
					jsonObject2.put("peizhi", "0");
					jsonObject2.put("bushu", "0");
					jsonObject2.put("waitbushu", "0");
					jsonObject2.put("huishou", "0");
					
					int allcount=0;
					for(int l = 0;l<allList.size();l++)
					{
						if(allList.get(l)[0].equals("已配置"))
						{
							jsonObject2.put("peizhi", allList.get(l)[1].toString());
						}
						if(allList.get(l)[0].equals("已部署"))
						{
							jsonObject2.put("bushu", allList.get(l)[1].toString());
						}
						if(allList.get(l)[0].equals("待部署"))
						{
							jsonObject2.put("waitbushu", allList.get(l)[1].toString());
						}
						if(allList.get(l)[0].equals("回收"))
						{
							jsonObject2.put("huishou", allList.get(l)[1].toString());
						}
						allcount += Integer.valueOf(allList.get(l)[1].toString());						
					}
					jsonObject2.put("all",String.valueOf(allcount));	
					
					List<Object>urlList = hibernateTemplate.find("select distinct message_id,mes_status from Mes_dev where uuid like '%"+uuid+"%' and major = '"+major+"' and minor >='"+v0+"' and minor <='"+v1+"' and mes_status='2' ");
					jsonObject2.put("url",urlList.size());	
					jsonArray2.add(jsonObject2);
					jsonObject2.clear();
				}
				
			}
		}
 		//查beacon库，总数，已部署，已配置
		
		
		return jsonArray2;		
	}
	
	public JSONArray findonebysession(String jsonstr){
		//uuid,major,value0,value1;
		System.out.println("findonebysession:"+jsonstr);
		jsonArray = new JSONArray();
		jsonObject = new JSONObject();
		//JSONObject jObject = new JSONObject(); 
		JSONArray jArray = JSONArray.fromObject(jsonstr);		
		//jObject = JSONObject.fromObject(jsonstr);
		String staff_id = jArray.getJSONObject(0).getString("staff_id");
		String uuid = jArray.getJSONObject(0).getString("uuid");
		String major = jArray.getJSONObject(0).getString("major");
		String value0 = jArray.getJSONObject(0).getString("value0");
		String value1 = jArray.getJSONObject(0).getString("value1");
		int nums = Integer.valueOf(value1) - Integer.valueOf(value0) + 1 ;
		List<Object[]>resulList = new ArrayList<Object[]>();
		resulList = hibernateTemplate.find("select count( distinct minor ),staff_id from Vdev_staff_bind where uuid like '%"+uuid+"%' and major = '"+major+"' and minor >= '"+value0+"' and minor <= '"+value1+"' GROUP BY staff_id");
		if (resulList != null && resulList.size() > 0) {
			//System.out.println(resulList.size()+"");
			for (int i = 0; i < resulList.size(); i++) {
				Object[] result = resulList.get(i);
				//System.out.println("staff:"+result[1]+"个数："+result[0]);
				if(nums==Integer.valueOf(result[0].toString()))
				{
					//jsonObject.put(i,result[1].toString());
					//System.out.println(result[1]+"和"+staff_id);
					if(AreYouMyChildren(result[1].toString(),staff_id))
					{
						//System.out.println(result[1]+"是"+staff_id+"子代理");
						List<Staff>staffList = new ArrayList<Staff>();
						staffList = hibernateTemplate.find("from Staff where staff_id = '"+result[1].toString()+"'");
						if(staffList.size()==1)
						{
							//System.out.println(staffList.get(0).getStaff_id());
							Staff tempStaff = staffList.get(0);
							jsonObject.put("id", tempStaff.getStaff_id());
							jsonObject.put("name", tempStaff.getStaff_name());
							jsonObject.put("pwd", MD5Util.convertMD5(tempStaff.getTempid().toString()));
							System.out.println(MD5Util.convertMD5(tempStaff.getTempid().toString()));
							jsonObject.put("manage", tempStaff.getManage());
							jsonObject.put("contact", tempStaff.getContact() == null ? "" : tempStaff.getContact());
							jsonObject.put("other", tempStaff.getOther_info() == null ? "" : tempStaff.getOther_info());
							jsonObject.put("session", tempStaff.getSessions() == null ? "" : tempStaff.getSessions().toString());
							//jsonObject.put("device", findsessiondev(tempStaff.getSessions()));  //Lynn添加设备数量
							jsonObject.put("qq", tempStaff.getQq() == null ? "" : tempStaff.getQq());
							jsonObject.put("email", tempStaff.getEmail() == null ? "" : tempStaff.getEmail());
							jsonObject.put("wechat", tempStaff.getWeichat() == null ? "" : tempStaff.getWeichat());
							jsonObject.put("type_id", tempStaff.getType_id() == null ? "" : tempStaff.getType_id());
							jsonObject.put("parent_id", tempStaff.getParent_id() == null ? "" : tempStaff.getParent_id());
							
							jsonArray.add(jsonObject);
						}
					}
					
					//jsonArray.add(jsonObject);
				}					
			}			
		}
		
		
		//jsonArray.add(jsonObject);
		return jsonArray;
	}
	//staff_id是否是StaffId或子代理
	public boolean AreYouMyChildren(String staff_id,String StaffId)
	{
		boolean YouAreMyChildren = false;
		if (StaffId.equalsIgnoreCase("super")) {
			return true;
		}

		if (staff_id.equals(StaffId)) {
			return true;			
		}
		else {
			List<Staff>staffs = new ArrayList<Staff>();
			staffs = hibernateTemplate.find("from Staff s where parent_id ='"+StaffId+"'");
			if (staffs!=null&&staffs.size()>0) {
				for (int i = 0; i < staffs.size(); i++) {
					Staff sStaff = staffs.get(i);
					String sStaff_id=sStaff.getStaff_id();
					YouAreMyChildren = AreYouMyChildren(staff_id, sStaff_id);	
					if(YouAreMyChildren)
					{
						return true;
					}
				}				
			}
			return YouAreMyChildren;
		}
		
	}
	
	public Staff getone(String id){
		List<Staff> result = new ArrayList<Staff>();
		result = hibernateTemplate.find("from Staff s where s.staff_id = '" + id + "'");
		if (result != null && result.size() > 0) {
				return result.get(0);
		}
		return null;
	}
	
	public boolean add(String jsonstr){
		ApplicationContext context = new ClassPathXmlApplicationContext("spring.xml");
		BasicDataSource datasource = (BasicDataSource) context.getBean("dataSource");
		String url = datasource.getUrl();
		String username = datasource.getUsername();
		String password = datasource.getPassword();
		
		String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		jsonArray = JSONArray.fromObject(jsonstr);
		jsonObject = jsonArray.getJSONObject(0);
		List<Staff> result = new ArrayList<Staff>();
		JSONObject uuidObject = null;
		String uuid = null;
		JSONArray majorArray = null;
		JSONObject majorObject = null;
		String major = null;
		JSONObject minorObject = null;
		JSONArray minorArray = null;
		List<Vdev_staff_bind> bindList = new ArrayList<Vdev_staff_bind>();
		List<Vdev_staff_bind> bindList1 = new ArrayList<Vdev_staff_bind>();
		result = hibernateTemplate.find("from Staff s where s.staff_id = '" + jsonObject.getString("staff_id") + "'");
		if (result.size() == 0) {
			Staff tempStaff = new Staff();
			tempStaff.setStaff_id(jsonObject.getString("staff_id"));
			tempStaff.setPwd(MD5Util.string2MD5(jsonObject.getString("pwd")));
			tempStaff.setStaff_name(jsonObject.getString("name"));
			tempStaff.setTempid(MD5Util.convertMD5(jsonObject.getString("pwd")));
			tempStaff.setManage(jsonObject.getString("manage"));
			tempStaff.setContact(jsonObject.getString("contact"));
			tempStaff.setOther_info(jsonObject.getString("other"));
			tempStaff.setCompany_id("B01001");
			tempStaff.setSessions(jsonObject.getString("session"));
			tempStaff.setParent_id(jsonObject.getString("parent_id"));
			tempStaff.setEmail(jsonObject.getString("email")==null ? "" : jsonObject.getString("email"));
			tempStaff.setQq(jsonObject.getString("qq") == null ? "" : jsonObject.getString("qq"));
			System.out.println(jsonObject.getString("qq"));
			tempStaff.setWeichat(jsonObject.getString("wechat") == null ? "" : jsonObject.getString("wechat"));
			tempStaff.setType_id(jsonObject.getString("type_id") == null ? "" : jsonObject.getString("type_id"));
			tempStaff.setCompany_id("B01001");
			String  date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
			tempStaff.setTime(date);
			hibernateTemplate.save(tempStaff);
			
			JSONArray uuidArray = JSONArray.fromObject(jsonObject.getString("session"));
			
			String staff_id = jsonObject.getString("staff_id");
			String parent_id = jsonObject.getString("parent_id");
			try {
				ExecutorService executor = Executors.newFixedThreadPool(32);
				
				//st = conn.createStatement();
				if (uuidArray != null && uuidArray.size() != 0) {
					for (int i = 0; i < uuidArray.size(); i++) {
						uuidObject = uuidArray.getJSONObject(i);
						uuid = uuidObject.getString("value");
						majorArray = JSONArray.fromObject(uuidObject.getString("majors"));
						for (int j = 0; j < majorArray.size(); j++) {
							majorObject = majorArray.getJSONObject(j);
							major = majorObject.getString("value");
							minorArray = JSONArray.fromObject(majorObject.getString("sections"));
							for (int k = 0; k < minorArray.size(); k++) {
								minorObject = minorArray.getJSONObject(k);
								int start = Integer.valueOf(minorObject.getString("value0"));
								int end = Integer.valueOf(minorObject.getString("value1"));
								System.out.println(start);
								System.out.print(end);
								for (int l = start; l <= end; l = l + 51) {
									if (end - l >= 50) {
										AddDevStaffBind addDevStaffBind = new AddDevStaffBind(url, username, password, parent_id, staff_id, uuid, major, l, l+50);
										executor.execute(addDevStaffBind);
									}
									else {
										AddDevStaffBind addDevStaffBind = new AddDevStaffBind(url, username, password, parent_id, staff_id, uuid, major, l, end);
										executor.execute(addDevStaffBind);
									}
								}
								
							}
						}
					}
				}
			
				System.out.println("wancheng");
				executor.shutdown();
				 return true;
			}
			catch (Exception e) {
				
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		}
		else{
			return false;
		}
		
	}
	
	public boolean edit(String jsonstr){
		ApplicationContext context = new ClassPathXmlApplicationContext("spring.xml");
		BasicDataSource datasource = (BasicDataSource) context.getBean("dataSource");
		String url = datasource.getUrl();
		String username = datasource.getUsername();
		String password = datasource.getPassword();
		
		String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		jsonArray = JSONArray.fromObject(jsonstr);
		jsonObject = jsonArray.getJSONObject(0);
		
		List<Staff> result = new ArrayList<Staff>();
		List<Vdev_staff_bind> bindList = new ArrayList<Vdev_staff_bind>();
		List<Vdev_staff_bind> bindList1 = new ArrayList<Vdev_staff_bind>();
		List<Vdevice> deviceList = new ArrayList<Vdevice>();
		List<Vdevice> deviceList2 = new ArrayList<Vdevice>();
		result = hibernateTemplate.find("from Staff s where s.staff_id = '" + jsonObject.getString("staff_id") + "'");
		if (result.size() == 1) {
			Staff tempStaff = result.get(0);
			tempStaff.setStaff_id(jsonObject.getString("staff_id"));
			tempStaff.setPwd(MD5Util.string2MD5(jsonObject.getString("pwd")));
			tempStaff.setStaff_name(jsonObject.getString("name"));
			tempStaff.setTempid(MD5Util.convertMD5(jsonObject.getString("pwd")));
			tempStaff.setManage(jsonObject.getString("manage"));
			tempStaff.setContact(jsonObject.getString("contact"));
			tempStaff.setOther_info(jsonObject.getString("other"));
			tempStaff.setCompany_id("B01001");
			tempStaff.setSessions(jsonObject.getString("session"));
			tempStaff.setParent_id(jsonObject.getString("parent_id"));//lynn
			tempStaff.setEmail(jsonObject.getString("email")==null ? "" : jsonObject.getString("email"));
			tempStaff.setQq(jsonObject.getString("qq") == null ? "" : jsonObject.getString("qq"));
			System.out.println(jsonObject.getString("qq"));
			tempStaff.setWeichat(jsonObject.getString("wechat") == null ? "" : jsonObject.getString("wechat"));
			tempStaff.setType_id(jsonObject.getString("type_id") == null ? "" : jsonObject.getString("type_id"));
			tempStaff.setCompany_id("B01001");
			String  date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
			tempStaff.setTime(date);
			
			//多线程edit权限
			try {
				
				ExecutorService executor = Executors.newFixedThreadPool(32);//32
				//Executor executor = (ThreadPoolExecutor)Executors.newFixedThreadPool(32);
				String staffId =jsonObject.getString("staff_id");
				String parentId = jsonObject.getString("parent_id");
				JSONArray jsonArrayNew = JSONArray.fromObject(jsonObject.getString("session"));
				List<Staff>staffs = new ArrayList<Staff>();
				
				staffs = hibernateTemplate.find("from Staff s where s.staff_id='"+staffId+"'");
				if (staffs.size()==1) {
					Staff oldStaff = staffs.get(0);
					JSONArray jsonArrayOld = JSONArray.fromObject(oldStaff.getSessions());
					JSONArray jsonArrayDel=find_differ_session(jsonArrayOld,jsonArrayNew);
					JSONArray jsonArrayAdd=find_differ_session(jsonArrayNew,jsonArrayOld);
					//////////////////////////////////////////////////////////////
					if(oldStaff.getSessions().equals("all"))//判断super的权限是all的情况
					{
						hibernateTemplate.update(tempStaff);
						return true;
					}
					if (jsonArrayDel!=null&&jsonArrayDel.size()>0) {
						//有删除权限
						
						int size=jsonArrayDel.size();
						System.out.print(size);
						System.out.println("--有删除权限");
						for (int i = 0; i < jsonArrayDel.size(); i=i+51) {
							if (jsonArrayDel.size()-i>50) {
								EditDevStaffBind editDevStaffBind = new EditDevStaffBind(url, username, password, parentId, staffId, jsonArrayDel, i, i+50, false);
								executor.execute(editDevStaffBind);
								
							} else {
								EditDevStaffBind editDevStaffBind = new EditDevStaffBind(url, username, password, parentId, staffId, jsonArrayDel, i, size-1, false);
								executor.execute(editDevStaffBind);

							}
						
						}
						
					}
					if (jsonArrayAdd!=null&&jsonArrayAdd.size()>0) {
						//有增加权限
						int size=jsonArrayAdd.size();
						System.out.print(size);
						System.out.println("--有增加权限");
						for (int i = 0; i < jsonArrayAdd.size(); i=i+51) {
							if (jsonArrayAdd.size()-i>50) {
								EditDevStaffBind editDevStaffBind = new EditDevStaffBind(url, username, password, parentId, staffId, jsonArrayAdd, i, i+50, true);
								executor.execute(editDevStaffBind);
								
							} else {
								EditDevStaffBind editDevStaffBind = new EditDevStaffBind(url, username, password, parentId, staffId, jsonArrayAdd, i, size-1, true);
								executor.execute(editDevStaffBind);

							}
						
						}
					}
					System.out.println("edit is ok!");
					executor.shutdown();
					
				}
				else {
					return false;
				}
				
			} 

			
			/*
			//修改edit，根据权限增减
			try {
				
				String staffId =jsonObject.getString("staff_id");
				String parentId = jsonObject.getString("parent_id");
				JSONArray jsonArrayNew = JSONArray.fromObject(jsonObject.getString("session"));
				List<Staff>staffs = new ArrayList<Staff>();
				
				Connection conn=null;
				PreparedStatement pst = null;
				PreparedStatement updatepst = null;
				PreparedStatement deletepst =null;
				conn = DriverManager.getConnection(url, username, password);
				conn.setAutoCommit(false);
				pst = conn.prepareStatement("insert into vdev_staff_bind(uuid,major,minor,status,staff_id,time) value(?,?,?,?,?,?)");
				updatepst = conn.prepareStatement("update Vdev_staff_bind set status=? where uuid=?  and major=? and minor=? and staff_id = '" + parentId + "'");
				deletepst = conn.prepareStatement("delete from Vdev_staff_bind where uuid=? and major=? and minor=? and staff_id ='"+ staffId +"'");
				
				staffs = hibernateTemplate.find("from Staff s where s.staff_id='"+staffId+"'");
				if (staffs.size()==1) {
					Staff oldStaff = staffs.get(0);
					JSONArray jsonArrayOld = JSONArray.fromObject(oldStaff.getSessions());
					JSONArray jsonArrayDel=find_differ_session(jsonArrayOld,jsonArrayNew);
					JSONArray jsonArrayAdd=find_differ_session(jsonArrayNew,jsonArrayOld);
					if (jsonArrayDel!=null&&jsonArrayDel.size()>0) {
						//有删除权限
						System.out.println("有删除权限");
						for (int i = 0; i < jsonArrayDel.size(); i++) {
							JSONObject jsonObjectDel =jsonArrayDel.getJSONObject(i);
							String vuuid = jsonObjectDel.getString("uuid");
							String vmajor=jsonObjectDel.getString("major");
							String vminor = jsonObjectDel.getString("minor");
							
							System.out.println("from Vdev_staff_bind v where v.staff_id='"+staffId+"'and v.uuid='"+vuuid+"'and v.major='"+vmajor+"' and v.minor='"+vminor+"'");
							bindList=hibernateTemplate.find("from Vdev_staff_bind v where v.staff_id='"+staffId+"'and v.uuid='"+vuuid+"'and v.major='"+vmajor+"' and v.minor='"+vminor+"'");
							if (bindList.size()>0) {
								//判断status是0 才可以删除
								
								deletepst.setString(1, vuuid);
								deletepst.setString(2, vmajor);
								deletepst.setString(3, vminor);
								deletepst.addBatch();
								
							}
							bindList1=hibernateTemplate.find("from Vdev_staff_bind v where v.staff_id='"+parentId+"'and v.uuid='"+vuuid+"'and v.major='"+vmajor+"' and v.minor='"+vminor+"'");
							if (bindList1.size() == 1) {
								Vdev_staff_bind tempBind1 = bindList1.get(0);
								String statusp=String.valueOf(Integer.valueOf( tempBind1.getStatus())-1);
								
								updatepst.setString(1,statusp);
								updatepst.setString(2,vuuid);
								updatepst.setString(3,vmajor);
								updatepst.setString(4,vminor);
								updatepst.addBatch();
								
							}
							
							if (i%50==49) {
								updatepst.executeBatch();
								deletepst.executeBatch();
						        conn.commit();
							}
						}
						
					}
					if (jsonArrayAdd!=null&&jsonArrayAdd.size()>0) {
						//有增加权限
						System.out.println("有增加权限");
						for (int i = 0; i < jsonArrayAdd.size(); i++) {
							JSONObject jsonObjectAdd =jsonArrayAdd.getJSONObject(i);
							String vuuid = jsonObjectAdd.getString("uuid");
							String vmajor=jsonObjectAdd.getString("major");
							String vminor = jsonObjectAdd.getString("minor");
							
							
							bindList1=hibernateTemplate.find("from Vdev_staff_bind v where v.staff_id='"+parentId+"'and v.uuid='"+vuuid+"'and v.major='"+vmajor+"' and v.minor='"+vminor+"'");
							if (bindList1.size()>0) {
								Vdev_staff_bind tempStaff_bind = bindList1.get(0);
								String sessionp= String.valueOf(Integer.valueOf(tempStaff_bind.getStatus())+1);
								
								
								updatepst.setString(1,sessionp);
								updatepst.setString(2,vuuid);
								updatepst.setString(3,vmajor);
								updatepst.setString(4,vminor);
								updatepst.addBatch();
								
								pst.setString(1, vuuid);
								pst.setString(2, vmajor);
								pst.setString(3, vminor);
								pst.setString(4, "0");
								pst.setString(5, staffId);
								pst.setString(6, time);
								pst.addBatch();
							}
							else {
								//分配给第一个子代理
								pst.setString(1, vuuid);
								pst.setString(2, vmajor);
								pst.setString(3, vminor);
								pst.setString(4, "0");
								pst.setString(5, staffId);
								pst.setString(6, time);
								pst.addBatch();
								pst.setString(1, vuuid);
								pst.setString(2, vmajor);
								pst.setString(3, vminor);
								pst.setString(4, "1");
								pst.setString(5, parentId);
								pst.setString(6, time);
								pst.addBatch();
								
							}
							if (i%50==49) {
								updatepst.executeBatch();
								pst.executeBatch();
						        conn.commit();
							}
						}						
												
					}
					deletepst.executeBatch();
					updatepst.executeBatch();
					pst.executeBatch();
			        conn.commit();	
				
					System.out.println("wancheng");
					deletepst.close();
					updatepst.close();
					pst.close();
					conn.close();
					conn = null;
				}
				else {
					return false;
					
				}
				
				
			}
			*/
			 catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
				return false;
			}
			hibernateTemplate.update(tempStaff);
			return true;
		}
		
		else{
			return false;
		}
		
	}
	
	public boolean delete(String id){
		List<Staff> result = new ArrayList<Staff>();
		jsonArray = new JSONArray();
		jsonObject = new JSONObject();
		result = hibernateTemplate.find("from Staff s where s.staff_id = '" + id + "'");
		
		if (result != null && result.size() > 0) {
			Staff tempStaff = result.get(0);
			hibernateTemplate.delete(tempStaff);
		}
		return true;
	}


	//Lynn 寻找sessions改变的部分
	
	public JSONArray foundSessionChange(String jsonstr) {
		JSONArray returnJsonArray = new JSONArray();
		JSONObject returnJsonObject = new JSONObject();
		jsonArray = JSONArray.fromObject(jsonstr);
		jsonObject = jsonArray.getJSONObject(0);
		String staffId=jsonObject.getString("staff_id");
		String SessionsNew = jsonObject.getString("sessions");
		JSONArray jsonArrayNew=JSONArray.fromObject(SessionsNew);//new
		
		
		List<Staff> staffs = new ArrayList<Staff>();
		staffs=hibernateTemplate.find("from Staff s where s.staff_id='"+staffId+"'");
		if(staffs.size()==1)
		{
			Staff staffO=staffs.get(0);
			String SessionsOld=staffO.getSessions();
			JSONArray jsonArrayOld = new JSONArray();
			JSONObject jsonObjectOld = new JSONObject();
			jsonArrayOld=JSONArray.fromObject(SessionsOld);//old
			
			returnJsonArray=find_differ_session(jsonArrayOld,jsonArrayNew);
			
		}		
		return returnJsonArray;
	}
	
	/*Lynn--15-08-06查找属于staff_id的没有绑定的device对应的权限(major，minor */
	
	public JSONArray unused_dev(String staff_id)
	{
		try {
			jsonArray = new JSONArray();
			jsonObject = new JSONObject(); //object - object2
			List<Object[]>objects = new ArrayList<Object[]>();//全部dev
			List<Object[]>objects2 = new ArrayList<Object[]>();//已绑定的dev
			
			objects = hibernateTemplate.find("select distinct minor,major,uuid from Vdev_staff_bind where staff_id='"+staff_id+"' order by uuid,major,minor");
			//objects2 = hibernateTemplate.find("select distinct minor,major,uuid from Vdev_mes_bind order by uuid ,major,minor");
			//暂注释

			/*
			for (int i = 0; i < 8; i++) {
				Object[] find =new Object[3];
				find[0]=String.valueOf(i);
				find[1]="10001";
				find[2]="uuid";
				objects.add(find);
			}
			for (int i = 0; i < 8; i++) {
				if(i%3==1)
				{continue;}
				Object[] find =new Object[3];
				find[0]=String.valueOf(i);
				find[1]="10001";
				find[2]="uuid";
				objects2.add(find);
			}
			for (int i = 0; i < 8; i++) {
				if(i%3==2)
				{continue;}
				Object[] find =new Object[3];
				find[0]=String.valueOf(i);
				find[1]="10009";
				find[2]="haha";
				objects.add(find);
			}
			*/
//			if (objects==null || objects.size()==0) {
//				//权限为空
//				objects=null;
//			}
//			else if (objects2==null || objects2.size()==0) {
//				//全部权限objects都是
//			}
//			else {
//				//返回objects-objects2
//				objects=NotInObject(objects, objects2);
//			}
			if (objects==null || objects.size()==0) {
				//权限为空
				objects=null;
			}
			else{
				objects2 = hibernateTemplate.find("select distinct minor,major,uuid from Mes_dev where mes_status !='4' order by uuid ,major,minor");//where mes_status !='4'
				if (objects2==null || objects2.size()==0) {
					//全部权限objects都是
				}
				else {
					//返回objects-objects2
					objects=NotInObject(objects, objects2);
				}
				
			}
				
				
			//将objects转化成权限集合jsonArray
			jsonArray = objectToJsonArray(objects);
			
			
			
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		
		return jsonArray;
	}
	
	public List<Object[]> NotInObject(List<Object[]> objects1,List<Object[]>objects2) {
		List<Object[]>objects0 = new ArrayList<Object[]>();
		objects0 = objects1;
		int size1=objects0.size(),size2=objects2.size();
		int ptr1=0,ptr2=0,i,j;
		
		
		for(j=ptr2 ; j<size2 ; j++)
		{
			//ptr2我觉得可以优化判断
			Object[] findObject2 = objects2.get(j);
			String uuid2=findObject2[2].toString();
			String major2 = findObject2[1].toString();
			String minor2 = findObject2[0].toString();
			for ( i = ptr1; i < size1; i++) {
				Object[] findObject1 = objects1.get(i);
				if (uuid2.equalsIgnoreCase(findObject1[2].toString())&&major2.equalsIgnoreCase(findObject1[1].toString())&&minor2.equalsIgnoreCase(findObject1[0].toString())) {
					objects0.remove(i);
					--size1;
					ptr1=i;
					break;
				} 
			}
			
		}
		
		return objects0;
	}

	public JSONArray objectToJsonArray(List<Object[]> objects) {
		//test is ok!
		JSONArray rJsonArray = new JSONArray();
		JSONArray majorArray = new JSONArray();
		JSONArray minorArray = new JSONArray();
		JSONObject rJsonObject = new JSONObject();
		JSONObject majorObject = new JSONObject();
		JSONObject minorObject = new JSONObject();
		String uuid0,major0,minor0,uuid1,major1,minor1,minor2;
		
		if (objects==null ||objects.size()==0) {
			rJsonArray=null;
		}
		else if (objects.size()==1) {
			uuid0 = objects.get(0)[2].toString();
			major0 = objects.get(0)[1].toString();
			minor0 = objects.get(0)[0].toString();
			minorObject.put("value0", minor0);
			minorObject.put("value1", minor0);
			minorArray.add(minorObject);
			majorObject.put("value", major0);
			majorObject.put("sections", minorArray);//minor
			majorArray.add(majorObject);
			rJsonObject.put("uuid", uuid0);
			rJsonObject.put("major", majorArray);
			rJsonArray.add(rJsonObject);
			
		} else {
			uuid0 = objects.get(0)[2].toString();
			major0 = objects.get(0)[1].toString();
			minor0 = objects.get(0)[0].toString();
			minor1 = objects.get(0)[0].toString();
			//System.out.println(minor0);
			for (int i = 1; i < objects.size(); i++) {
				uuid1 = objects.get(i)[2].toString();
				major1 = objects.get(i)[1].toString();
				minor2 = objects.get(i)[0].toString();
				//System.out.println(minor2);
				if (uuid1.equals(uuid0)) {
					
					if (major1.equals(major0)) {
						
						if (Integer.valueOf(minor2)-Integer.valueOf(minor1)==1) {
							minor1=minor2;
							
						} else {
							minorObject.put("value0", minor0);
							minorObject.put("value1", minor1);
							minorArray.add(minorObject);
							minor0=minor2;
							minor1=minor2;
						}
						
					}
					else {
						minorObject.put("value0", minor0);
						minorObject.put("value1", minor1);
						minorArray.add(minorObject);
						minor0=minor2;
						minor1=minor2;
						
						majorObject.put("value", major0);
						majorObject.put("sections", minorArray);//minor
						majorArray.add(majorObject);
						major0=major1;		
						minorArray.clear();
						//=null;
						//chushihua shibushi youwenti
						
					}
					
				} else {
					minorObject.put("value0", minor0);
					minorObject.put("value1", minor1);
					minorArray.add(minorObject);
					minor0=minor2;
					minor1=minor2;
					majorObject.put("value", major0);
					majorObject.put("sections", minorArray);//minor
					majorArray.add(majorObject);
					major0=major1;		
					rJsonObject.put("uuid", uuid0);
					rJsonObject.put("major", majorArray);
					rJsonArray.add(rJsonObject);
					uuid0=uuid1;
					minorArray.clear();//=null;
					majorArray.clear();//=null;

				}
				
				
			}
			//zuihoujia
			minorObject.put("value0", minor0);
			minorObject.put("value1", minor1);
			minorArray.add(minorObject);
			majorObject.put("value", major0);
			majorObject.put("sections", minorArray);//minor
			majorArray.add(majorObject);
			rJsonObject.put("uuid", uuid0);
			rJsonObject.put("major", majorArray);
			rJsonArray.add(rJsonObject);
			minorArray.clear();//=null;
			majorArray.clear();//=null;
			rJsonObject.clear();

		}
		
		rJsonObject.clear();//一个的时候clear		
		rJsonObject.put("size", objects.size());
		rJsonArray.add(rJsonObject);
		return rJsonArray; 
		
	}
	
	//判断权限session2是否在权限session1范围内
	
	public boolean verify_session(String  jsonstr){
		try {
			jsonArray = JSONArray.fromObject(jsonstr);
			jsonObject = jsonArray.getJSONObject(0);
			List<Staff> result = new ArrayList<Staff>();
			if (jsonObject.getString("session").equalsIgnoreCase("") ||jsonObject.getString("session").equalsIgnoreCase("[]")) {
				return true;
			}
			JSONArray uuidArray2 = jsonObject.getJSONArray("session");
			result = hibernateTemplate.find("from Staff s where s.staff_id = '" + jsonObject.getString("parent_id") + "'");
			Staff parentStaff = result.get(0);
			if(parentStaff.getSessions().equalsIgnoreCase("all") || parentStaff.getType_id().equalsIgnoreCase("0")){
				return true;
			}
			JSONArray uuidArray1 = JSONArray.fromObject(parentStaff.getSessions());
			return verify_uuidArray(uuidArray1, uuidArray2);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return false;
		}
		
		
	}
	
	public boolean verify_uuidArray(JSONArray uuidArray1, JSONArray uuidArray2){
		boolean flag = true;
		if(uuidArray2==null || uuidArray2.size() == 0)//Lynn
		{
			return true;
		}
		for (int i = 0; i < uuidArray2.size(); i++) {
			JSONObject uuidObject2 = uuidArray2.getJSONObject(i);
			if (!verify_uuidObject(uuidArray1, uuidObject2)) {
				flag = false;
				break;
			}
		}
		return flag;
	}
	
	public boolean verify_uuidObject(JSONArray uuidArray, JSONObject uuidObject){
		boolean flag = false;
		String uuidString = uuidObject.getString("value");
		for (int i = 0; i < uuidArray.size(); i++) {
			if (!uuidArray.getJSONObject(i).getString("value").equals(uuidString)) {
				continue;
			}
			else {
				JSONArray majorArray1 = uuidArray.getJSONObject(i).getJSONArray("majors");
				JSONArray majorArray2 = uuidObject.getJSONArray("majors");
				if (!verify_majorArray(majorArray1, majorArray2)) {
					continue;
				}
				else{
					flag = true;
					break;
				}
			}
		}
		return flag;
	}
	
	public boolean verify_majorArray(JSONArray majorArray1, JSONArray majorArray2){
		boolean flag = true;
		for (int i = 0; i < majorArray2.size(); i++) {
			JSONObject majorObject = majorArray2.getJSONObject(i);
			if (!verify_majorObject(majorArray1, majorObject)) {
				flag = false;
				break;
			}
		}
		return flag;
	}
	
	public boolean verify_majorObject(JSONArray majorArray, JSONObject majorObject){
		boolean flag = false;

		String majorString = majorObject.getString("value");
		for (int i = 0; i < majorArray.size(); i++) {
			if (!majorArray.getJSONObject(i).getString("value").equals(majorString)) {
				continue;
			}
			else {
				JSONArray minorArray1 = majorArray.getJSONObject(i).getJSONArray("sections");
				JSONArray minorArray2 = majorObject.getJSONArray("sections");
				if (!verify_minorArray(minorArray1, minorArray2)) {
					continue;
				}
				else{
					flag = true;
					break;
				}
			}
		}
		return flag;
	}
	
	public boolean verify_minorArray(JSONArray minorArray1, JSONArray minorArray2){
		
		boolean flag = true;
		for (int i = 0; i < minorArray2.size(); i++) {
			JSONObject minorObject = minorArray2.getJSONObject(i);
			if (!verify_minorObject(minorArray1, minorObject)) {
				flag = false;
				break;
			}
		}
		return flag;
	}
	
	public boolean verify_minorObject(JSONArray minorArray, JSONObject minorObject){
		boolean flag = false;
		int child_minor0 = Integer.valueOf(minorObject.getString("value0"));
		int child_minor1 = Integer.valueOf(minorObject.getString("value1"));
		if (child_minor0 <= child_minor1) {
			for (int i = 0; i < minorArray.size(); i++) {
				JSONObject parent_minorObject = minorArray.getJSONObject(i);
				int parent_minor0 = Integer.valueOf(parent_minorObject.getString("value0"));
				int parent_minor1 = Integer.valueOf(parent_minorObject.getString("value1"));
				if (parent_minor0 <= child_minor0 && parent_minor1 >= child_minor1) {
					flag = true;
					break;
				}		
			}
		}	
		return flag;

	
	}
	

	//李晋调用的接口
	public JSONObject addSession(String filepath){
		JSONObject resultObject = new JSONObject();
		List<Staff> staffList = new ArrayList<Staff>();
		List<Vdevice> deviceList = new ArrayList<Vdevice>();
		try {
			String staff_id = "super";
			staffList = hibernateTemplate.find("from Staff s where s.staff_id = '" + staff_id + "'");
			Workbook book = Workbook.getWorkbook(new File(filepath));  
			Sheet sheet = book.getSheet(0);  
			for (int i = 1; i < sheet.getRows(); i++) {
	 			String device_id = sheet.getCell(0,i).getContents();
	 			String uuid = sheet.getCell(1,i).getContents();
	 			String major = sheet.getCell(2,i).getContents();
	 			String minor = sheet.getCell(3,i).getContents();
				deviceList = hibernateTemplate.find("from Vdevice v where v.vdevice_id='" + device_id + "'");					
				if (deviceList == null || deviceList.size() == 0) {
					String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
					Vdevice tempVdevice = new Vdevice();
					tempVdevice.setMajor(major);
					tempVdevice.setMinor(minor);
					tempVdevice.setUuid(uuid);
					tempVdevice.setVdevice_id(device_id);		
					tempVdevice.setType("1");
					tempVdevice.setTime(time);
					hibernateTemplate.save(tempVdevice);
					Vdev_staff_bind tempBind = new Vdev_staff_bind();
					tempBind.setStaff_id(staff_id);
					tempBind.setStatus("0");
					tempBind.setMajor(major);
					tempBind.setMinor(minor);
					tempBind.setUuid(uuid);
					tempBind.setTime(time);
					hibernateTemplate.save(tempBind);
				}
			}
			book.close();  
			if (update_session(staffList.get(0))) {
				resultObject.put("message","分配成功");
				resultObject.put("success","true");
				return resultObject;
			}
			else {
				resultObject.put("message","插入成功，update 失败");
				resultObject.put("success","false");
				return resultObject;
			}
		} catch (Exception e) {
			// TODO: handle exception
			resultObject.put("message", e.getMessage());
			resultObject.put("success", false);
			return resultObject;
		}
	}
	
	//每次插入后重新更新staff的session字段
	public boolean update_session(Staff staff){
		List<Object[]> sessionList = new ArrayList<Object[]>();
		JSONArray uuidArray = new JSONArray();
		try {
			sessionList = hibernateTemplate.find("select distinct uuid,staff_id from Vdev_staff_bind s where s.staff_id = '" + staff.getStaff_id() + "'");
			if (sessionList != null && sessionList.size() != 0) {
				for (int i = 0; i < sessionList.size(); i++) {
					String uuidString = sessionList.get(i)[0].toString();
					JSONObject uuidObject = update_uuid(uuidString, staff.getStaff_id());
					uuidArray.add(uuidObject);
				}
				System.out.println(uuidArray.toString());
				staff.setSessions(uuidArray.toString());
				hibernateTemplate.update(staff);
			}	
			return true;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return false;
		}
	}
	
	public JSONObject update_uuid(String uuid, String staff_id){
		JSONObject uuidObject = new  JSONObject();
		JSONArray majorArray = new JSONArray();
		List<Object[]> majorList = new ArrayList<Object[]>();
		try {
			majorList = hibernateTemplate.find("select distinct major,uuid from Vdev_staff_bind s where s.staff_id = '" + staff_id + "' and s.uuid ='" + uuid +"'");
			for (int i = 0; i < majorList.size(); i++) {
				JSONObject majorObject = new JSONObject();
				String major = majorList.get(i)[0].toString();
				majorObject.put("value", major);
				majorObject.put("sections", update_major(staff_id, uuid, major));
				majorArray.add(majorObject);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		uuidObject.put("value", uuid);
		uuidObject.put("majors", majorArray);
		return uuidObject;
	}
	
	public JSONArray update_major(String staff_id, String uuid, String major){
		JSONArray minorArray = new JSONArray();
		List<Object[]> minorList = new ArrayList<Object[]>();
		int a[] = new int[100000];
		try {
			
			minorList = hibernateTemplate.find("select minor,major from Vdev_staff_bind s where s.staff_id = '" + staff_id + "' and s.uuid ='" + uuid +"' and s.major ='"+ major +"'");
			for (int i = 0; i < minorList.size(); i++) {
				int index = Integer.valueOf(minorList.get(i)[0].toString());
				a[index] = 1;
			}
			int start = 0,i;
			boolean flag = false;
			
			for (i = 0; i < a.length; i++) {
				
				if (a[i] == 0 && flag) {
					JSONObject minorObject = new  JSONObject();
					minorObject.put("value0", String.valueOf(start));
					minorObject.put("value1", String.valueOf(i-1));
					minorArray.add(minorObject);
					flag = false;
				}
				else if(a[i] == 1 && !flag) {
					flag = true;
					start = i;
				}
				else {
					continue;
				}
			}
			if (flag) {
				JSONObject minorObject = new  JSONObject();
				minorObject.put("value0", String.valueOf(start));
				minorObject.put("value1", String.valueOf(i-1));
				minorArray.add(minorObject);
			}
			return minorArray;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return minorArray;
		}
		
	}
	
	//查找每个staff的空闲设备号（major、minor）
	public JSONArray  find_vacant(String staff_id){
		List<Object[]> sessionList = new ArrayList<Object[]>();
		JSONArray uuidArray = new JSONArray();
		try {
			sessionList = hibernateTemplate.find("select distinct uuid,staff_id from Vdev_staff_bind s where s.staff_id = '" + staff_id + "' and s.status='0'");
			if (sessionList != null && sessionList.size() != 0) {
				for (int i = 0; i < sessionList.size(); i++) {
					String uuidString = sessionList.get(i)[0].toString();
					JSONObject uuidObject = find_uuid(uuidString, staff_id);
					uuidArray.add(uuidObject);
				}
				
			}	
			return uuidArray;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return uuidArray;
		}
	}
	
	public JSONObject find_uuid(String uuid, String staff_id){
		JSONObject uuidObject = new  JSONObject();
		JSONArray majorArray = new JSONArray();
		List<Object[]> majorList = new ArrayList<Object[]>();
		try {
			majorList = hibernateTemplate.find("select distinct major,uuid from Vdev_staff_bind s where s.staff_id = '" + staff_id + "' and s.uuid ='" + uuid +"' and s.status='0'");
			for (int i = 0; i < majorList.size(); i++) {
				JSONObject majorObject = new JSONObject();
				String major = majorList.get(i)[0].toString();
				majorObject.put("value", major);
				majorObject.put("sections", find_major(staff_id, uuid, major));
				majorArray.add(majorObject);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		uuidObject.put("value", uuid);
		uuidObject.put("majors", majorArray);
		return uuidObject;
	}
	
	public JSONArray find_major(String staff_id, String uuid, String major){
		JSONArray minorArray = new JSONArray();
		List<Object[]> minorList = new ArrayList<Object[]>();
		int a[] = new int[100000];
		try {
			
			minorList = hibernateTemplate.find("select minor,major from Staff_dev s where s.staff_id = '" + staff_id + "' and s.uuid ='" + uuid +"' and s.major ='"+ major +"' and s.status='0'");
			int max = 0;
			int min = 80000;
			for (int i = 0; i < minorList.size(); i++) {
				int index = Integer.valueOf(minorList.get(i)[0].toString());
				if (min > index) {
					min = index;
				}
				if (max < index) {
					max = index;
				}
				a[index] = 1;
			}
			int start = 0,i;
			boolean flag = false;
			
			for (i = min; i <= max; i++) {
				
				if (a[i] == 0 && flag) {
					JSONObject minorObject = new  JSONObject();
					minorObject.put("value0", String.valueOf(start));
					minorObject.put("value1", String.valueOf(i-1));
					minorArray.add(minorObject);
					flag = false;
				}
				else if(a[i] == 1 && !flag) {
					flag = true;
					start = i;
				}
				else {
					continue;
				}
			}
			if (flag) {
				JSONObject minorObject = new  JSONObject();
				minorObject.put("value0", String.valueOf(start));
				minorObject.put("value1", String.valueOf(i-1));
				minorArray.add(minorObject);
			}
			return minorArray;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return minorArray;
		}
		
	}
	
	
	public boolean test(){
		List<Staff> result = new ArrayList<Staff>();
		JSONArray resultArray = new JSONArray();
		result = hibernateTemplate.find("from Staff s where s.staff_id = 'A01'");
		Staff tempStaff = result.get(0);
		result = hibernateTemplate.find("from Staff s where s.staff_id = 'A02'");
		Staff tempStaff1 = result.get(0);
		resultArray = find_differ_session(JSONArray.fromObject(tempStaff.getSessions()), JSONArray.fromObject(tempStaff1.getSessions()));
		System.out.println(resultArray);
		
		return true;
		
	}
	
	//找出uuidArray-uuidArray1的部分
	public JSONArray find_differ_session(JSONArray uuidArray, JSONArray uuidArray1){
		JSONArray resultArray = new JSONArray();
		JSONObject tempObject = new JSONObject();
		try {

			for (int i = 0; i < uuidArray.size(); i++) {
				JSONObject uuidObject = uuidArray.getJSONObject(i);
				String uuid = uuidObject.getString("value");
				JSONArray majorArray = JSONArray.fromObject(uuidObject.getString("majors"));
				for (int j = 0; j < majorArray.size(); j++) {
					JSONObject majorObject = majorArray.getJSONObject(j);
					String major = majorObject.getString("value");
					JSONArray minorArray = JSONArray.fromObject(majorObject.getString("sections"));
					for (int k = 0; k < minorArray.size(); k++) {
						JSONObject minorObject = minorArray.getJSONObject(k);
						int start = Integer.valueOf(minorObject.getString("value0"));
						int end = Integer.valueOf(minorObject.getString("value1"));
						for (int l = start; l <= end; l++) {
							if(!check_minor(uuid, major, l, uuidArray1)){
								tempObject.put("uuid", uuid);
								tempObject.put("major", major);
								tempObject.put("minor", String.valueOf(l));
								resultArray.add(tempObject);
							}
						}
					}
				}
			}
			return resultArray;
		} catch (Exception e) {
			// TODO: handle exception
			return resultArray;
		}
		
	}
	
	//检查uuid-major-minor 是否在uuidArray里面;在返回true
	public boolean check_minor(String tempuuid, String tempmajor, int tempminor, JSONArray uuidArray){
		boolean flag = false;

		for (int i = 0; i < uuidArray.size(); i++) {
			JSONObject uuidObject = uuidArray.getJSONObject(i);
			String uuid = uuidObject.getString("value");
			if (uuid.equals(tempuuid)) {
				JSONArray majorArray = JSONArray.fromObject(uuidObject.getString("majors"));
				for (int j = 0; j < majorArray.size(); j++) {
					JSONObject majorObject = majorArray.getJSONObject(j);
					String major = majorObject.getString("value");
					if (major.equals(tempmajor)) {
						JSONArray minorArray = JSONArray.fromObject(majorObject.getString("sections"));
						for (int k = 0; k < minorArray.size(); k++) {
							JSONObject minorObject = minorArray.getJSONObject(k);
							int start = Integer.valueOf(minorObject.getString("value0"));
							int end = Integer.valueOf(minorObject.getString("value1"));
							if (tempminor >= start && tempminor <= end) {
								flag = true;
							}
						}
					}
					
				}
			}
		}
	
		return flag;
		
	}

	
}

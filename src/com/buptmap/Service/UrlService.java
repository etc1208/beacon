package com.buptmap.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.annotation.Resource;
import javax.faces.context.Flash;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.springframework.stereotype.Component;
import com.buptmap.DAO.MessageDao;
import com.buptmap.DAO.StaffDao;
import com.buptmap.DAO.Staff_devDAO;
import com.buptmap.model.Message;
import com.buptmap.model.Staff;
import com.buptmap.model.Staff_mes;
import com.buptmap.model.Vdev_mes_bind;
import com.buptmap.model.Vdevice;
import com.buptmap.util.WeChatAPI;

@Component
public class UrlService {
	private MessageDao messageDao;
	private Staff_devDAO staff_devDao;
	private StaffDao staffDao;
	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	
	public Map<String, String> getLogoUrl(String local_logo){
		Map<String,String> map = new HashMap<String,String>();			
		map = WeChatAPI.getLogoUrl(local_logo);
		return map;
	}
	public Map<String,Object> addMessage(String json){
		lock.writeLock().lock();
		try{
			Map<String,Object> map = new HashMap<String,Object>();
			//判断major、minor再添加
			System.out.println(json);
			JSONArray jsonArray = JSONArray.fromObject(json);
			JSONObject o = jsonArray.getJSONObject(0);
			String staff_id = o.getString("parent_id");
			boolean flag = staffDao.verify_session(json);
			//Staff_dev sd = this.staff_devDao.checkExist(staff_id, uuid, major, minor);	
			
			if( !flag ){
				// major、minor范围错误
				map.put("success", false);
				map.put("message", "权限不足，无法绑定此URL");
				return map;
			}else{
				JSONArray devices = this.formatDevices(json);
				
				if(devices == null){
					map.put("success", false);
					map.put("message", "该beacon号段存在未注册设备");
					return map;
				}
				
				Message m = new Message();
				String content = o.getString("content");
				String name = o.getString("name");
				String other_info = o.getString("other_info");
				String title = o.getString("title");
				JSONArray uuidArray = o.getJSONArray("session"); 
				JSONArray majorsArray = uuidArray.getJSONObject(0).getJSONArray("majors");
				String sessions = sessionsToString(majorsArray);
				String total = TotalOfSessions(majorsArray);
				
				// 判断权限并调用微信接口
				Staff staff = staffDao.getone(staff_id);
				if(staff == null){
					map.put("success", false);
					map.put("message", "权限不足，无法绑定此URL");
					return map;
				}else{
					if(o.containsKey("logo")) {
						m.setLogo(o.getString("logo"));
					}
					if(o.containsKey("logo_url")){
						m.setLogo_url(o.getString("logo_url"));
					}
					
					m.setContent(content);
					m.setName(name);
					m.setOther_info(other_info);
					m.setProject_id(o.getString("project_id"));
					m.setStart_time(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
					m.setEnd_time(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
					m.setTitle(title);
					m.setLast_modify_id(staff_id);
					//sessions，total字段
					//System.out.println("sessions:--->"+sessions);
					//System.out.println("total:--->"+total);
				
					m.setSessions(sessions);
					m.setTotal(total);
					
					//有审核权限
					if(staff.getManage().equals("1") ){
						//上传的是图片地址
						if(o.containsKey("logo")){
							Map<String,String> logoResult = WeChatAPI.getLogoUrl(o.getString("logo"));
							//logo接口成功
							if(logoResult.containsKey("url")){
								m.setLogo_url(logoResult.get("url"));			
								Map<String,String> pageResult = WeChatAPI.getPageId(devices, title, name, content, other_info,logoResult.get("url"));
								//page接口成功
								if(pageResult.containsKey("page_id")){
									m.setStatus("2");
									m.setPage_id(pageResult.get("page_id"));
								}else{
									m.setStatus("3");
									map.put("pageError", pageResult.get("addPageError"));
								}
							}else{
								m.setStatus("3");
								map.put("logoError", logoResult.get("addLogoError"));
							}
						}
						
						//上传的是服务器logo
						if(o.containsKey("logo_url")){
							Map<String,String> pageResult = WeChatAPI.getPageId(devices, title, name, content, other_info,o.getString("logo_url"));
							//page接口成功
							if(pageResult.containsKey("page_id")){
								m.setStatus("2");
								m.setPage_id(pageResult.get("page_id"));
							}else{
								m.setStatus("3");
								map.put("pageError", pageResult.get("addPageError"));
							}
						}
							
					//无审核权限	
					}else if(staff.getManage().equals("0")){
						m.setStatus("1");
					}
					
					// 若保存message时出错?
					messageDao.save(m);

					Vdev_mes_bind  dev_mes = new Vdev_mes_bind();

					
					for(int n = 0; n < devices.size();n++){
						dev_mes.setMessage_id(m.getId()+"");
						dev_mes.setUuid(devices.getJSONObject(n).getString("uuid"));
						dev_mes.setMinor(devices.getJSONObject(n).getString("minor"));
						dev_mes.setMajor(devices.getJSONObject(n).getString("major"));
						dev_mes.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
						//Lynn-16-01-13-判断uuid,major,minor是否存在，若存在,走更新，并记录message_id,更新sessions
						if(staff_devDao.updateDev_mes(dev_mes,staff_id))
						{
							
						}
						else {
							staff_devDao.saveDev_mes(dev_mes);
							
						}
						
					}	
					
					map.put("success", true);
					return map;
				}
			}
		}finally{
			lock.writeLock().unlock();
		}
		
	}
	
	public Map<String,Object> editMessage(String json){
		lock.writeLock().lock();
		try{
		Map<String,Object> map = new HashMap<String,Object>();
		JSONArray jsonArray = JSONArray.fromObject(json);
		JSONObject o = jsonArray.getJSONObject(0);
		Message m = this.messageDao.message_one(Integer.parseInt(o.getString("url_id")));
		if(m == null){
			map.put("success", false);
			map.put("message", "无此页面编号对应信息");
			return map;
		}
		String content = o.getString("content");
		String name = o.getString("name");
		String other_info = o.getString("other_info");
		String title = o.getString("title");
		String staff_id = o.getString("staff_id");
		
		// 判断权限并调用微信接口
		
		Staff staff = staffDao.getone(staff_id);
		if(staff == null){
			map.put("success", false);
			map.put("message", "权限不足，无法绑定此URL");
			return map;
		}else{
			//有审核权限
			if(staff.getManage().equals("1") ){
				Map<String,String> pageResult = new HashMap<String,String>();
				//上传的是图片
				if(o.containsKey("logo")){
					Map<String,String> logoResult = WeChatAPI.getLogoUrl(o.getString("logo"));
					//logo接口成功
					if(logoResult.containsKey("url")){
						m.setLogo_url(logoResult.get("url"));			
						//判断page_id是否为空，进行新增或是修改
						if(m.getPage_id() == null || m.getPage_id().equals("")){
							List<Vdev_mes_bind> temp= staff_devDao.selectByMessage(m.getId()+"");
							if(temp != null){ 
								JSONArray devices = new JSONArray();
								JSONObject device = new JSONObject();
								for(int i = 0; i < temp.size();i++){
									device.put("uuid", temp.get(i).getUuid());
									device.put("major", temp.get(i).getMajor());
									device.put("minor", temp.get(i).getMinor());
									devices.add(device);
								}
								pageResult = WeChatAPI.getPageId(devices,title,name, content, other_info, logoResult.get("url"));
								//page添加接口成功
								if(pageResult.containsKey("page_id")){
									m.setStatus("2");
									m.setPage_id(pageResult.get("page_id"));
								}else{
									m.setStatus("3");
									map.put("pageError", pageResult.get("addPageError"));
								}
							}
						}else{
							pageResult = WeChatAPI.editPage(m.getPage_id(), title, name, content, other_info, logoResult.get("url"));
							//page修改接口成功
							if(pageResult.containsKey("page_id")){
								m.setStatus("2");
							}else{
								m.setStatus("3");
								map.put("pageError", pageResult.get("editPageError"));
							}
						}
					}else{
						m.setStatus("3");
						map.put("logoError", logoResult.get("addLogoError"));
					}				
				}
				
				//上传的是服务器logo
				if(o.containsKey("logo_url")){
					//判断page_id是否为空，进行新增或是修改
					if(m.getPage_id() == null || m.getPage_id().equals("")){
						List<Vdev_mes_bind> temp= staff_devDao.selectByMessage(m.getId()+"");
						if(temp != null){ 
							JSONArray devices = new JSONArray();
							JSONObject device = new JSONObject();
							for(int i = 0; i < temp.size();i++){
								device.put("uuid", temp.get(i).getUuid());
								device.put("major", temp.get(i).getMajor());
								device.put("minor", temp.get(i).getMinor());
								devices.add(device);
							}
							pageResult = WeChatAPI.getPageId(devices,title,name, content, other_info, o.getString("logo_url"));
							//page添加接口成功
							if(pageResult.containsKey("page_id")){
								m.setStatus("2");
								m.setPage_id(pageResult.get("page_id"));
							}else{
								m.setStatus("3");
								map.put("pageError", pageResult.get("addPageError"));
							}
						}
					}else{
						pageResult = WeChatAPI.editPage(m.getPage_id(), title, name, content, other_info, o.getString("logo_url"));
						//page修改接口成功
						if(pageResult.containsKey("page_id")){
							m.setStatus("2");
						}else{
							m.setStatus("3");
							map.put("pageError", pageResult.get("editPageError"));
						}
					}
				}
				
				//无审核权限
			}else if(staff.getManage().equals("0")){
				m.setStatus("1");
			}
			
		}
		
		map.put("success", true);
		m.setContent(content);
		
		if(o.containsKey("logo")){
			m.setLogo(o.getString("logo"));
		}
		if(o.containsKey("logo_url")){
			m.setLogo_url(o.getString("logo_url"));
		}
		
		m.setName(name);
		m.setOther_info(other_info);
		m.setProject_id(o.getString("project_id"));
		m.setEnd_time(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
		m.setTitle(title);
		m.setLast_modify_id(staff_id);		
		
		 messageDao.update(m);
		 
		 return map;
		}finally{
			lock.writeLock().unlock();
		}
	}
	
	public Map<String,Object> adddeleteMessage(String json){
		lock.writeLock().lock();
		try {
			//message_id,page_id,parent_id,session,uuid,major,value0,value1
			
			Map<String,Object> map = new HashMap<String,Object>();
			List<Object[]>sObjects_oldList = new ArrayList<Object[]>();
			List<Object[]>sObjects_newList = new ArrayList<Object[]>();
			List<Object[]>sObjects_addList = new ArrayList<Object[]>();
			List<Object[]>sObjects_kongList = new ArrayList<Object[]>();
			List<Object[]>zhongList = new ArrayList<Object[]>();
			
			JSONArray jsonArray=JSONArray.fromObject(json);
			JSONObject jsonObject = jsonArray.getJSONObject(0);
			String mes_id = jsonObject.getString("message_id");
			String staff_id = jsonObject.getString("parent_id");
			String page_id = jsonObject.getString("page_id");
			JSONArray uuidArray = jsonObject.getJSONArray("session");
			
			if(!UrlHasDev(json))//先判断新的绑定是否为空，如果为空，直接走删除流程，置标志位 '4'
			{
				this.delete(Integer.valueOf(mes_id));
				map.put("success", true);
				map.put("description", "Link device success!");
				map.put("message", "全部删除");
				return map;	
			}
			else {
				 
				String uuid = uuidArray.getJSONObject(0).getString("value");			
				JSONArray majorsArray = uuidArray.getJSONObject(0).getJSONArray("majors");
				JSONObject sessionsObject = messageDao.getMesSessions(Integer.valueOf(mes_id));//major,value0,value1.
				JSONObject resultObject =new JSONObject();
				
				//求新增绑定
				sObjects_oldList = jons2obj(sessionsObject,uuid);
				sObjects_newList = jsons1obj(majorsArray, uuid);
				//map.put("目前sessions：", sObjects_oldList);
				//map.put("新sessions:", sObjects_newList);
				zhongList.addAll(sObjects_newList);//   = sObjects_newList;
				sObjects_addList = staffDao.NotInObject(zhongList, sObjects_oldList);//会对第一个参数进行修改
				List<Object[]>zhongList2=new ArrayList<Object[]>();
				zhongList2.addAll(sObjects_addList);
				JSONArray kongArray = staffDao.unused_dev(staff_id);
				List<Object[]>unuseList = new ArrayList<Object[]>();
					
				//考虑到多个major
				for(int i=0;i<kongArray.size()-1;i++)
				{
					String uuid2 = kongArray.getJSONObject(i).getString("uuid");
					JSONArray majors2Array = kongArray.getJSONObject(i).getJSONArray("major");
					sObjects_kongList = jsons1obj(majors2Array,uuid2);
					
					unuseList = staffDao.NotInObject(zhongList2, sObjects_kongList);
				}
				
				System.out.println("kongxian:"+unuseList.size());
				//map.put("新增绑定：", sObjects_addList);
				if((unuseList.size()==0||unuseList==null))
				{
					//对新增进行绑定
					//map.put("新增：", "kongxian");
					//map.put("新增绑定：", sObjects_addList);
					if(sObjects_addList!=null&&sObjects_addList.size()>0)
					{
						for (int i = 0; i < sObjects_addList.size(); i++) {
							JSONObject deviceObject = new JSONObject();
							deviceObject.put("uuid", sObjects_addList.get(i)[0]);
							deviceObject.put("major", sObjects_addList.get(i)[1]);
							deviceObject.put("minor", sObjects_addList.get(i)[2]);
							//System.out.println(deviceObject.getString("uuid")+" "+deviceObject.getString("major")+" "+deviceObject.getString("minor"));
							resultObject = WeChatAPI.addDeleteDevice(deviceObject, page_id);
							if(resultObject.getString("success").equals("false"))
							{
								map.put("success", false);
								map.put("description", resultObject.getString("description"));
								map.put("message", "您的新增绑定未成功！");
								return map;								
							}						
						}
						//我们的库里还要加绑定关系。
						if(resultObject.getString("success").equals("true"))
						{
							for (int i = 0; i < sObjects_addList.size(); i++) {
								JSONObject deviceObject = new JSONObject();
								deviceObject.put("uuid", sObjects_addList.get(i)[0]);
								deviceObject.put("major", sObjects_addList.get(i)[1]);
								deviceObject.put("minor", sObjects_addList.get(i)[2]);
								
								Vdev_mes_bind  dev_mes = new Vdev_mes_bind();
								dev_mes.setMessage_id(mes_id+"");
								dev_mes.setUuid(deviceObject.getString("uuid"));
								dev_mes.setMinor(deviceObject.getString("minor"));
								dev_mes.setMajor(deviceObject.getString("major"));
								dev_mes.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
								staff_devDao.saveDev_mes(dev_mes);
							}
						}						
					}									
				}
				else {
					map.put("success", false);
					map.put("message", "您的新增绑定不在您的空闲权限内，请确认后再进行绑定");
					return map;						
				}
				
				//求需要删除的绑定
				List<Object[]>zhongList3 =new ArrayList<Object[]>();
				zhongList3.addAll(sObjects_oldList);
				zhongList3= staffDao.NotInObject( zhongList3, sObjects_newList);
				//map.put("删除绑定：", zhongList3);
				if(zhongList3!=null&&zhongList3.size()>0)
				{
					for (int i = 0; i < zhongList3.size(); i++) {
						JSONObject deviceObject = new JSONObject();
						deviceObject.put("uuid", zhongList3.get(i)[0]);
						deviceObject.put("major", zhongList3.get(i)[1]);
						deviceObject.put("minor", zhongList3.get(i)[2]);
						resultObject = WeChatAPI.addDeleteDevice(deviceObject, null);
						if(resultObject.getString("success").equals("false"))
						{
							map.put("success", false);
							map.put("description", resultObject.getString("description"));
							map.put("message", "您的新增绑定未成功！");
							return map;								
						}
						
					}
					
					if(resultObject.getString("success").equals("true"))
					{
						for (int i = 0; i < zhongList3.size(); i++) {
							JSONObject deviceObject = new JSONObject();
							deviceObject.put("uuid", zhongList3.get(i)[0]);
							deviceObject.put("major", zhongList3.get(i)[1]);
							deviceObject.put("minor", zhongList3.get(i)[2]);
							
							 // 删除Dev_mes绑定表
							Vdev_mes_bind  dev_mes = new Vdev_mes_bind();
							dev_mes.setMessage_id(mes_id+"");
							dev_mes.setUuid(deviceObject.getString("uuid"));
							dev_mes.setMinor(deviceObject.getString("minor"));
							dev_mes.setMajor(deviceObject.getString("major"));
							staff_devDao.deleteDev_mes(dev_mes);
							
						}
						
						
					}					
				}
				
				//message表中进行修改sessions字段
				Message m = messageDao.message_one(Integer.parseInt(mes_id));
				String sessions = sessionsToString(majorsArray);
				String total = TotalOfSessions(majorsArray);
				System.out.println("adddelete-->sessions:"+sessions+"//total:"+total);
				m.setSessions(sessions);
				m.setTotal(total);
				messageDao.update(m);
				
				
				map.put("success", true);
				//map.put("description", "Link device success!");
				map.put("message", "您的增减绑定成功！");
				return map;	
			}
			
					
		}
		finally{
			lock.writeLock().unlock();
		}
	}
	public boolean UrlHasDev(String json)
	{
		JSONArray jsonArray=JSONArray.fromObject(json);
		JSONObject jsonObject = jsonArray.getJSONObject(0);
		JSONArray uuidArray = jsonObject.getJSONArray("session"); 
		if(uuidArray==null||uuidArray.size()==0)
			return false;
		//很有可能下面的代码就不用了，暂不注销
		String uuid = uuidArray.getJSONObject(0).getString("value");	
		if(uuid.equals("")||uuid==null)
		{
			return false;			
		}
		else {
			JSONArray majorsArray = uuidArray.getJSONObject(0).getJSONArray("majors");
			String major = majorsArray.getJSONObject(0).getString("value");
			if(major.equals("")||major==null)
			{
				return false;
			}
			else {
				JSONArray minorsArray = majorsArray.getJSONObject(0).getJSONArray("sections");
				String v0 = minorsArray.getJSONObject(0).getString("value0");
				String v1 = minorsArray.getJSONObject(0).getString("value1");
				if(v0.equals("")||v0==null||v1.equals("")||v1==null)
				{
					return false;
				}
				else {
					int start = Integer.parseInt(v0);
					int end = Integer.parseInt(v1);
					if(start>end)
					{
						return false;
					}
				}
				
			}
			
		}
		return true;
	}
	
	public List<Object[]>jsons1obj(JSONArray majorsArray ,String uuid)
	{
		//System.out.println("js1-->size:"+majorsArray.size());
		List<Object[]> objectList = new ArrayList<Object[]>();
		for(int i=0;i<majorsArray.size();i++)
		{
			JSONObject majorsObject = majorsArray.getJSONObject(i);
			String major = majorsObject.getString("value");
			JSONArray minorArray = majorsObject.getJSONArray("sections");
			//JSONArray minorArray = (majorsObject.getJSONArray("sections")!=null)?(majorsObject.getJSONArray("sections")):(majorsObject.getJSONArray("minor"));
			for(int k=0; k<minorArray.size(); k++){
					int start2 = Integer.parseInt(minorArray.getJSONObject(k).getString("value0"));
					int end2 = Integer.parseInt(minorArray.getJSONObject(k).getString("value1")); 
					//System.out.println("js1-->start:"+start2+". end:"+end2);
					for(int j = start2;j<=end2;j++)
					{
						//System.out.println("j:"+j);
						Object[] tempObject = new Object[3];
						tempObject[0]=new String( uuid);
						tempObject[1]=new String( major );
						tempObject[2]=new String( String.valueOf(j));
						objectList.add(tempObject);
					}
			}
			
		}
		
		return objectList;
	}
	
	public List<Object[]> jons2obj(JSONObject sessionsObject,String uuid)
	{

		//System.out.println("size:"+sessionsObject.size());
		List<Object[]> objectList = new ArrayList<Object[]>();
		Iterator it = sessionsObject.keys();
		while (it.hasNext()) {			
			String key = it.next().toString();
			System.out.println(key);
			String minorString = sessionsObject.getString(key);	
			JSONArray minorJsonArray = JSONArray.fromObject(minorString);
			
			
			for(int i = 0;i<minorJsonArray.size();i++)
			{
				JSONObject minorObject =minorJsonArray.getJSONObject(i);
				int start = Integer.valueOf( minorObject.getString("value0") );
				int end = Integer.valueOf( minorObject.getString("value1") );
				//System.out.println("start:"+start+". end:"+end);
				for(int j=start;j<=end;j++)
				{
					Object[] tempObject = new Object[3];
					tempObject[0]=new String( uuid);
					tempObject[1]=new String( key );
					tempObject[2]=new String( String.valueOf(j));
					objectList.add(tempObject);
				}				
			}			
			
		}
		
		return objectList;
	}
	
	public boolean checkunuse(String json){
		//message_id,uuid,major,value0,value1
		boolean flag = staffDao.verify_session(json);
		/*
		JSONArray jsonArray = JSONArray.fromObject(json);
		JSONObject jsonObject = jsonArray.getJSONObject(0);
		String staff_id = jsonObject.getString("parent_id");
		JSONArray seArray = jsonObject.getJSONArray("session");
		JSONObject seObject = seArray.getJSONObject(0);
		String uuid1 = seObject.getString("value");
		JSONArray majors1Array = seObject.getJSONArray("majors");
		*/
		
		if( flag ){
			return true;
		}		
		
		return false;
	}
	
	public JSONArray checkDevice(String json){
		JSONArray result = new JSONArray();
		JSONObject deviceObj = new JSONObject();
		List<Vdev_mes_bind> devices = this.getDevices(json);
		if(devices != null && devices.size() > 0){
			for(int d = 0; d < devices.size(); d++){
				deviceObj.put("major", devices.get(d).getMajor());
				deviceObj.put("minor", devices.get(d).getMinor());
				result.add(deviceObj);
			}
		}
		return result;
	}
	
	public boolean checkMessage(String json){
		JSONArray jsonArray = JSONArray.fromObject(json);
		JSONObject obj = jsonArray.getJSONObject(0);
		String url = obj.getString("url");
		List<Message> messages = messageDao.checkUrl(url);
		if( messages != null ){
			return messages.size() > 0 ? false:true;
		}else{
			return true;
		}
	}

	public JSONObject delete(int message_id){
		List<Vdev_mes_bind> temp= staff_devDao.selectByMessage(message_id+"");
		JSONObject result = new JSONObject();
		Message m = this.messageDao.message_one(message_id);
		if(temp != null && m != null){
			if(m.getPage_id() != null && !m.getPage_id().equals("")){
				result = WeChatAPI.deletePage(temp,m.getPage_id());
				if(result.getBoolean("page_message") && result.getBoolean("device_message")){
					m.setStatus("4");
					messageDao.update(m);
				}
			}else{
				m.setStatus("4");
				messageDao.update(m);
				result.put("page_message", true);
				result.put("device_message", true);
				result.put("description", "Success");
			}
			return result;
		}else{
			return null;
		}
		
	}
	
	/*Lynn 15-08-31 条件搜索URL*/	
	
	public JSONArray showList2(String staff_id,String jsonString) {
		lock.writeLock().lock();
		
		try{
			
			//判断条件
			String sqlcon = "";
			if (jsonString!=null && !jsonString.equals("") ) {
				JSONObject jObject=JSONObject.fromObject(jsonString);
				if (jObject.has("title")&&jObject.getString("title")!=null&&!jObject.getString("title").equals("")) {
					sqlcon +=" and title like '%"+jObject.getString("title")+"%' ";
				}
				if (jObject.has("name")&&jObject.getString("name")!=null&&!jObject.getString("name").equals("")) {
					sqlcon +=" and name like '%"+jObject.getString("name")+"%' ";
				}
				if (jObject.has("pr_title")&&jObject.getString("pr_title")!=null&&!jObject.getString("pr_title").equals("")) {
					sqlcon +=" and pr_title like '%"+jObject.getString("pr_title")+"%' ";
				}
				if (jObject.has("major")&&jObject.getString("major")!=null&&!jObject.getString("major").equals("")) {
					sqlcon +=" and major = '"+jObject.getString("major")+"' ";
				}
				if (jObject.has("minor")&&jObject.getString("minor")!=null&&!jObject.getString("minor").equals("")) {
					sqlcon +=" and minor = '"+jObject.getString("minor")+"' ";
				}
				if (jObject.has("status")&&jObject.getString("status")!=null&&!jObject.getString("status").equals("")) {
					sqlcon +=" and status = '"+jObject.getString("status")+"' ";
				}
				if (jObject.has("end_time")&&jObject.getString("end_time")!=null&&!jObject.getString("end_time").equals("")) {
					sqlcon +=" and end_time <= '"+jObject.getString("end_time")+"' ";
				}
				if (jObject.has("otherInfo")&&jObject.getString("otherInfo")!=null&&!jObject.getString("otherInfo").equals("")) {
					sqlcon +=" and other_info like '%"+jObject.getString("otherInfo")+"%' ";
				}
				
			} else {
				//sqlcon="";

			}
			System.out.println("sqlcon:"+sqlcon);
			
			JSONArray result = messageDao.message_list_con(staff_id,sqlcon);//lynn 改
			
			JSONArray jsonArray = new JSONArray();
			JSONObject jsonObject = new JSONObject();		
    			if (result != null) {
    				for (int i = 0; i < result.size(); i++) {
    					JSONObject tempObj = result.getJSONObject(i);
    					/*
    					JSONObject majorResult = new JSONObject();
    					JSONObject majorObject = new JSONObject();
    					
						List<Integer> minors = new ArrayList<Integer>(); 
						minors.add(Integer.parseInt(tempObj.getString("minor")));
						majorObject.put(tempObj.getString("major"), minors);
    					
    					//去除重复的message，还是用sql distinct去重效率更好(类似detailWithSession)?
    					for(int j= i+1;j < result.size();j++){
    						if(result.getJSONObject(j).getInt("message_id") == tempObj.getInt("message_id")){
    							if(majorObject.containsKey(result.getJSONObject(j).getString("major"))){
    								List<Integer> tempMi = (List<Integer>) majorObject.get(result.getJSONObject(j).getString("major"));
    								tempMi.add(Integer.parseInt(result.getJSONObject(j).getString("minor")));
    							}else{
    								List<Integer> tempMi2 = new ArrayList<Integer>(); 
    								tempMi2.add(Integer.parseInt(result.getJSONObject(j).getString("minor")));
    								majorObject.put(result.getJSONObject(j).getString("major"), tempMi2);
    							}
    							result.remove(j);
    							j--;
    						}
    					}					
    					
    					
    					for(Object key : majorObject.keySet()){
    						JSONObject minorObject = new JSONObject();
        					JSONArray minorArray = new JSONArray();
        					
        					//前提是数据库中取出的minor排序正确，否则需要对minorT重新排序
    						List<Integer> minorT =(List<Integer>) majorObject.get(key);
    						if(minorT != null){
        						if(minorT.size() == 1){
        							minorObject.put("value0", minorT.get(0));
        							minorObject.put("value1", minorT.get(0));
        							minorArray.add(minorObject);
        						}else{
        							int m = 0; int n = 1;
        							for( ; n < minorT.size(); n++){
        								if(minorT.get(n) - minorT.get(n-1) > 1){
        									minorObject.put("value0", minorT.get(m));
        									minorObject.put("value1", minorT.get(n-1));
        									m = n;
        									minorArray.add(minorObject);
        								}
        							}
        							minorObject.put("value0", minorT.get(m));
        							minorObject.put("value1", minorT.get(n-1));
        							minorArray.add(minorObject);
        						}
        					}

    						majorResult.put(key, minorArray);
    						
    						minorObject = null;
    						minorArray = null;
						}
						*/
    					jsonObject.put("message_id", tempObj.getInt("message_id"));
    					jsonObject.put("title", tempObj.getString("title"));
    					jsonObject.put("name", tempObj.getString("name"));
    					jsonObject.put("content",tempObj.getString("content"));
    					jsonObject.put("project_title",tempObj.getString("project_title"));
    					jsonObject.put("end_time",tempObj.getString("end_time"));
    					jsonObject.put("status", tempObj.getString("status"));
    					jsonObject.put("logo_url", tempObj.getString("logo_url"));
    					jsonObject.put("page_id", tempObj.getString("page_id"));
    					jsonObject.put("project_id", tempObj.getString("project_id"));
    					jsonObject.put("other_info", tempObj.getString("other_info"));
    					jsonObject.put("sessions", tempObj.getString("sessions"));
    					jsonObject.put("total", tempObj.getString("total"));
    					//jsonObject.put("sessions",majorResult);
    					jsonArray.add(jsonObject);
    					
    					/*majorResult = null;
    					minors = null;
    					majorObject = null;*/
    				}
    				return jsonArray;
    			}else{
    				return null;
    			}
		}finally{
			lock.writeLock().unlock();
		}
	}
	
	/**
	 * @author yh
	 */
	public Integer getTotalNumber(String staff_id){
		int totalNumber = 0;
		try {
		    totalNumber = messageDao.getTotalNumber(staff_id);			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return totalNumber;
	}
	
	
	public JSONArray showList(String staff_id) {
		lock.writeLock().lock();
		try{
			JSONArray result = messageDao.message_list(staff_id);
			JSONArray jsonArray = new JSONArray();
			JSONObject jsonObject = new JSONObject();			
			JSONObject majorObject = new JSONObject();
    			if (result != null) {
    				for (int i = 0; i < result.size(); i++) {
    					JSONArray majorsArray = new JSONArray();
    					JSONObject tempMessage = result.getJSONObject(i);
    					for(int j= i+1;j < result.size();j++){
    						if(result.getJSONObject(j).getInt("message_id") ==  tempMessage.getInt("message_id")){
    							majorObject.put("major", result.getJSONObject(j).getString("major"));
    							majorObject.put("minor", result.getJSONObject(j).getString("minor"));
    							majorsArray.add(majorObject);
    							result.remove(j);
    							j--;
    						}
    					}
    					
    					majorObject.put("major", tempMessage.getString("major"));
    					majorObject.put("minor", tempMessage.getString("minor"));
						majorsArray.add(majorObject);
    							
						jsonObject.put("message_id", tempMessage.getInt("message_id"));
    					jsonObject.put("title", tempMessage.getString("title"));
    					jsonObject.put("name", tempMessage.getString("name"));
    					jsonObject.put("content",tempMessage.getString("content"));
    					jsonObject.put("project_title",tempMessage.getString("project_title"));
    					jsonObject.put("end_time",tempMessage.getString("end_time"));
    					jsonObject.put("status", tempMessage.getString("status"));
    					jsonObject.put("logo_url", tempMessage.getString("logo_url"));
    					jsonObject.put("page_id", tempMessage.getString("page_id"));
    					jsonObject.put("project_id", tempMessage.getString("project_id"));
    					jsonObject.put("other_info", tempMessage.getString("other_info"));
    					jsonObject.put("majors", majorsArray);
    					jsonArray.add(jsonObject);
    					
    					majorsArray = null;
    					tempMessage = null;
    				}
    				return jsonArray;
    			}else{
    				return null;
    			}
		}finally{
			lock.writeLock().unlock();
		}
	}
	
	
	public JSONArray showListWithSession(String staff_id,int flag) {//Lynn添加分页
		lock.writeLock().lock();
		
		try{
			//JSONArray result = messageDao.message_list(staff_id);
			JSONArray result = messageDao.message_page_list(staff_id,flag);
			JSONArray jsonArray = new JSONArray();
			JSONObject jsonObject = new JSONObject();		
    			if (result != null) {
    				for (int i = 0; i < result.size(); i++) {
    					JSONObject tempObj = result.getJSONObject(i);
    					
    					/*151123-Lynn-用字段替换计算
    					JSONObject majorResult = new JSONObject();
    					JSONObject majorObject = new JSONObject();
    					
						List<Integer> minors = new ArrayList<Integer>(); 
						minors.add(Integer.parseInt(tempObj.getString("minor")));
						majorObject.put(tempObj.getString("major"), minors);
    					
    					//去除重复的message，还是用sql distinct去重效率更好(类似detailWithSession)?
    					for(int j= i+1;j < result.size();j++){
    						if(result.getJSONObject(j).getInt("message_id") == tempObj.getInt("message_id")){
    							if(majorObject.containsKey(result.getJSONObject(j).getString("major"))){
    								List<Integer> tempMi = (List<Integer>) majorObject.get(result.getJSONObject(j).getString("major"));
    								tempMi.add(Integer.parseInt(result.getJSONObject(j).getString("minor")));
    							}else{
    								List<Integer> tempMi2 = new ArrayList<Integer>(); 
    								tempMi2.add(Integer.parseInt(result.getJSONObject(j).getString("minor")));
    								majorObject.put(result.getJSONObject(j).getString("major"), tempMi2);
    							}
    							result.remove(j);
    							j--;
    						}
    					}					
    					
    					int totalBeacon = 0;
    					for(Object key : majorObject.keySet()){
    						JSONObject minorObject = new JSONObject();
        					JSONArray minorArray = new JSONArray();
        					
        					//前提是数据库中取出的minor排序正确，否则需要对minorT重新排序
    						List<Integer> minorT =(List<Integer>) majorObject.get(key);
    						if(minorT != null){
        						if(minorT.size() == 1){
        							minorObject.put("value0", minorT.get(0));
        							minorObject.put("value1", minorT.get(0));
        							minorArray.add(minorObject);
        						}else{
        							int m = 0; int n = 1;
        							for( ; n < minorT.size(); n++){
        								if(minorT.get(n) - minorT.get(n-1) > 1){
        									minorObject.put("value0", minorT.get(m));
        									minorObject.put("value1", minorT.get(n-1));
        									m = n;
        									minorArray.add(minorObject);
        								}
        							}
        							minorObject.put("value0", minorT.get(m));
        							minorObject.put("value1", minorT.get(n-1));
        							minorArray.add(minorObject);
        						}
        						totalBeacon += minorT.size();
        					}

    						majorResult.put(key, minorArray);
    						
    						minorObject = null;
    						minorArray = null;
						}
    					*/
    					//System.out.println("------------打印session字符串"+majorResult);
    					//System.out.println("------------打印session总数"+totalBeacon);
    					//messageDao.addSessionAndTotal(tempObj.getInt("message_id"),majorResult.toString(),totalBeacon);
    					jsonObject.put("message_id", tempObj.getInt("message_id"));
    					jsonObject.put("title", tempObj.getString("title"));
    					jsonObject.put("name", tempObj.getString("name"));
    					jsonObject.put("content",tempObj.getString("content"));
    					jsonObject.put("project_title",tempObj.getString("project_title"));
    					jsonObject.put("end_time",tempObj.getString("end_time"));
    					jsonObject.put("status", tempObj.getString("status"));
    					jsonObject.put("logo_url", tempObj.getString("logo_url"));
    					jsonObject.put("page_id", tempObj.getString("page_id"));
    					jsonObject.put("project_id", tempObj.getString("project_id"));
    					jsonObject.put("other_info", tempObj.getString("other_info"));
    					jsonObject.put("sessions", tempObj.getString("sessions"));
    					jsonObject.put("total", tempObj.getString("total"));
    					//jsonObject.put("sessions",majorResult);
    					jsonArray.add(jsonObject);
    					
    					//majorResult = null;
    					//minors = null;
    					//majorObject = null;
    				}
    				return jsonArray;
    			}else{
    				return null;
    			}
		}finally{
			lock.writeLock().unlock();
		}
	}
	
	
	public JSONArray showDeList(int message_id,String staff_id) {
		lock.writeLock().lock();
		try{
			List<Staff_mes> result = messageDao.message_deList(message_id,staff_id);
			JSONArray jsonArray = new JSONArray();
			JSONArray sessionArray = new JSONArray();
			JSONObject jsonObject = new JSONObject();			
			JSONObject sessionObject = new JSONObject();
			Staff_mes tempMessage = new Staff_mes();
			if(result != null  ){
				for(int i = 0; i < result.size(); i++){
					tempMessage = result.get(i);
					sessionObject.put("uuid", tempMessage.getUuid()==null?"":tempMessage.getUuid());
					sessionObject.put("major", tempMessage.getMajor()==null?"":tempMessage.getMajor());
					sessionObject.put("minor", tempMessage.getMinor()==null?"":tempMessage.getMinor());
					sessionArray.add(sessionObject);
				}
				
				tempMessage = result.get(0);
				jsonObject.put("message_id", tempMessage.getMessage_id());
				jsonObject.put("title", tempMessage.getTitle()==null?"":tempMessage.getTitle());
				jsonObject.put("name", tempMessage.getName()==null?"":tempMessage.getName());
				jsonObject.put("content", tempMessage.getContent()==null?"":tempMessage.getContent());
				jsonObject.put("start_time", tempMessage.getStart_time()==null?"":tempMessage.getStart_time());
				jsonObject.put("logo", tempMessage.getLogo()==null?"":tempMessage.getLogo());	
				jsonObject.put("status", tempMessage.getStatus()==null?"":tempMessage.getStatus());
				jsonObject.put("other_info", tempMessage.getOther_info()==null?"":tempMessage.getOther_info());
				jsonObject.put("page_id", tempMessage.getPage_id()==null?"":tempMessage.getPage_id());
				jsonObject.put("project_id", tempMessage.getProject_id()==null?"":tempMessage.getProject_id());
				jsonObject.put("last_modify_id", tempMessage.getLast_modify_id()==null?"":tempMessage.getLast_modify_id());
				jsonObject.put("project_title", tempMessage.getPr_title()==null?"":tempMessage.getPr_title());
				jsonObject.put("end_time", tempMessage.getEnd_time()==null?"":tempMessage.getEnd_time());
				jsonObject.put("logo_url", tempMessage.getLogo_url()==null?"":tempMessage.getLogo_url());
				jsonObject.put("staff_id", tempMessage.getStaff_id()==null?"":tempMessage.getStaff_id());				
				jsonObject.put("pr_begin_time", tempMessage.getPr_begin_time()==null?"":tempMessage.getPr_begin_time());
				jsonObject.put("pr_end_time", tempMessage.getPr_end_time()==null?"":tempMessage.getPr_end_time());
				jsonObject.put("sessions", sessionArray);
				jsonArray.add(jsonObject);
				return jsonArray;
			}
			return null;
		}finally{
			lock.writeLock().unlock();
		}
	}
	
	
	public JSONArray showDetailWithSession(int message_id,String staff_id) {
		lock.writeLock().lock();
		try{
			List<Staff_mes> result = messageDao.message_deList(message_id,staff_id);
			JSONArray jsonArray = new JSONArray();
			
			JSONObject jsonObject = new JSONObject();			
			Staff_mes tempMessage = new Staff_mes();
			
			if(result != null) {	
				
			tempMessage = result.get(0);
			jsonObject.put("message_id", tempMessage.getMessage_id());
			jsonObject.put("title", tempMessage.getTitle()==null?"":tempMessage.getTitle());
			jsonObject.put("name", tempMessage.getName()==null?"":tempMessage.getName());
			jsonObject.put("content", tempMessage.getContent()==null?"":tempMessage.getContent());
			jsonObject.put("start_time", tempMessage.getStart_time()==null?"":tempMessage.getStart_time());
			jsonObject.put("logo", tempMessage.getLogo()==null?"":tempMessage.getLogo());	
			jsonObject.put("status", tempMessage.getStatus()==null?"":tempMessage.getStatus());
			jsonObject.put("other_info", tempMessage.getOther_info()==null?"":tempMessage.getOther_info());
			jsonObject.put("page_id", tempMessage.getPage_id()==null?"":tempMessage.getPage_id());
			jsonObject.put("project_id", tempMessage.getProject_id()==null?"":tempMessage.getProject_id());
			jsonObject.put("last_modify_id", tempMessage.getLast_modify_id()==null?"":tempMessage.getLast_modify_id());
			jsonObject.put("project_title", tempMessage.getPr_title()==null?"":tempMessage.getPr_title());
			jsonObject.put("end_time", tempMessage.getEnd_time()==null?"":tempMessage.getEnd_time());
			jsonObject.put("logo_url", tempMessage.getLogo_url()==null?"":tempMessage.getLogo_url());
			jsonObject.put("staff_id", tempMessage.getStaff_id()==null?"":tempMessage.getStaff_id());				
			jsonObject.put("pr_begin_time", tempMessage.getPr_begin_time()==null?"":tempMessage.getPr_begin_time());
			jsonObject.put("pr_end_time", tempMessage.getPr_end_time()==null?"":tempMessage.getPr_end_time());
			jsonObject.put("sessions", this.getUUIDs(message_id, staff_id));
			//Lynn 加资源对应的iBeacon设备个数
			jsonObject.put("devices", messageDao.findsessiondev(getUUIDs(message_id, staff_id)));
			jsonArray.add(jsonObject);
			return jsonArray;
			}
			
			return null;
		}finally{
			lock.writeLock().unlock();
		}
	}
	
	//
	
	
	public JSONArray getUUIDs(int message_id,String staff_id){
		JSONArray sessionArray = new JSONArray();
		JSONObject sessionObject = new JSONObject();
		
		List<String> uuids = staff_devDao.distinctUUID(message_id, staff_id);
		if(uuids != null){
			for(int i = 0; i < uuids.size(); i++){
				sessionObject.put("uuid", uuids.get(i));
				sessionObject.put("majors", getMajors(message_id,staff_id,uuids.get(i)));
				sessionArray.add(sessionObject);
			}
		}
		
		return sessionArray;
	}
	
	public JSONArray getMajors(int message_id,String staff_id,String uuid){
		JSONArray majorArray = new JSONArray();
		JSONObject majorObject = new JSONObject();
		
		List<String> majors = staff_devDao.distinctMajor(message_id, staff_id, uuid);
		if(majors != null){
			for(int i = 0; i < majors.size(); i++){
				majorObject.put("major", majors.get(i));
				majorObject.put("minors", this.getMinors(message_id, staff_id, uuid, majors.get(i)));
				majorArray.add(majorObject);
			}
		}
		return majorArray;
	}
	
	public JSONArray getMinors(int message_id,String staff_id,String uuid,String major){
		JSONArray minorArray = new JSONArray();
		JSONObject minorObject = new JSONObject();
		
		int[] minors = staff_devDao.findMinors(message_id, staff_id, uuid, major);
		if(minors != null){
			Arrays.sort(minors);
			if(minors.length == 1){
				minorObject.put("value0", minors[0]);
				minorObject.put("value1", minors[0]);
				minorArray.add(minorObject);
			}else{
				int m = 0; int n = 1;
				for( ; n < minors.length; n++){
					if(minors[n] - minors[n-1] > 1){
						minorObject.put("value0", minors[m]);
						minorObject.put("value1", minors[n-1]);
						m = n;
						minorArray.add(minorObject);
					}
				}
				minorObject.put("value0", minors[m]);
				minorObject.put("value1", minors[n-1]);
				minorArray.add(minorObject);
			}
		}
		
		return minorArray;
	}
	
	
	
	/**
	 * 
	 * @param json
	 * @return 根据传入的session字段取出所有在该范围内的devices
	 */
	private List<Vdev_mes_bind> getDevices(String json){
		List<Vdev_mes_bind> total = new ArrayList<Vdev_mes_bind>();
		List<Vdev_mes_bind> temp = new ArrayList<Vdev_mes_bind>();
		JSONArray jsonArray = JSONArray.fromObject(json);
		JSONObject jsonObject = jsonArray.getJSONObject(0);
		JSONArray uuidArray = jsonObject.getJSONArray("session");
		
		String uuid = null;
		String major = null;
		String start = null;
		String end = null;
		for(int i = 0; i<uuidArray.size();i++){
			
			uuid = uuidArray.getJSONObject(i).getString("value");
			JSONArray majorArray = uuidArray.getJSONObject(i).getJSONArray("majors");
			
			for(int j=0; j<majorArray.size();j++){
				major = majorArray.getJSONObject(j).getString("value");
				JSONArray minorArray = majorArray.getJSONObject(j).getJSONArray("sections");
				
				for(int k=0; k<minorArray.size(); k++){
					start = minorArray.getJSONObject(k).getString("value0");
					end = minorArray.getJSONObject(k).getString("value1");
					temp = staff_devDao.checkMinors(uuid, major, start, end);
					if(temp != null){
						total.addAll(temp);
					}
				}
			}	
			
		}
		
		/*String[] device_ids = new String[total.size()];
		for(int l=0;l<total.size();l++){
			device_ids[l] = total.get(l).getVdevice_id();
		}
		return device_ids;*/
		return total;
	}
	
	
	/**
	 * @param majorsArray
	 * @author buptLynn
	 * @return 根据传入的major对应的JSONArray，返回对应的minor个数
	 * 
	 */
	 
	public String TotalOfSessions(JSONArray majorsArray)
	{
		int total=0,start=0,end=0;
		for(int i=0;i<majorsArray.size();i++)
		{
			JSONArray minorArray = majorsArray.getJSONObject(i).getJSONArray("sections");
			for(int j=0;j<minorArray.size();j++)
			{
				start = Integer.parseInt(minorArray.getJSONObject(j).getString("value0"));
				end = Integer.parseInt(minorArray.getJSONObject(j).getString("value1"));
				total +=(end-start+1);
			}
		}		
		
		return String.valueOf(total);
	}
	
	public String urlTotalOfSessions(JSONArray majorsArray)
	{
		int total=0,start=0,end=0;
		for(int i=0;i<majorsArray.size();i++)
		{
			JSONArray minorArray = majorsArray.getJSONObject(i).getJSONArray("minors");
			for(int j=0;j<minorArray.size();j++)
			{
				start = Integer.parseInt(minorArray.getJSONObject(j).getString("value0"));
				end = Integer.parseInt(minorArray.getJSONObject(j).getString("value1"));
				total +=(end-start+1);
			}
		}		
		
		return String.valueOf(total);
	}
	/**
	 * @author buptLynn
	 * @param majorsArray
	 * @return 根据传入的major对应的JSONArray，返回message存储的sessions字段形式
	 * 
	 * */
	
	public String sessionsToString(JSONArray majorsArray)
	{
		String result="",majorString="",minorString="";
		JSONObject jsonObject = new JSONObject();
		for (int i = 0; i < majorsArray.size(); i++) {
			majorString = majorsArray.getJSONObject(i).getString("value");
			minorString = majorsArray.getJSONObject(i).getString("sections");
			jsonObject.put(majorString, minorString);
			
		}
		result+=jsonObject.toString();
				
		return result;
	}
	
	public String urlsessionsToString(JSONArray majorsArray)
	{
		String result="",majorString="",minorString="";
		JSONObject jsonObject = new JSONObject();
		for (int i = 0; i < majorsArray.size(); i++) {
			majorString = majorsArray.getJSONObject(i).getString("major");
			minorString = majorsArray.getJSONObject(i).getString("minors");
			jsonObject.put(majorString, minorString);
			result+=jsonObject.toString();
		}
				
		return result;
	}
	
	/**
	 * 
	 * @param json
	 * @return 根据传入的session字段取出所有在该范围内的device,组装成JSONArray
	 */
	private JSONArray formatDevices(String json){
	
		JSONArray jsonArray = JSONArray.fromObject(json);
		JSONObject jsonObject = jsonArray.getJSONObject(0);
		JSONArray uuidArray = jsonObject.getJSONArray("session");
		
		String uuid = null;
		String major = null;
		int start = 0;
		int end = 0;
		JSONArray total = new JSONArray();
		JSONObject temp = new JSONObject();
		List<Vdevice> devTemp = new ArrayList<Vdevice>();
		
		for(int i = 0; i<uuidArray.size();i++){
			
			uuid = uuidArray.getJSONObject(i).getString("value");
			JSONArray majorArray = uuidArray.getJSONObject(i).getJSONArray("majors");
			
			for(int j=0; j<majorArray.size();j++){
				major = majorArray.getJSONObject(j).getString("value");
				JSONArray minorArray = majorArray.getJSONObject(j).getJSONArray("sections");
				
				for(int k=0; k<minorArray.size(); k++){
					start = Integer.parseInt(minorArray.getJSONObject(k).getString("value0"));
					end = Integer.parseInt(minorArray.getJSONObject(k).getString("value1"));
					//验证号段有对应的device_id
					devTemp = staff_devDao.checkDeviceId(uuid, major, start+"", end+"");
					if(devTemp != null&&devTemp.size() > 0){
						return null;
					}
					
					while(start <= end){
							temp.put("uuid", uuid);
							temp.put("major", major);
							temp.put("minor", start+"");
							total.add(temp);
							start++;
					}
				}
			}	
			
		}
		return total;
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

	
}

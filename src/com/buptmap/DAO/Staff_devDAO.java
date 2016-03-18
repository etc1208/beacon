package com.buptmap.DAO;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import net.sf.json.JSONArray;

import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;

import com.buptmap.Service.UrlService;
import com.buptmap.model.Mes_dev;
import com.buptmap.model.Message;
import com.buptmap.model.Staff_dev;
import com.buptmap.model.Vdev_mes_bind;
import com.buptmap.model.Vdevice;
/**
 * 
 * @author weiier
 *		methods be used when manage URL 
 */
@Component
public class Staff_devDAO {
	private MessageDao messageDao;
	private UrlService urlService;
	private HibernateTemplate hibernateTemplate;
	private List<String> temp;
	public Staff_dev checkExist( String staff_id , String uuid, String major,String minor) {
		List<Staff_dev> result = new ArrayList<Staff_dev>();		
		Staff_dev temp = new Staff_dev();
		try {
			result = hibernateTemplate.find("from Staff_dev where staff_id='"+staff_id+"' and uuid='"+uuid
					+"' and major='"+major+"' and minor='"+minor+"'");
			if (result != null && result.size() > 0) {		
				temp	 = result.get(0);
				return temp;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public List<Vdevice> selectByminors(String uuid,String major,String start,String end){
		List<Vdevice> result = new ArrayList<Vdevice>();
		result = hibernateTemplate.find("from Vdevice where uuid='"+uuid+"' and major='"+major+"' and minor between '"+start+"' and '"+end+"'");
		if(result != null && result.size() > 0){
			return result;
		}else{
			return null;
		}
	}
	
	public List<Vdev_mes_bind> checkMinors(String uuid,String major,String start,String end){
		List<Vdev_mes_bind> result = new ArrayList<Vdev_mes_bind>();
		result = hibernateTemplate.find("from Vdev_mes_bind where uuid='"+uuid+"' and major='"+major+"' and minor between '"+start+"' and '"+end+"'");
		if(result != null && result.size() > 0){
			return result;
		}else{
			return null;
		}
	}
	
	public List<Vdevice> checkDeviceId(String uuid,String major,String start,String end){
		List<Vdevice> result = new ArrayList<Vdevice>();
		result = hibernateTemplate.find("from Vdevice where uuid='"+uuid+"' and major='"+major
				+"' and vdevice_id='-1' and minor between '"+start+"' and '"+end+"'");
		if(result != null && result.size() > 0){
			return result;
		}else{
			return null;
		}
	}
	
	public List<Vdev_mes_bind> selectByMessage(String message_id){
		List<Vdev_mes_bind> result = new ArrayList<Vdev_mes_bind>();
		result = hibernateTemplate.find("from Vdev_mes_bind where message_id='"+message_id+"'");
		if(result != null && result.size() > 0){
			return result;
		}else{
			return null;
		}
	}
	
	public Boolean saveDev_mes(Vdev_mes_bind dev_mes){
		try{
			this.hibernateTemplate.save(dev_mes);			
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	//Lynn
	public boolean updateDev_mes(Vdev_mes_bind dev_mes,String staff_id)
	{
		try{
			String a = dev_mes.getMessage_id();
			String b = dev_mes.getUuid();
			String c = dev_mes.getMajor();
			String d = dev_mes.getMinor();
			String e = dev_mes.getTime();
			
			//还得用视图，查看status==2，进行更新。
			//Mes_dev视图中，message_id取mes_dev中的message_id，所以可以直接更新message_id
			List<Mes_dev>mes_devs = hibernateTemplate.find("from Mes_dev where uuid='"+b+"' and major='"+c+"' and minor='"+d+"' and mes_status='2'");
			if (mes_devs!=null&&mes_devs.size()==1) {
				String mes_id = mes_devs.get(0).getMessage_id();
				System.out.println("message_idold:"+mes_id);
				List<Vdev_mes_bind>vdev_mes_binds =hibernateTemplate.find("from Vdev_mes_bind where uuid='"+b+"' and major='"+c+"' and minor='"+d+"' and message_id='"+mes_id+"'");
				if(vdev_mes_binds!=null&&vdev_mes_binds.size()==1)
				{
					Vdev_mes_bind vBind = vdev_mes_binds.get(0);
					vBind.setMessage_id(a);
					vBind.setUuid(b);
					vBind.setMajor(c);
					vBind.setMinor(d);
					vBind.setTime(e);
					hibernateTemplate.update(vBind);
					//更新sessions
					//message表中进行修改sessions字段
					try {
						Message m2 = new Message();
						m2= messageDao.message_one(Integer.parseInt(mes_id));
						JSONArray uuidArray = urlService.getUUIDs(Integer.parseInt(mes_id), staff_id);
						JSONArray majorsArray = uuidArray.getJSONObject(0).getJSONArray("majors");
						String sessions =urlService.urlsessionsToString(majorsArray);
						String total =urlService.urlTotalOfSessions(majorsArray);
						System.out.println("sessions_oldmes : "+sessions+"    total_oldmes : "+total);
						m2.setSessions(sessions);
						m2.setTotal(total);
						messageDao.update(m2);	
						
					} catch (Exception e2) {
						// TODO: handle exception
						e2.printStackTrace();
					}
					return true;
				}
				else {
					return false;
				}				
			}
//			List<Vdev_mes_bind>vdev_mes_binds =hibernateTemplate.find("from Vdev_mes_bind where uuid='"+b+"' and major='"+c+"' and minor='"+d+"'");
//			if(vdev_mes_binds!=null&&vdev_mes_binds.size()>0)
//			{
//				Vdev_mes_bind vBind = vdev_mes_binds.get(0);
//				String mes_id = vBind.getMessage_id();
//				System.out.println("message_idold:"+mes_id);
//				
//				vBind.setMessage_id(a);
//				vBind.setTime(e);
//				hibernateTemplate.update(dev_mes);
//				
//				//更新sessions
//				//message表中进行修改sessions字段
//				Message m = messageDao.message_one(Integer.parseInt(mes_id));
//				JSONArray uuidArray = urlService.getUUIDs(Integer.parseInt(mes_id), staff_id);
//				JSONArray majorsArray = uuidArray.getJSONObject(0).getJSONArray("majors");
//				String sessions =urlService.sessionsToString(majorsArray);
//				String total =urlService.TotalOfSessions(majorsArray);
//				System.out.println("total_old:"+total);
//				m.setSessions(sessions);
//				m.setTotal(total);
//				messageDao.update(m);				
//				
//				return true;
//			}
			else {
				return false;
			}
				
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		
	}
	//Lynn
	public Boolean deleteDev_mes(Vdev_mes_bind dev_mes){
		try{
			String a = dev_mes.getMessage_id();
			String b = dev_mes.getUuid();
			String c = dev_mes.getMajor();
			String d = dev_mes.getMinor();
			List<Vdev_mes_bind>vdev_mes_binds =hibernateTemplate.find("from Vdev_mes_bind where message_id='"+a+"' and uuid='"+b+"' and major='"+c+"' and minor='"+d+"'");
			if(vdev_mes_binds!=null&&vdev_mes_binds.size()>0)
			{
				Vdev_mes_bind vBind = vdev_mes_binds.get(0);
				hibernateTemplate.delete(vBind);				
			}
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public List<String> distinctUUID(int message_id,String staff_id) {
		List<String> result = new ArrayList<String>();
		try {
			temp = hibernateTemplate.find("select distinct uuid from Staff_mes where message_id=? and staff_id=?",new Object[]{message_id,staff_id});
			if (temp != null && temp.size() > 0) {
				for(int i = 0; i < temp.size(); i++){
					result.add( temp.get(i));
				}
				return result;
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public List<String> distinctMajor(int message_id,String staff_id,String uuid) {
		List<String> result = new ArrayList<String>();
		try {
			temp = hibernateTemplate.find("select distinct major from Staff_mes where message_id=? and staff_id=? and uuid=?",new Object[]{message_id,staff_id,uuid});
			if (temp != null && temp.size() > 0) {
				for(int i = 0; i < temp.size(); i++){
					result.add(temp.get(i));
				}
				return result;
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public int[] findMinors(int message_id,String staff_id,String uuid,String major) {
		try {
			temp = this.hibernateTemplate.find("select minor from Staff_mes where message_id=? and staff_id=? and uuid=? and major=?",
					new Object[]{message_id,staff_id,uuid,major});
			if (temp != null && temp.size() > 0) {
				int[] result = new int[temp.size()];
				for(int i = 0; i < temp.size(); i++){
					result[i] = Integer.parseInt(temp.get(i));
				}
				return result;
			}
			return null;
		} catch (Exception e) {
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

	public MessageDao getMessageDao() {
		return messageDao;
	}
	@Resource
	public void setMessageDao(MessageDao messageDao) {
		this.messageDao = messageDao;
	}
	public UrlService getUrlService() {
		return urlService;
	}
	@Resource
	public void setUrlService(UrlService urlService) {
		this.urlService = urlService;
	}
	
	
	
}

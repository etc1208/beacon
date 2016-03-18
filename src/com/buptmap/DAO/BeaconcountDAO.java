package com.buptmap.DAO;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.annotation.Resource;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;

import com.buptmap.model.Beaconcount;
import com.buptmap.model.Staff;


@Component("beaconcountDao")
public class BeaconcountDAO {

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
	
	public boolean add(String jsonstr){
		String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		jsonArray = JSONArray.fromObject(jsonstr);
		jsonObject = jsonArray.getJSONObject(0);
		Beaconcount beaconcount = new Beaconcount();
		beaconcount.setAmount(Integer.valueOf(jsonObject.getString("amount")));
		beaconcount.setReceive_id(jsonObject.getString("receive_id"));
		beaconcount.setSend_id(jsonObject.getString("send_id"));
		beaconcount.setTime(date);
		beaconcount.setType_id(jsonObject.getString("type_id"));
		hibernateTemplate.save(beaconcount);
		return true;
	}
	
	public JSONArray find(String receive_id, String  send_id){
		List<Beaconcount> result = new ArrayList<Beaconcount>();
		jsonArray = new JSONArray();
		jsonObject = new JSONObject();
		
		result = hibernateTemplate.find("from Beaconcount where receive_id='" +receive_id+ "' and send_id='" +send_id+ "'");
		if (result != null && result.size() > 0) {
			for (int i = 0; i < result.size(); i++) {
				Beaconcount beaconcount = result.get(i);
				jsonObject.put("type", beaconcount.getType_id());
				jsonObject.put("amount", beaconcount.getAmount());
				jsonObject.put("time", beaconcount.getTime());
				jsonArray.add(jsonObject);
				
			}
		}
		return jsonArray;
	}

	public JSONArray total(String  send_id){
		List<Object[]> result = new ArrayList<Object[]>();
		List<Object[]> tempList = new ArrayList<Object[]>();
		List<Object[]> tempList2 = new ArrayList<Object[]>();
		List<Beaconcount> result1 = new ArrayList<Beaconcount>();
		List<Object[]> result2 = new ArrayList<Object[]>();
		List<Object[]> result3 = new ArrayList<Object[]>();
		List<Object[]> ID = new ArrayList<Object[]>();
		List<Object[]> ID_unUsed = new ArrayList<Object[]>();		
		List<Staff> staffList = new ArrayList<Staff>();
		int total_Id = 0;
		jsonArray = new JSONArray();
		jsonObject = new JSONObject();
		Object[] receiveObjects = null;
		try {
			//首先获取send_id自己的分配信息
			tempList = hibernateTemplate.find("select parent_id,staff_name from Staff where staff_id='" +send_id+ "'");
			if(tempList != null && tempList.size() > 0){
				String parent_id = tempList.get(0)[0].toString();
				String receive_name = tempList.get(0)[1].toString();
				String receive_id = send_id;
				jsonObject.put("receive_id", receive_id);	
				jsonObject.put("receive_name", receive_name);
				int used = 0;
				int recover = 0;
				result2 = hibernateTemplate.find("from Beacon b where b.status!='回收' and b.create_id = '" + receive_id + "'");
				if (result2 != null && result2.size() != 0) {
					used += result2.size();
				}
				result3 = hibernateTemplate.find("from Beacon b where b.status='回收' and b.create_id = '" + receive_id + "'");
				if (result3 != null && result3.size() != 0) {
					recover += result3.size();
				}				
				
				jsonObject.put("Beacon_used", used);		
				jsonObject.put("Beacon_recover", recover);		
				result1 = hibernateTemplate.find("from Beaconcount where receive_id='" +receive_id+ "' and send_id='" +parent_id+ "'");				
				if (result1 != null && result1.size() != 0) {
					int account = 0;
					for (int j = 0; j < result1.size(); j++) {
						Beaconcount beaconcount = result1.get(j);
						account += beaconcount.getAmount();
					}
					jsonObject.put("Beacon_all", account);					
				}
				else {
					jsonObject.put("Beacon_all", 0);				
				}
				//计算ID资源数，已用ID数
				 ID = hibernateTemplate.find("select distinct minor,major,uuid from Vdev_staff_bind where staff_id='"+receive_id+"' order by uuid,major,minor");
				 if(ID.size()>0 && ID != null){
					 jsonObject.put("ID_all", ID.size());
					 total_Id = ID.size();
				 }else{
					 jsonObject.put("ID_all", 0);
					 total_Id = 0;
				 }
				 tempList = hibernateTemplate.find("select distinct minor,major,uuid from Mes_dev where mes_status !='4' order by uuid,major,minor");//where mes_status !='4'	 
				 //System.out.println("ID数目！！！！！"+ID.size());
				 ID_unUsed=NotInObject(ID, tempList);
				// System.out.println("ID数目！！！！！"+ID.size()+"ID_unUsed数目！！！！"+ID_unUsed.size());
				 if(ID_unUsed.size()>0 && ID_unUsed != null){
					 jsonObject.put("ID_used", total_Id-ID_unUsed.size());
				 }else{
					 jsonObject.put("ID_used", total_Id);
				 }
				 jsonArray.add(jsonObject);
			}
			
			//获取send_id子代理的分配信息
			tempList = hibernateTemplate.find("select staff_id,staff_name from Staff where parent_id='" +send_id+ "'");
			if (tempList != null && tempList.size() > 0) {				
				for (int i = 0; i < tempList.size(); i++) {
					receiveObjects = tempList.get(i);
					String receive_id = receiveObjects[0].toString();
					jsonObject.put("receive_id", receive_id);	
					jsonObject.put("receive_name", receiveObjects[1].toString());
					int used = 0;
					int recover = 0;
					result2 = hibernateTemplate.find("from Beacon b where b.status!='回收' and b.create_id = '" +receive_id + "'");
					if (result2 != null && result2.size() != 0) {
						used += result2.size();
					}
					result3 = hibernateTemplate.find("from Beacon b where b.status='回收' and b.create_id = '" + receive_id + "'");
					if (result3 != null && result3.size() != 0) {
						recover += result3.size();
					}
					
					jsonObject.put("Beacon_used", used);		
					jsonObject.put("Beacon_recover", recover);		
					result1 = hibernateTemplate.find("from Beaconcount where receive_id='" +receive_id+ "' and send_id='" +send_id+ "' order by time desc");
					
					if (result1 != null && result1.size() != 0) {
						int account = 0;
						for (int j = 0; j < result1.size(); j++) {
							Beaconcount beaconcount = result1.get(j);
							account += beaconcount.getAmount();
						}
						jsonObject.put("Beacon_all", account);
						//jsonObject.put("time", result1.get(0).getTime());
					}
					else {
						jsonObject.put("Beacon_all", 0);
						//jsonObject.put("time", "");
					}
					
					//计算ID资源数，已用ID数
					 ID = hibernateTemplate.find("select distinct minor,major,uuid from Vdev_staff_bind where staff_id='"+receive_id+"' order by uuid,major,minor");
					 if(ID.size()>0 && ID != null){
						 jsonObject.put("ID_all", ID.size());
						 total_Id = ID.size();
					 }else{
						 jsonObject.put("ID_all", 0);
						 total_Id = 0;
					 }
					 tempList2 = hibernateTemplate.find("select distinct minor,major,uuid from Mes_dev where mes_status !='4' order by uuid ,major,minor");//where mes_status !='4'	 
					 ID_unUsed = NotInObject(ID, tempList2);	 
					 if(ID_unUsed.size()>0 && ID_unUsed != null){
						 jsonObject.put("ID_used", total_Id-ID_unUsed.size());
					 }else{
						 jsonObject.put("ID_used", total_Id);
					 }
					jsonArray.add(jsonObject);
					
				}
			}
			return jsonArray;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return jsonArray;
		}
		
	}
	
	public List<Object[]> NotInObject(List<Object[]> objects1,List<Object[]>objects2) {
		int size1=objects1.size(),size2=objects2.size();
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
					objects1.remove(i);
					--size1;
					ptr1=i;
					break;
				} 
			}
			
		}
		
		return objects1;
	}

}

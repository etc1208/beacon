package com.buptmap.action;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.struts2.ServletActionContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.buptmap.Service.BeaconapiService;
import com.buptmap.Service.ProjectService;
import com.buptmap.Service.StaffService;
import com.buptmap.Service.StatisticService;
import com.buptmap.Service.UrlService;
import com.buptmap.model.Testbeacon;
import com.buptmap.util.MD5Util;
import com.opensymphony.xwork2.ActionSupport;

/**
 * 
 * @author yh
 *提供给成都商家那边的api
 */

@Component
@Scope("prototype")
public class BeaconapiAction extends ActionSupport{
	private String filePath;
	private String savePath;
	private String user_id;
	private String pwd;
	private String key;
	private String jsonstr;
	private StaffService staffService;
	private BeaconapiService beaconapiService;
	private UrlService urlService;
	private StatisticService statisticService;
	private Map<String,Object> resultObj;
	/*
	 * 将http开头的一个网络文件复制到服务器，返回服务器中地址
	 */
	public boolean verifyKey(){
		if(user_id == null || user_id == "" || key == null || key == ""){			
        	return false;
		}
		if(!staffService.verify(user_id, key)){
        	return false;
		}
		return true;
	}
	
	public String getSecretKey(){
		Map<String , Object> map = new HashMap<String, Object>();
		try {
			map = beaconapiService.getSecretKey(user_id, MD5Util.string2MD5(pwd));
		} catch (Exception e) {
			e.printStackTrace();
			map.put("success", false);
			map.put("message", e.getMessage());
		}
		resultObj = JSONObject.fromObject(map);
		return SUCCESS;
	}
	
	public Map<String, Object> packageInfo() throws Exception{
		Map<String, Object> map = new HashMap<String, Object>();
	
		String dstname = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
		String[] suffix = filePath.split("\\.");
		String suf = suffix[suffix.length-1].toLowerCase();
		System.out.println("文件格式为："+suf);
		if((!suf.equals("jpg"))&&(!suf.equals("jpeg"))&&(!suf.equals("gif"))&&(!suf.equals("png"))&&(!suf.equals("xls"))){
			map.put("success",false);
			map.put("message", "文件格式不正确");
		}else {
			dstname = dstname+"."+suffix[suffix.length-1];
			String dstPath = this.getSavePath()+File.separator+dstname;
			System.out.println("保存在服务器中的路径："+dstPath);		
			File dstFile = new File(dstPath);
			
			InputStream in = null;
			OutputStream out = null;
			try {
				out = new FileOutputStream(dstFile);
				in = new URL(filePath).openStream();
				byte[] buff = new byte[16*1024];
				int len = 0;
				while((len = in.read(buff))>0){
					out.write(buff, 0, len);
				}
				map.put("success", true);
				map.put("filePath", dstPath);//返回给用户图片在服务器上对应的地址
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				map.put("success", false);
				map.put("message", e.getMessage());
			}finally{
				if(null!=in){
					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
						map.put("success", false);
						map.put("message", e.getMessage());
					}
				}
				if(null!=out){
					try {
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
						map.put("success", false);
						map.put("message", e.getMessage());
					}
				}
			}						
		}
		
		return map;
	}
	
	public String getFileAddr() {		
		Map<String, Object> map = new HashMap<String, Object>();
		if(!verifyKey()){
			map.put("success",false);
			map.put("message", "验证user_id和key失败");
			resultObj = JSONObject.fromObject(map);
			return SUCCESS;
		}
		try{
			map = packageInfo();
			
		}catch (Exception e) {
			map.put("success",false);
			map.put("message", e.getMessage());
			e.printStackTrace();
		}
		resultObj = JSONObject.fromObject(map);
		return SUCCESS;
	}
	
	public String importEquipExcel(){
		Map<String, Object> map = new HashMap<String, Object>();
		
		/*if(!verifyKey()){
			map.put("success",false);
			map.put("message", "验证user_id和key失败");
			resultObj = JSONObject.fromObject(map);
			return SUCCESS;
		}*/
		if (filePath == null || filePath.equals("")) {
			map.put("success", false);
			map.put("message", "数据传送为空");
			resultObj = JSONObject.fromObject(map);
			return SUCCESS;
		}
		String[] suffix = filePath.split("\\.");
		if (!suffix[suffix.length-1].equals("xls")){
			System.out.println("-----结尾是："+suffix[suffix.length-1]);
			map.put("success", false);
			map.put("message", "文件格式只能为.xls结尾的excel文件");
			resultObj = JSONObject.fromObject(map);
			return SUCCESS;
		}
		File file = new File(filePath);
		if (!file.exists()){
			map.put("success", false);
			map.put("message", "文件不存在");
			resultObj = JSONObject.fromObject(map);
			return SUCCESS;
		}
		
		try {
			System.out.println("excel文件地址为："+filePath);
			resultObj = beaconapiService.importEquipExcel(filePath,user_id);
			return SUCCESS;
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			map.put("success", false);
			map.put("message", e.toString());
			resultObj = JSONObject.fromObject(map);
			return "success";
		}
	}
	
	public String deployAndConfig(){
		
		Map<String, Object> map = new HashMap<String, Object>(); 
		if(!verifyKey()){
			map.put("success",false);
			map.put("message", "验证user_id和key失败");
			resultObj = JSONObject.fromObject(map);
			return SUCCESS;
		}
		try {
			System.out.println("deployAndConfig接口请求字符串："+jsonstr);
			if (jsonstr == null || jsonstr == "") {
				map.put("success", false);
				map.put("message", "请求字符串为空");
				resultObj = JSONObject.fromObject(map);
				return "success";
			}			
			JSONObject object = JSONObject.fromObject(jsonstr);
			JSONArray data = JSONArray.fromObject(object.getString("data"));	    	
			if (beaconapiService.deployAndConfig(data, user_id)) {
				map.put("success", true);
				map.put("message", "部署与配置成功");
				resultObj = JSONObject.fromObject(map);
				return "success";
			}else {
				map.put("success", false);
				map.put("message", "部署与配置失败");
				resultObj = JSONObject.fromObject(map);
				return "success";
			}					
		}catch (Exception e) {
			e.printStackTrace();
			map.put("success", false);
			map.put("message", "deployAndConfig接口发生异常，"+e.getMessage());
			resultObj = JSONObject.fromObject(map);
			return "success";
		}	
	}
	
	public String addUrl() throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		if(!verifyKey()){
			map.put("success",false);
			map.put("message", "验证user_id和key失败");
			resultObj = JSONObject.fromObject(map);
			return SUCCESS;
		}
		try{
			System.out.println("addUrl接口请求字符串："+jsonstr);
			map = urlService.addMessage(jsonstr);
		}catch(Exception e){
			e.printStackTrace();
			map.put("success", false);
			map.put("message", e.toString());
		}
		resultObj = JSONObject.fromObject(map);
		return SUCCESS;
	}
	
	public String editUrl() throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		if(!verifyKey()){
			map.put("success",false);
			map.put("message", "验证user_id和key失败");
			resultObj = JSONObject.fromObject(map);
			return SUCCESS;
		}
		try{
			map = this.urlService.editMessage(jsonstr);
		}catch(Exception e){
			e.printStackTrace();
			map.put("success", false);
			map.put("message", e.toString());
		}
		resultObj = JSONObject.fromObject(map);
		return SUCCESS;
	}
	
	public String getProject(){
		Map<String, Object> map = new HashMap<String, Object>();
		if(!verifyKey()){
			map.put("success",false);
			map.put("message", "验证user_id和key失败");
			resultObj = JSONObject.fromObject(map);
			return SUCCESS;
		}
		try {
			JSONArray resultArray =beaconapiService.getProject(user_id);
			if (resultArray != null && resultArray.size() >0) {
				map.put("success", true);
				map.put("total", resultArray.size());
				map.put("project", resultArray);
				resultObj = JSONObject.fromObject(map);
			}else {
				map.put("success", false);
				map.put("message", "目前没有您所属项目。");
				resultObj = JSONObject.fromObject(map);
			}			
			return SUCCESS;
		} catch (Exception e) {
			e.printStackTrace();
			map.put("success", false);
			map.put("message", e.toString());
			resultObj = JSONObject.fromObject(map);
			return "success";
		}
	}
	
	public String operateUrl() throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> map2 = new HashMap<String, Object>();
		if(!verifyKey()){
			map.put("success",false);
			map.put("message", "验证user_id和key失败");
			resultObj = JSONObject.fromObject(map);
			return SUCCESS;
		}
		
		map2 = packageInfo();
		if(!map2.containsKey("filePath")){
			map.put("success", false);
			map.put("message", "获取图片在服务器中地址失败");
			resultObj = JSONObject.fromObject(map);
			return SUCCESS;
		}else {
			String logo = map2.get("filePath").toString();
			try{
				System.out.println("--调用operateUrl接口传递字符串---"+jsonstr);
				map = beaconapiService.operateMessage(jsonstr,logo);
				if(map.containsKey("pageError") || map.containsKey("logoError") || map.containsValue(false)){
					map.put("success", false);
				}else {
					map.put("message", "url审核通过");
				}
			}catch(Exception e){
				e.printStackTrace();
				map.put("success", false);
				map.put("message", e.toString());
			}
			resultObj = JSONObject.fromObject(map);
			return SUCCESS;
		}
		
	}
	
	public String getStatisticInfo() {
		Map<String, Object> map = new HashMap<String, Object>();
		if(!verifyKey()){
			map.put("success",false);
			map.put("message", "验证user_id和key失败");
			resultObj = JSONObject.fromObject(map);
			return SUCCESS;
		}
		try {
			System.out.println("getStatisticInfo接口请求字符串："+jsonstr);
			if(!statisticService.Instaff(jsonstr)){
				map.put("success", false);
				map.put("message", "无权限做相关查询");
				resultObj = JSONObject.fromObject(map);
				return "success";
			}
			
			JSONArray resultArray = statisticService.findStatisticMinor(jsonstr);			
			if (resultArray != null && resultArray.size() > 0) {
				map.put("success", true);
				map.put("total", resultArray.size());
				map.put("statistic", resultArray);
				resultObj = JSONObject.fromObject(map);
			}
			else {
				map.put("success", false);
				map.put("message", "没有查到相关的统计信息");
				resultObj = JSONObject.fromObject(map);
			}			
			
			return SUCCESS;			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			map.put("success", false);
			map.put("message", e.toString());
			resultObj = JSONObject.fromObject(map);
			
			return "success";
		}
		
	}
	
	//专为修改url部分的域名做的工具
	public String updateUrl(){
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			map = beaconapiService.updateUrl();
			resultObj = JSONObject.fromObject(map);
		} catch (Exception e) {
			e.printStackTrace();
			map.put("success", false);
			map.put("message", e.toString());
			resultObj = JSONObject.fromObject(map);
		}
		return "success";
	}
	
	public void setSavePath(String value){
		this.savePath = value;
	}
	private String getSavePath() throws Exception{		
		return ServletActionContext.getServletContext()
			.getRealPath(savePath);
	}
	public Map<String, Object> getResultObj() {
		return resultObj;
	}
	public void setResultObj(Map<String, Object> resultObj) {
		this.resultObj = resultObj;
	}
	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	public String getUser_id() {
		return user_id;
	}
	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}
	public String getPwd() {
		return pwd;
	}

	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public StaffService getStaffService() {
		return staffService;
	}
	@Resource
	public void setStaffService(StaffService staffService) {
		this.staffService = staffService;
	}

	public BeaconapiService getBeaconapiService() {
		return beaconapiService;
	}
	@Resource
	public void setBeaconapiService(BeaconapiService beaconapiService) {
		this.beaconapiService = beaconapiService;
	}

	public String getJsonstr() {
		return jsonstr;
	}

	public void setJsonstr(String jsonstr) {
		this.jsonstr = jsonstr;
	}

	public UrlService getUrlService() {
		return urlService;
	}
	@Resource
	public void setUrlService(UrlService urlService) {
		this.urlService = urlService;
	}

	public StatisticService getStatisticService() {
		return statisticService;
	}
	@Resource
	public void setStatisticService(StatisticService statisticService) {
		this.statisticService = statisticService;
	}

}

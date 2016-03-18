package com.buptmap.action;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.sf.json.JSONObject;

import org.apache.struts2.ServletActionContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.buptmap.util.JSONUnit;
import com.buptmap.util.NetTools;
import com.opensymphony.xwork2.ActionSupport;

@Component
@Scope("prototype")
public class WechatAction extends ActionSupport{

	private static final long serialVersionUID = 1L;
	
	private JSONObject resultObj;
	private String savePath;
	private String url;
	private String ticket;
	
	public String sign(){
		Map<String, Object> ret = new HashMap<String, Object>();
		String signature = "";
		
		String nonce_str = create_nonce_str();
		String jsapi_ticket = getJsApiTicket();
		String timestamp = create_timestamp();
		System.out.println("------------URL--------"+url);
		//String url = "http://bm.mapnext.com:8080/WechatYaoYiYao/main.html?x=39.28&y=14.28&spotName=920&floorId=Floor9";
		String string1 = "jsapi_ticket="+jsapi_ticket+"&noncestr="+nonce_str+"&timestamp="+timestamp+"&url="+url;
		System.out.println("拼接的字符串string1为："+string1);
		try {
            MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            crypt.reset();
            crypt.update(string1.getBytes("UTF-8"));
            signature = byteToHex(crypt.digest());
            System.out.println("得到签名signiture为："+signature);
            ret.put("url", url);
            ret.put("jsapi_ticket", jsapi_ticket);
            ret.put("nonceStr", nonce_str);
            ret.put("timestamp", timestamp);
            ret.put("signature", signature);
            ret.put("success", true);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            ret.put("success", false);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            ret.put("success", false);
        }
		
		
        resultObj = JSONObject.fromObject(ret);
		return SUCCESS;
	}
	
	public String get_access_token() {
		String url = "http://wx.sinofond.com/beacon/admin.php/Share/get_json_token";
		String jsonStr = JSONUnit.loadJSON(url);
		JSONObject jsonObject = JSONObject.fromObject(jsonStr);
		System.out.println("获取access_token:"+jsonObject.getString("token"));
		return jsonObject.getString("token");
		/*String AppID = "wxfffd87bbcd89bd32";
		String AppSecret = "5ffeaac8893c1003124cc7d37c7a3232";
		String access_token = null;
		FileInputStream fis = null;
		FileOutputStream fos = null;
		InputStreamReader isr = null;
		BufferedReader buf = null;
		String temp = null;
		JSONObject jsonObject = null;
		try {
			
			String filePath = this.getSavePath()+File.separator+"access_token.json";
			System.out.println("-----access_token.json文件路径-----："+filePath);
			File file = new File(filePath);
			if(file.exists()){
				 fis = new FileInputStream(filePath);
				 isr = new InputStreamReader(fis);
				 buf = new BufferedReader(isr);
				 temp = buf.readLine();//{"ticket":"...","expires_in":....}
				 jsonObject = JSONObject.fromObject(temp);
				 //System.out.println("文件中时间戳："+jsonObject.getString("expires_in"));
				 //System.out.println("系统时间戳为："+System.currentTimeMillis());
				 if(Long.parseLong(jsonObject.getString("expires_in")) > System.currentTimeMillis()){
					 System.out.println("文件中access_token未过期,可直接读取文件内容:"+jsonObject.getString("access_token"));
					 access_token = jsonObject.getString("access_token");
				 } else {
				     String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid="+AppID+"&secret="+AppSecret;
				     String jsonStr = JSONUnit.loadJSON(url);
				     jsonObject = JSONObject.fromObject(jsonStr);
				     if(jsonObject.getString("access_token").length() > 0){
				    	 access_token = jsonObject.getString("access_token");
				    	 Long expires_in = jsonObject.getLong("expires_in")*1000 + System.currentTimeMillis();
				    	 temp = "{\"access_token\":\""+access_token+"\",\"expires_in\":"+expires_in+"}";
				    	 fos = new FileOutputStream(file);
				    	 fos.write(temp.getBytes());
				    	 fos.close();
				    	 
				    	 System.out.println("文件中access_token过期,重新调用微信接口获取:"+access_token);
				     }else {
				         System.out.println("调用微信接口获取access_token失败");
				     }
				}
				 if(fis != null){
					 fis.close();
				 }
				 if(fos != null){
					 fos.close();
				 }
			} else {
				System.out.println("access_token.json文件不存在");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
        return access_token;*/
	}
	
	public String getJsApiTicket() {
		String ticket = null;
		FileInputStream fis = null;
		FileOutputStream fos = null;
		InputStreamReader isr = null;
		BufferedReader buf = null;
		String temp = null;
		JSONObject jsonObject = null;
		try {
			
			String filePath = this.getSavePath()+File.separator+"jsapi_ticket.json";
			System.out.println("-----jsapi_ticket.json文件路径-----："+filePath);
			File file = new File(filePath);
			if(file.exists()){
				 fis = new FileInputStream(filePath);
				 isr = new InputStreamReader(fis);
				 buf = new BufferedReader(isr);
				 temp = buf.readLine();//{"ticket":"...","expires_in":....}
				 jsonObject = JSONObject.fromObject(temp);
				 //System.out.println("文件中时间戳："+jsonObject.getString("expires_in"));
				 //System.out.println("系统时间戳为："+System.currentTimeMillis());
				 if(Long.parseLong(jsonObject.getString("expires_in")) > System.currentTimeMillis()){
					 System.out.println("文件中jsapi_ticket未过期,可直接读取文件内容:"+jsonObject.getString("ticket"));
					 ticket = jsonObject.getString("ticket");
				 } else {
					 String access_token = get_access_token();
				     String url = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token="+access_token+"&type=jsapi";
				     String jsonStr = JSONUnit.loadJSON(url);
				     jsonObject = JSONObject.fromObject(jsonStr);
				     if(jsonObject.get("errcode").equals(0) && jsonObject.get("errmsg").equals("ok")){
				    	 ticket = jsonObject.getString("ticket");
				    	 Long expires_in = jsonObject.getLong("expires_in")*1000 + System.currentTimeMillis();
				    	 temp = "{\"ticket\":\""+ticket+"\",\"expires_in\":"+expires_in+"}";
				    	 fos = new FileOutputStream(file);
				    	 fos.write(temp.getBytes());
				    	 fos.close();
				    	 
				    	 System.out.println("文件中jsapi_ticket过期,重新调用微信接口获取:"+ticket);
				     }else {
				         System.out.println("调用微信接口获取jsapi_ticket失败");
				     }
				}
				 if(fis != null){
					 fis.close();
				 }
				 if(fos != null){
					 fos.close();
				 }
			} else {
				System.out.println("jsapi_ticket.json文件不存在");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
        return ticket;
    }
	
	private String create_nonce_str() {
        return UUID.randomUUID().toString();
    }
	
	private String create_timestamp() {
        return Long.toString(System.currentTimeMillis() / 1000);
    }
	
	private String byteToHex(final byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash)
        {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }
	
	public String getUserInfo(){
		Map<String, Object> map = new HashMap<String, Object>();
		String url = "https://api.weixin.qq.com/shakearound/user/getshakeinfo?access_token="+get_access_token();
		String postStr = "{\"ticket\":\""+ticket+"\"}";
		try {
			String result = NetTools.doPost(url, postStr);
			JSONObject jsonObject = JSONObject.fromObject(result);
			if(jsonObject.getString("errmsg").equals("success.") && jsonObject.getInt("errcode") == 0){
				if(jsonObject.containsKey("data")){
					JSONObject data = JSONObject.fromObject(jsonObject.get("data"));
					if (data.containsKey("page_id")) {
						map.put("page_id", data.get("page_id"));
					}
					if (data.containsKey("openid")) {
						map.put("openid", data.get("openid"));
					}
					if (data.containsKey("brand_userame")) {
						map.put("brand_userame", data.get("brand_userame"));
					}
					if (data.containsKey("beacon_info")) {
						JSONObject beacon_info = JSONObject.fromObject(data.get("beacon_info"));
						if (beacon_info.containsKey("distance")) {
							map.put("distance", beacon_info.get("distance"));
						}
						if (beacon_info.containsKey("uuid")) {
							map.put("uuid", beacon_info.get("uuid"));
						}
						if (beacon_info.containsKey("major")) {
							map.put("major", beacon_info.get("major"));
						}
						if (beacon_info.containsKey("minor")) {
							map.put("minor", beacon_info.get("minor"));
						}
					}
				}
				map.put("success", true);
			} else {
				map.put("success", false);
				map.put("message", jsonObject.get("errmsg"));
			}
		} catch (Exception e) {
			map.put("success", false);
			map.put("message", e.getMessage());
			e.printStackTrace();
		}
		
		resultObj = JSONObject.fromObject(map);
		return SUCCESS;
	}

	public JSONObject getResultObj() {
		return resultObj;
	}

	public void setResultObj(JSONObject resultObj) {
		this.resultObj = resultObj;
	}
	
	public void setSavePath(String value){
		this.savePath = value;
	}
	private String getSavePath() throws Exception{		
		return ServletActionContext.getServletContext()
			.getRealPath(savePath);
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getTicket() {
		return ticket;
	}

	public void setTicket(String ticket) {
		this.ticket = ticket;
	}
}

package test;

import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.buptmap.action.PlaceAction;
import com.buptmap.util.NetTools;
import com.buptmap.util.*;

public class test {
	public static void main(String[] args){
		/*ApplicationContext applicationContext = new ClassPathXmlApplicationContext("spring.xml");
		PlaceAction place1 = (PlaceAction)applicationContext.getBean("placeAction");
		PlaceAction place2 = (PlaceAction)applicationContext.getBean("placeAction");
		System.out.println(place1==place2);*/
		
		/*Map<String,String> resultMap = new HashMap<String,String>();
		String urlStr = "http://10.103.240.198:8080/IBeaconSystem/url!add";            
        Map<String, String> textMap = new HashMap<String, String>();  		      
        textMap.put("file", "testname");  
        Map<String, String> fileMap = new HashMap<String, String>();  
        fileMap.put("upload", "E:\\120.png");  
        String ret = NetTools.formUpload(urlStr, textMap, fileMap);
        
        System.out.println(ret);*/
		
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("uuid", "SJGYV-JHIHUH-HUIGHIV");
		jsonObject.put("major", "11111");
		jsonObject.put("minor", "22222");
		String[] pages = null;
		
		WeChatAPI.deviceLinkPages(jsonObject, pages);
		
		
        
	}
}

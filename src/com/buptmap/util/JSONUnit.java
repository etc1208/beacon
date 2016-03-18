package com.buptmap.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class JSONUnit {
	public static String loadJSON (String url) {
        StringBuilder json = new StringBuilder();
        try {
            URL URL = new URL(url);
            URLConnection urlConnection = URL.openConnection();
            BufferedReader bf = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(),"utf-8"));
            String inputLine = null;
            while ( (inputLine = bf.readLine()) != null) {
                json.append(inputLine);
            }
            bf.close();
        } catch (MalformedURLException e) {
        	e.printStackTrace();
        	return null;
        } catch (IOException e) {
        	e.printStackTrace();
        	return null;
        }
        return json.toString();
    }
}

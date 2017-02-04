package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.json.JSONObject;

public class JSONUtils {

    public static JSONObject getJSONObject(File f) throws IOException{
        
    	String jsonStr = "";
    	BufferedReader reader = new BufferedReader(new FileReader(f));
    	String ln;
    	while((ln = reader.readLine()) != null){
    		jsonStr += ln;
    	}
    	
    	JSONObject json = new JSONObject(jsonStr);
    	reader.close();
    	return json;
    }
	
}

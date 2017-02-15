package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.json.JSONObject;

public class JSONUtils {

    public static JSONObject getJSONObject(File f){
    	String jsonStr = getFileAsString(f);
    	JSONObject json = new JSONObject(jsonStr);
    	return json;
    }
    
    public static String getFileAsString(File f){
    	try{
	    	String jsonStr = "";
	    	BufferedReader reader = new BufferedReader(new FileReader(f));
	    	String ln;
	    	while((ln = reader.readLine()) != null){
	    		jsonStr += ln;
	    	}
	    	reader.close();
	    	return jsonStr;
    	}
    	catch(Exception e){
    		return null;
    	}
    }
    
    /**
     * Returns the "bill" embedded JSONObject within a vote
     * if it exists. If the vote is not a bill vote, then
     * null will be returned
     * @param vote JSONObject
     * @return bill JSONObject or null
     */
    public static JSONObject getVoteBill(JSONObject vote){
    	try{
    		return vote.getJSONObject("bill");
    	}
    	catch(Exception e){
    		return null;
    	}
    }
	
}

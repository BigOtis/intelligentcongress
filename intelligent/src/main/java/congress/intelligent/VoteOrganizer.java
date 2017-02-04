package congress.intelligent;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import utils.JSONUtils;

public class VoteOrganizer {
	
	private static String senate113Votes_2013 = "C:\\Users\\pgl57\\Desktop\\msproj\\congress\\113\\votes\\2013";
	private static String senate113Votes_2014 = "C:\\Users\\pgl57\\Desktop\\msproj\\congress\\113\\votes\\2014";

	
	public static void main( String[] args ) throws IOException{
    	
    	VoteOrganizer vr = new VoteOrganizer();
    	vr.organizeVotes(senate113Votes_2013,"s113_2013_votes");
    	vr.organizeVotes(senate113Votes_2014,"s113_2014_votes");
   	
    }
    
    public void organizeVotes(String dir, String dirName) throws IOException{
    	
    	File sDir = new File(dir);
    	File[] files = sDir.listFiles();
    	
    	int count = 0;
    	
    	File oDir = new File(dirName);
    	oDir.mkdir();
    	for(File dir2 : files){
    		if(dir2.getName().contains("s")){
	    		for(File file : dir2.listFiles()){
	    			if(file.getName().contains("data.json")){
	    				JSONObject data = JSONUtils.getJSONObject(file);
	    				try{
	    					String billName = data.getJSONObject("bill").getInt("number") + "";
	    					FileUtils.copyFile(file, new File(dirName + "\\svote_" + billName + "_" + count));
	    					count++;
	    				}
	    				catch(Exception e){
	    					System.out.println(dir2.getName());
	    				}
	    			}
	    		}
    		}
    	}
    	System.out.println("Count: " + count);
    }
}

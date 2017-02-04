package congress.intelligent;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import utils.JSONUtils;

public class VoteOrganizer {
	
	private static String senate113Votes_2013 = "C:\\Users\\pgl57\\Desktop\\msproj\\congress\\113\\votes\\2013";
	private static String senate113Votes_2014 = "C:\\Users\\pgl57\\Desktop\\msproj\\congress\\113\\votes\\2014";
	private static String senate114Votes_2015 = "C:\\Users\\pgl57\\Desktop\\msproj\\congress\\114\\votes\\2015";
	private static String senate114Votes_2016 = "C:\\Users\\pgl57\\Desktop\\msproj\\congress\\114\\votes\\2016";

	
	public static void main( String[] args ) throws IOException{
    	
    	VoteOrganizer vr = new VoteOrganizer();
    	vr.organizeVotes(senate113Votes_2013,"s113_2013_votes");
    	vr.organizeVotes(senate113Votes_2014,"s113_2014_votes");
    	vr.organizeVotes(senate114Votes_2015,"s114_2015_votes");
    	vr.organizeVotes(senate114Votes_2016,"s114_2016_votes");
   	
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
	    				JSONObject vote = JSONUtils.getJSONObject(file);
	    				JSONObject bill = JSONUtils.getVoteBill(vote);
	    				String name = "";
	    				if(bill != null){
	    					name = bill.getInt("number")+"";
	    				}
	    				else{
	    					name = vote.getString("category");
	    				}
    					FileUtils.copyFile(file, new File(dirName + "\\svote_" + name + "_" + count));
    					count++;
	    			}
	    		}
    		}
    	}
    	System.out.println("Count: " + count);
    }
}

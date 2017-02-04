package congress.intelligent;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import utils.JSONUtils;

/**
 * 
 *
 */
public class BillOrganizer{
	
	private static String senate113Bills = "C:\\Users\\pgl57\\Desktop\\msproj\\congress\\113\\bills\\s";
	private static String senate114Bills = "C:\\Users\\pgl57\\Desktop\\msproj\\congress\\114\\bills\\s";

    public static void main( String[] args ) throws IOException{
    	
    	BillOrganizer br = new BillOrganizer();
    	br.printBillStatistics(senate113Bills,"s113_bills");
    	br.printBillStatistics(senate114Bills,"s114_bills");
    }
    
    public void printBillStatistics(String dir, String newDirName) throws IOException{
    	
    	File sDir = new File(dir);
    	File[] files = sDir.listFiles();
    	System.out.println("Found: " + files.length + " files in " + sDir.getName());
    	File oDir = new File(newDirName);
    	oDir.mkdir();
    	int count = 0;
    	for(File dir2 : files){
    		for(File file : dir2.listFiles()){
    			if(file.getName().contains("data.json")){
    				JSONObject data = JSONUtils.getJSONObject(file);
    				String bill = data.getString("bill_id");
    				FileUtils.copyFile(file, new File(newDirName + "\\sbill_" + bill));
    				count++;
    			}
    		}		
    	}
    	System.out.println("Count: " + count);
    }
    
}

package congress.mongodb.load;

import java.io.File;
import java.io.FileFilter;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import utils.JSONUtils;

public class ImportBillText {

	public static void main(String args[]){
		MongoClient mongo = new MongoClient("localhost", 27017);
		MongoDatabase db = mongo.getDatabase("CongressDB");
		MongoCollection<Document> collection = db.getCollection("SenateBillText");
		
		String[] billFolders = new String[]{"114"};
		
		for(String dirName : billFolders){
			System.out.println("Adding bills text from: " + dirName);
			File dir = new File("s" + dirName + "_bill_text");
			for(File dir2 : dir.listFiles()){
				File[] dir3 = dir2.listFiles(new FileFilter() {
					@Override
					public boolean accept(File pathname) {
						if(pathname.getName().contains("text-v"))
							return true;
						return false;
					}
				});
				String billTextString = JSONUtils.getFileAsString(dir3[0].listFiles()[0].listFiles()[1]);
				String billName = dir2.getName() + "-" + dirName;
				
				collection.insertOne(new Document().append("bill_id", billName).append("bill_text", billTextString));
			}
		}
		mongo.close();
	}
	
	public static void insertJSON(String json, MongoCollection<Document> collection){
		
		Document dbObject = Document.parse(json);
		collection.insertOne(dbObject);
	}
	
}

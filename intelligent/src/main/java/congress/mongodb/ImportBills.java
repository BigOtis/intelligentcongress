package congress.mongodb;

import java.io.File;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import utils.JSONUtils;

public class ImportBills {
	
	public static void main(String args[]){
		MongoClient mongo = new MongoClient("localhost", 27017);
		MongoDatabase db = mongo.getDatabase("CongressDB");
		MongoCollection<Document> collection = db.getCollection("SenateBills");
		
		String[] billFolders = new String[]{"s113_bills", "s114_bills"};
		
		for(String dirName : billFolders){
			System.out.println("Adding bills from: " + dirName);
			File dir = new File(dirName);
			for(File f : dir.listFiles()){
				String json = JSONUtils.getFileAsString(f);
				insertJSON(json, collection);
			}
		}
		mongo.close();
	}
	
	public static void insertJSON(String json, MongoCollection<Document> collection){
		
		Document dbObject = Document.parse(json);
		collection.insertOne(dbObject);

//		collection = db.getCollection("congressDB");
//		MongoCursor<Document> cursorDocJSON = collection.find().iterator();
//		while (cursorDocJSON.hasNext()) {
//			System.out.println(cursorDocJSON.next());
//		}
	}

}

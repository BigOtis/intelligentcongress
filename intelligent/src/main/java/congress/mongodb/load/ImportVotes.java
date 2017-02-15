package congress.mongodb.load;

import java.io.File;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import utils.JSONUtils;

public class ImportVotes {
	
	public static void main(String args[]){
		MongoClient mongo = new MongoClient("localhost", 27017);
		MongoDatabase db = mongo.getDatabase("CongressDB");
		MongoCollection<Document> collection = db.getCollection("SenateVotes");
		
		String[] voteFolders = new String[]{"s113_2013_votes", "s113_2014_votes", "s114_2015_votes", "s114_2016_votes"};
		
		for(String dirName : voteFolders){
			System.out.println("Adding votes from: " + dirName);
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

	}
}

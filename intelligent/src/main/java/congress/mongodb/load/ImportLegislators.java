package congress.mongodb.load;

import java.io.File;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import utils.JSONUtils;

public class ImportLegislators {

	public static void main(String args[]){
		MongoClient mongo = new MongoClient("localhost", 27017);
		MongoDatabase db = mongo.getDatabase("CongressDB");
		MongoCollection<Document> collection = db.getCollection("Legislators");
				
		JSONArray array = new JSONArray(JSONUtils.getFileAsString(new File("legislators.json")));
		for(int i = 0; i < array.length(); i++){
			insertJSON(((JSONObject) array.get(i)).toString(), collection);
		}
		

		mongo.close();
	}
	
	public static void insertJSON(String json, MongoCollection<Document> collection){
		
		Document dbObject = Document.parse(json);
		collection.insertOne(dbObject);

	}
	
}

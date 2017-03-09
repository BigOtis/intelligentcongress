package congress.mongodb.load;

import java.io.File;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import com.mongodb.client.MongoCollection;

import congress.mongo.facade.MongoFacade;
import utils.JSONUtils;

public class ImportLegislatorData {

	public static void main(String args[]){
		
		System.out.println("Adding legislators to DB from legislators.json...");
		MongoFacade mongo = MongoFacade.getInstance();
		MongoCollection<Document> collection = mongo.db.getCollection("Legislators");
		
		JSONArray legislators = new JSONArray(JSONUtils.getFileAsString(new File("legislators-current.json")));
		for(int i = 0; i < legislators.length(); i++){
			JSONObject legislator = legislators.getJSONObject(i);
			collection.insertOne(Document.parse(legislator.toString()));
		}
	}
	
}

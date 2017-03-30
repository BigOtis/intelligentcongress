package congress.mongodb.load;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.bson.Document;

import com.ibm.watson.watson_developer_cloud.natural_language_understanding.NaturalLanguageUnderstanding;
import com.mongodb.client.MongoCollection;

import congress.mongo.facade.MongoFacade;

public class LoadWatsonStats {
	
	private static NaturalLanguageUnderstanding nlu;
	
	private static boolean getBillsForSenator = false;
	private static String senatorBioID = "M000303";

	public static void main(String args[]) throws FileNotFoundException, IOException{
		
		System.getProperties().load(new FileInputStream("watson.properties"));
    	String username = System.getProperty("watson.analysis.username4");
    	String password = System.getProperty("watson.analysis.password4");
    	
		nlu = new NaturalLanguageUnderstanding(username, password);
		
		MongoFacade mongo = MongoFacade.getInstance();
		MongoCollection<Document> watsonBills = mongo.db.getCollection("WatsonBills");
		MongoCollection<Document> textBills = mongo.db.getCollection("SenateBillText");
		List<String> legislatorBills = (List<String>) mongo.getLegislatorByBioID(senatorBioID).get("sponsored_bills");
			
		System.out.println("Bills for " + senatorBioID + ": " + legislatorBills.size());
		for(Document bill : textBills.find()){
		
			String bill_id = bill.getString("bill_id");
			Document existDoc = watsonBills.find(new Document("bill_id", bill_id)).first();
			if(existDoc == null && (legislatorBills.contains(bill_id) || !getBillsForSenator)){
				String text = bill.getString("bill_text");
				String analysisJSON = getWatsonTextAnalysis(text);
				Document newDoc = Document.parse(analysisJSON);
				newDoc.append("bill_id", bill_id);
				watsonBills.insertOne(newDoc);
				System.out.println(bill_id + " complete");
			}
		}
	}
	
	public static String getWatsonTextAnalysis(String text){
		
		return nlu.analyze(
				Arrays.asList(new String[] {"categories", "emotion", "keywords", "sentiment" }), 
				text, 
				false
		).execute().toString();
	}
		

}

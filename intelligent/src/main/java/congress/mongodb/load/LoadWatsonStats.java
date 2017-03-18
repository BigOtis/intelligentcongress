package congress.mongodb.load;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bson.Document;

import com.ibm.watson.watson_developer_cloud.natural_language_understanding.NaturalLanguageUnderstanding;
import com.mongodb.client.MongoCollection;

import congress.items.IndividualVote;
import congress.mongo.facade.MongoFacade;

public class LoadWatsonStats {
	
	private static NaturalLanguageUnderstanding nlu;

	public static void main(String args[]) throws FileNotFoundException, IOException{
		
		System.getProperties().load(new FileInputStream("watson.properties"));
    	String username = System.getProperty("watson.analysis.username");
    	String password = System.getProperty("watson.analysis.password");
    	
		nlu = new NaturalLanguageUnderstanding(username, password);
		
		MongoFacade mongo = MongoFacade.getInstance();
		Map<String, List<IndividualVote>> votesMap = mongo.createLegislatorNameVoteMap(mongo.queryAllPassageVotes());
		List<IndividualVote> votes = createVotesList(votesMap);
		
		Set<String> bills = new HashSet<>();
		for(IndividualVote vote : votes){
			bills.add(vote.getFullVote().getBillName());
		}
		
		MongoCollection<Document> watsonBills = mongo.db.getCollection("WatsonBills");
		MongoCollection<Document> textBills = mongo.db.getCollection("SenateBillText");

		for(String bill_id : bills){
		
			Document bill = textBills.find(new Document("bill_id", bill_id)).first();
			Document existDoc = watsonBills.find(new Document("bill_id", bill_id)).first();
			if(existDoc == null){
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
		
		return nlu.analyze(nlu.getFeatures(), text, false).execute().toString();
		
	}
		
	public static List<IndividualVote> createVotesList(Map<String, List<IndividualVote>> votesMap){
		List<IndividualVote> votes = new ArrayList<>();
		for(String key : votesMap.keySet()){
			votes.addAll(votesMap.get(key));
		}
		return votes;
	}

}

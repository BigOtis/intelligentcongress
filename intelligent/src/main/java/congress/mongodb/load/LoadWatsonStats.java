package congress.mongodb.load;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bson.Document;

import com.ibm.watson.developer_cloud.natural_language_classifier.v1.NaturalLanguageClassifier;
import com.ibm.watson.developer_cloud.natural_language_classifier.v1.model.Classification;
import com.ibm.watson.developer_cloud.natural_language_classifier.v1.model.Classifiers;
import com.mongodb.DB;
import com.mongodb.client.MongoCollection;

import congress.items.IndividualVote;
import congress.mongo.facade.MongoFacade;
import utils.JSONUtils;

public class LoadWatsonStats {

	public static void main(String args[]){
		
		getWatsonTextAnalysis(JSONUtils.getFileAsString(new File("document.text")));
		
		MongoFacade mongo = MongoFacade.getInstance();
		Map<String, List<IndividualVote>> votesMap = mongo.createLegislatorVoteMap(mongo.queryAllPassageVotes());
		List<IndividualVote> votes = createVotesList(votesMap);
		
		Set<String> bills = new HashSet<>();
		for(IndividualVote vote : votes){
			bills.add(vote.getFullVote().getBillName());
		}
		
		MongoCollection<Document> watsonBills = mongo.db.getCollection("WatsonBills");
		MongoCollection<Document> textBills = mongo.db.getCollection("SenateBillText");

		for(String bill_id : bills){
		
			Document bill = textBills.find(new Document("bill_id", bill_id)).first();
			String text = bill.getString("bill_text");
			System.out.println(bill_id);
		}
		
	}
	
	public static void getWatsonTextAnalysis(String text){
		
		
	}
		
	public static List<IndividualVote> createVotesList(Map<String, List<IndividualVote>> votesMap){
		List<IndividualVote> votes = new ArrayList<>();
		for(String key : votesMap.keySet()){
			votes.addAll(votesMap.get(key));
		}
		return votes;
	}

}

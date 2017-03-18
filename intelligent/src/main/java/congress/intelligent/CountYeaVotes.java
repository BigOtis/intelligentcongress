package congress.intelligent;

import java.util.List;
import java.util.Map;

import org.bson.Document;

import com.mongodb.client.MongoCollection;

import congress.items.IndividualVote;
import congress.mongo.facade.MongoFacade;

public class CountYeaVotes {
	
	public static MongoFacade facade;
	public static MongoCollection<Document> billText;
	public static MongoCollection<Document> watsonData;

	public static double yea = 0;
	public static double total = 0;
	
	public static void main(String args[]){
		
		facade = MongoFacade.getInstance();
		billText = facade.db.getCollection("SenateBillText");
		watsonData = facade.db.getCollection("WatsonBills");
		Map<String, List<IndividualVote>> map = facade.createLegislatorIDVoteMap(facade.queryAllPassageVotes());
		
		for(String id : map.keySet()){
			for(IndividualVote vote : map.get(id)){
				if(vote.getVoteType() == IndividualVote.VOTE_YEA){
					yea++;
					total++;
				}
				if(vote.getVoteType() == IndividualVote.VOTE_NAY){
					total++;
				}
			}
		}
		
		System.out.println("Yea: " + yea);
		System.out.println("Total: " + total);
		System.out.println("% Yea: " + (yea/total));

	}

}

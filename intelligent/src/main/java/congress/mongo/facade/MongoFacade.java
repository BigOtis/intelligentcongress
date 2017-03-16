package congress.mongo.facade;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import congress.items.Bill;
import congress.items.IndividualVote;
import congress.items.Vote;

/**
 * Simple interface that abstracts calls to 
 * the US Congress Mongo database
 * 
 * @author Phillip Lopez - pgl5711@rit.edu
 *
 */
public class MongoFacade {
	
	public enum Party{
		REPUBLICAN, DEMOCRAT, INDEPENDENT
	}
	
	/**
	 * Singleton
	 */
	private static MongoFacade instance = new MongoFacade();
	
	/**
	 * MongoClient API
	 */
	public MongoClient mongo;
	
	/**
	 * The opened database
	 */
	public MongoDatabase db;
	
	public MongoFacade(){
        try {
			System.getProperties().load(new FileInputStream("mongo.properties"));
		} catch (IOException e) {
			System.err.println("MISSING MONGO.PROPERTIES FILE. DB WILL NOT LOAD CORRECTLY.");
		}
		mongo = new MongoClient(System.getProperty("mongo.address"), 
				Integer.valueOf(System.getProperty("mongo.port")));		
		mongo = new MongoClient("localhost", 27017);
		db = mongo.getDatabase("CongressDB");
	}
	
	public static MongoFacade getInstance(){
		return instance;
	}

	/**
	 * Loads every single individual roll call passage vote in the database
	 * 
	 * These are the votes where a bill would actually be passed by the Senate
	 * 
	 * @return List<IndividualVote>
	 */
	public List<IndividualVote> queryAllPassageVotes(){
		
		MongoCollection<Document> collection = db.getCollection("SenateVotes");
		FindIterable<Document> votes = collection.find(new Document("bill", new Document("$exists", true)));
		List<IndividualVote> voteObjects = new ArrayList<>();
		for(Document doc : votes){
			Vote vote = new Vote(doc);
			if(vote.getBill() != null){
				if(vote.hasAssociatedBillText() && "passage".equals(vote.getCategory())){
					voteObjects.addAll(vote.getIndividualVotes());
				}
			}
		}
		return voteObjects;
	}
	
	/**
	 * Pulls down an individual bill given a description of it
	 * 
	 * @param congressNum
	 * @param num
	 * @param houseOrSenate
	 * 
	 * @return A Bill Object
	 */
	public Bill queryBill(String congressNum, String num, String houseOrSenate){
		
		MongoCollection<Document> collection = db.getCollection("SenateBills");
		FindIterable<Document> bills = collection.find(new Document("bill_id", houseOrSenate + num + "-" + "114"));
		if(bills.first() != null){
			return new Bill(bills.first());
		}
		return null;	
	}
	
	/**
	 * Gets the legislator document for the given bioguide_id
	 * @param bioguide_id
	 * @return
	 */
	public Document getLegislatorByBioID(String bioguide_id){
		
		MongoCollection<Document> legislators = db.getCollection("Legislators");
		FindIterable<Document> results = legislators.find(new Document("id.bioguide", bioguide_id));
		Document legislator = results.first();
		
		return legislator;
	}
	
	/**
	 * Gets the latest party affiliation for a legislator
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Party getLegislatorParty(String bioguide_id){
		
		Document legislator = getLegislatorByBioID(bioguide_id);
		if(legislator == null){
			System.err.println("Unable to find legislator: " + bioguide_id);
			return null;
		}
		
		List<Document> terms = (List<Document>) legislator.get("terms");
		Document term = terms.get(terms.size()-1);
		String party = term.getString("party");
		
		if("Republican".equals(party)){
			return Party.REPUBLICAN;
		}
		else if("Democrat".equals(party)){
			return Party.DEMOCRAT;
		}
		else{
			return Party.INDEPENDENT;
		}
	}
	
	/**
	 * Creates a map of individual votes
	 * @param votes
	 * @return
	 */
	public Map<String, List<IndividualVote>> createLegislatorVoteMap(List<IndividualVote> votes){
		Map<String, List<IndividualVote>> legislatorVoteMap = new HashMap<>();
		
		for(IndividualVote vote : votes){
			String name = vote.getDisplayName();
			if(!legislatorVoteMap.containsKey(name)){
				legislatorVoteMap.put(name, new ArrayList<>());
			}
			legislatorVoteMap.get(name).add(vote);			
		}
		
		return legislatorVoteMap;
	}
	
	public static Party getParty(String party){
		if("REPUBLICAN".equals(party)){
			return Party.REPUBLICAN;
		}
		else if("DEMOCRAT".equals(party)){
			return Party.DEMOCRAT;
		}
		else{
			return Party.INDEPENDENT;
		}
	}
}

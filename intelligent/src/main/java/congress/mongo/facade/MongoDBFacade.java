package congress.mongo.facade;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
public class MongoDBFacade {
	
	/**
	 * Singleton
	 */
	private static MongoDBFacade instance = new MongoDBFacade();
	
	/**
	 * MongoClient API
	 */
	public MongoClient mongo;
	
	/**
	 * The opened database
	 */
	public MongoDatabase db;
	
	public MongoDBFacade(){
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
	
	public static MongoDBFacade getInstance(){
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
			if(vote.hasAssociatedBillText() && "passage".equals(vote.getCategory())){
				voteObjects.addAll(vote.getIndividualVotes());
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
	
}

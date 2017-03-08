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
 * Simple interface made to work with the US Congress MongoDB
 * 
 * @author Phillip Lopez - pgl5711@rit.edu
 *
 */
public class MongoDBFacade {
	
	private static MongoDBFacade instance = new MongoDBFacade();
	
	public MongoClient mongo;
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

	public List<IndividualVote> queryAllVotes(){
		
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
	
	public Bill queryBill(String congressNum, String num, String houseOrSenate){
		
		MongoCollection<Document> collection = db.getCollection("SenateBills");
		FindIterable<Document> bills = collection.find(new Document("bill_id", houseOrSenate + num + "-" + "114"));
		if(bills.first() != null){
			return new Bill(bills.first());
		}
		return null;	
	}
	
}

package congress.mongo.facade;

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

public class MongoDBFacade {
	
	private static MongoDBFacade instance = new MongoDBFacade();
	
	public MongoClient mongo;
	public MongoDatabase db;
	
	public MongoDBFacade(){
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

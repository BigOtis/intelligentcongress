package congress.mongo.facade;

import java.util.List;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import congress.items.Bill;
import congress.items.IndividualVote;

public class MongoDBFacade {
	
	public MongoClient mongo;
	public MongoDatabase db;
	
	public MongoDBFacade(){
		mongo = new MongoClient("localhost", 27017);
		db = mongo.getDatabase("CongressDB");
	}

	public List<IndividualVote> queryAllVotes(){
		
		MongoCollection<Document> collection = db.getCollection("SenateVotes");
		FindIterable<Document> votes = collection.find(new Document("bill", new Document("$exists", true)));
		
		return null;
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

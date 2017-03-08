package congress.items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import com.mongodb.client.MongoCollection;

import congress.mongo.facade.MongoDBFacade;

public class Vote {
	
	public static final String[] voteTypeNames = new String[]{"Yea", "Nay", "Present", "Not Voting"};

	private JSONObject voteJSON;
	private Bill bill = null;
	private MongoDBFacade mongo = MongoDBFacade.getInstance();
	private Map<CharSequence, Integer> billWordFrequency = null;
	
	public Vote(Document voteBSON){
		this.voteJSON = new JSONObject(voteBSON.toJson());
	}
	
	public Vote(JSONObject voteJSON){
		this.voteJSON = voteJSON;
	}
	
	public String getBillName(){
		JSONObject bill;
		try{
			bill = voteJSON.getJSONObject("bill");
		}
		catch(Exception e){
			// Not a bill
			return null;
		}
		return getChamber() + bill.getInt("number") + "-" + bill.get("congress");
	}
	
	public Map<CharSequence, Integer> getBillWordFrequency(){
		
		if(billWordFrequency != null){
			return billWordFrequency;
		}
		
		Map<CharSequence, Integer> wordMap = new HashMap<>();
		String bill_id = getChamber() + getBillNumber() + "-" + getCongressNumber();
		MongoCollection<Document> billText = mongo.db.getCollection("SenateBillText");
		Document doc = billText.find(new Document().append("bill_id", bill_id)).first();
		Document wordDoc = (Document) doc.get("wordCounts");
		for(String word : wordDoc.keySet()){
			wordMap.put(word, wordDoc.getInteger(word));
		}
		billWordFrequency = wordMap;
		return wordMap;
	}
	
	public Bill getBill(){
		if(bill != null){
			return bill;
		}
		Bill b = mongo.queryBill(getCongressName(), getBillNumber(), getChamber());
		return b;
	}
	
	public String getBillNumber(){
		JSONObject bill;
		try{
			bill = voteJSON.getJSONObject("bill");
		}
		catch(Exception e){
			// Not a bill
			return null;
		}
		return bill.getInt("number") + "";
	}
	
	public String getChamber(){
		return voteJSON.getString("chamber");
	}
	
	public int getVoteNumber(){
		return voteJSON.getInt("number");
	}
	
	public int getCongressNumber(){
		return voteJSON.getInt("congress");
	}
	
	public String getDate(){
		return voteJSON.getString("date");
	}
	
	public String getVoteID(){
		return voteJSON.getString("vote_id");
	}
	
	public List<IndividualVote> getIndividualVotes(){
		
		List<IndividualVote> votesArray = new ArrayList<>();
		JSONObject individualVotes = voteJSON.getJSONObject("votes");
		for(String type : voteTypeNames){
			JSONArray ary = individualVotes.getJSONArray(type);
			for(int i = 0; i < ary.length(); i++){
				votesArray.add(new IndividualVote(this, type, (JSONObject) ary.getJSONObject(i)));
			}
		}
		return votesArray;
	}
	
	public String getSession(){
		return voteJSON.getString("session");
	}
	
	public String getCongressName(){
		return voteJSON.getString("chamber") + getCongressNumber();
	}
	
	public String getCategory(){
		return voteJSON.getString("category");
	}
	
	public boolean hasAssociatedBillText(){
		
		MongoCollection<Document> billText = mongo.db.getCollection("SenateBillText");
		String bill_id = getChamber() + getBillNumber() + "-" + getCongressNumber();
		return billText.count(new Document().append("bill_id", bill_id)) > 0;

	}
}

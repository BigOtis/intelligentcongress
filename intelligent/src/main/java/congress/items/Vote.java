package congress.items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import com.mongodb.client.MongoCollection;

import congress.mongo.facade.MongoFacade;

public class Vote {
	
	public static final String[] voteTypeNames = new String[]{"Yea", "Nay", "Present", "Not Voting"};

	private JSONObject voteJSON;
	private Bill bill = null;
	private MongoFacade mongo = MongoFacade.getInstance();
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
		MongoCollection<Document> watsonBills = mongo.db.getCollection("WatsonBills");
		MongoCollection<Document> billText = mongo.db.getCollection("SenateBillText");
		Document doc = watsonBills.find(new Document().append("bill_id", bill_id)).first();
		Document textDoc = billText.find(new Document().append("bill_id", bill_id)).first();
		String text = textDoc.getString("text");
		List<Document> keywords = (List<Document>) doc.get("keywords");
		for(Document kw : keywords){
			String[] kws = kw.getString("text").split(" ");
			for(String word : kws){
				word = word.toLowerCase();
				wordMap.put(word, StringUtils.countMatches(word, word));
			}
		}
		List<Document> entities = (List<Document>) doc.get("entities");
		for(Document kw : keywords){
			String[] kws = kw.getString("text").split(" ");
			for(String word : kws){
				word = word.toLowerCase();
				wordMap.put(word, StringUtils.countMatches(word, word));
			}
		}
		billWordFrequency = wordMap;
		return wordMap;
	}
	
	public Bill getBill(){
		if(bill != null){
			return bill;
		}
		Bill b = mongo.queryBill(getCongressName(), getBillNumber(), getChamber());
		bill = b;
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

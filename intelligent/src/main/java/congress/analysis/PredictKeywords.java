package congress.analysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.text.beta.similarity.CosineSimilarity;
import org.bson.Document;

import com.mongodb.client.MongoCollection;

import congress.items.IndividualVote;
import congress.mongo.facade.MongoFacade;

public class PredictKeywords {

	public static List<String> stopwords = Arrays.asList(new String[]{"united", "states", "code", "bills", 
			"date", "114th", "congress", "113th", "america",
			"short", "title", "types", "bill", "the", "they", "and"});
	
	public static MongoFacade facade;
	public static MongoCollection<Document> billText;
	public static MongoCollection<Document> watsonData;

	public static double correct = 0;
	public static double total = 0;
	
	public static void main(String args[]){
		
		facade = MongoFacade.getInstance();
		billText = facade.db.getCollection("SenateBillText");
		watsonData = facade.db.getCollection("WatsonBills");
		Map<String, List<IndividualVote>> map = facade.createLegislatorIDVoteMap(facade.queryAllPassageVotes());

		Set<String> bills = new HashSet<>();
		for(String id : map.keySet()){
			for(IndividualVote vote : map.get(id)){
				bills.add(vote.getFullVote().getBillName());
			}
		}
		
		double allCorrect = 0;
		double allTotal = 0;
		ArrayList<String> billList = new ArrayList<>(bills);
		Random r = new Random();
		while(!billList.isEmpty()){
			
			ArrayList<String> testBills = new ArrayList<>();
			for(int i = 0; i < 5 && !billList.isEmpty(); i++){
				testBills.add(billList.remove(r.nextInt(billList.size())));
			}
			
			doBillPrediction(testBills, map);
			allCorrect += correct;
			allTotal += total;
		}
		
		System.out.println("All Correct: " + allCorrect);
		System.out.println("All Total: " + allTotal);
		System.out.println("All Accuracy: " + (allCorrect/allTotal));
	}
	
	public static void doBillPrediction(List<String> testBills, Map<String, List<IndividualVote>> map){
		
		boolean isTest = true;
		CosineSimilarity cs = new CosineSimilarity();

		correct = 0;
		total = 0;
		for(String id : map.keySet()){
			
			List<IndividualVote> votes = map.get(id);
			Document congressperson = facade.getLegislatorByLisID(id);
			String lastName = ((Document) congressperson.get("name")).getString("last");
			System.out.println("Processing votes for: " + lastName + "...");

			Map<CharSequence, Integer> yeaKeywords = new HashMap<>();
			Map<CharSequence, Integer> nayKeywords = new HashMap<>();

			for(IndividualVote vote : votes){
				
				String subject = vote.getFullVote().getBill().getTopSubject();
				String bill_id = vote.getFullVote().getBillName();
				
				if(!testBills.contains(bill_id) || !isTest){
					Map<CharSequence, Integer> addTo = null;
					if(vote.getVoteType() == IndividualVote.VOTE_YEA){
						addTo = yeaKeywords;
					}
					
					if(vote.getVoteType() == IndividualVote.VOTE_NAY){
						addTo = nayKeywords;
					}
					
					if(addTo != null){
						
						Map<CharSequence, Integer> keywords = vote.getFullVote().getBillWordFrequency();
								//getBillKeywordCount(bill_id, subject);
	
						for(CharSequence word : keywords.keySet()){
							int count = 1;
							if(addTo.containsKey(word)){
								addTo.put(word, addTo.get(word) + count);
							}
							else{
								addTo.put(word, count);
							}
						}
					}
				}
			}
			
			for(IndividualVote vote : votes){
				
				String subject = vote.getFullVote().getBill().getTopSubject();
				String bill_id = vote.getFullVote().getBillName();
				if(testBills.contains(bill_id) || !isTest){
					if(vote.getVoteType() == IndividualVote.VOTE_YEA || vote.getVoteType() == IndividualVote.VOTE_NAY){
						total ++;
						Map<CharSequence, Integer> billKeywords = vote.getFullVote().getBillWordFrequency();
						double yeasim = cs.cosineSimilarity(billKeywords, yeaKeywords);
						double naysim = cs.cosineSimilarity(billKeywords, nayKeywords);
//						double yeasim = numMatches(billKeywords, yeaKeywords);
//						double naysim = numMatches(billKeywords, nayKeywords);
		
						System.out.println("Yea: " + yeasim + "\tNay" + naysim);
						if(yeasim >= naysim && vote.getVoteType() == IndividualVote.VOTE_YEA){
							correct++;
						}
						if(yeasim < naysim && vote.getVoteType() == IndividualVote.VOTE_NAY){
							correct++;
						}
					}
				}
			}
		}
		
		System.out.println("Total Correct: " + correct);
		System.out.println("Total: " + total);
		System.out.println("Accuracy: " + (correct/total));

	}
	
	static Map<String, Map<CharSequence, Integer>> billCounts = new HashMap<>();
	
	public static Map<CharSequence, Integer> getBillKeywordCount(String bill_id, String subject){
		
		if(billCounts.containsKey(bill_id)){
			return billCounts.get(bill_id);
		}
		
		Map<CharSequence, Integer> addTo = new HashMap<>();
		String text = billText.find(new Document("bill_id", bill_id))
				  .first().getString("bill_text").toLowerCase();
		Document doc = watsonData.find(new Document("bill_id", bill_id)).first();
		
		List<String> kwList = new ArrayList<>();
		List<Document> keywords = (List<Document>) doc.get("keywords");
		for(Document kw : keywords){
			String[] kws = kw.getString("text").split(" ");
			for(String word : kws){
				word = word.toLowerCase().replaceAll("[^A-Za-z]","");
				if(word.length() > 3 && !stopwords.contains(word)){
					kwList.add(word);
				}
			}
		}
				
		for(String word : kwList){
			int count = 1;//StringUtils.countMatches(text, word);
			if(addTo.containsKey(word)){
				addTo.put(word, addTo.get(word) + count);
			}
			else{
				addTo.put(word, count);
			}
		}
		
		billCounts.put(bill_id, addTo);
		return addTo;
	}
	
	public static int numMatches(Map<CharSequence, Integer> vec1, Map<CharSequence, Integer> vec2){
		int num = 0;
		
		for(CharSequence word : vec1.keySet()){
			if(vec2.containsKey(word)){
				
				int c1 = vec1.get(word);
				int c2 = vec2.get(word);
				//num += Math.min(c1, c2);
				num += 1;
				
			}
		}
		
		return num;
	}
}

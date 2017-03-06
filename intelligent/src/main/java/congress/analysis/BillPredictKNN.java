package congress.analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.text.beta.similarity.CosineSimilarity;

import congress.items.Bill;
import congress.items.IndividualVote;
import congress.mongo.facade.MongoDBFacade;

public class BillPredictKNN {

	public static void main(String args[]){
		
		MongoDBFacade db = MongoDBFacade.getInstance();
		List<IndividualVote> votes = db.queryAllVotes();
		System.out.println("Found: " + votes.size() + " votes");
				
		Map<String, List<IndividualVote>> legislatorVoteMap = createLegislatorVoteMap(votes);
		
		// Total Number of votes for each Senator
		for(String name : legislatorVoteMap.keySet()){
			System.out.println("Name: " + name + "\t# Votes: " 
				+ legislatorVoteMap.get(name).size());
		}
		
		String schumer = "Schumer (D-NY)";
		BillPredictKNN bpknn = new BillPredictKNN();
		bpknn.doKNN(legislatorVoteMap.get(schumer));
		
	}
	
	public void doKNN(List<IndividualVote> votes){
				
		List<IndividualVote> billVotes = new ArrayList<>();
		for(IndividualVote vote : votes){
			Bill b = vote.getFullVote().getBill(MongoDBFacade.getInstance());
			if(b != null){
				billVotes.add(vote);
			}
		}
		
		votes = billVotes;
		IndividualVote v1 = votes.remove(0);
		BillVoteComparator bvc = new BillVoteComparator(v1, MongoDBFacade.getInstance());
		Collections.sort(votes, bvc);
		
		
		System.out.println("V1:\t" + v1.getVoteType());
		for(int i = 0; i < 5; i++){
			String result = "Result:\t" + votes.get(i).getVoteType();
			System.out.println(result);
		}
		
	}
	
	public static Map<String, List<IndividualVote>> createLegislatorVoteMap(List<IndividualVote> votes){
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
	
	
	public class BillVoteComparator implements Comparator<IndividualVote>{
		
		IndividualVote mainVote;
		MongoDBFacade db;
		Bill mainBill;
		Map<CharSequence, Integer> mainWordFrequency;
		
		public BillVoteComparator(IndividualVote mainVote, MongoDBFacade db){
			this.mainVote = mainVote;
			this.db = db;
			this.mainBill = mainVote.getFullVote().getBill(db);
			this.mainWordFrequency = getWordFrequency(mainBill);
		}

		@Override
		public int compare(IndividualVote v1, IndividualVote v2) {
			
			CosineSimilarity cs = new CosineSimilarity();
			Bill b1 = v1.getFullVote().getBill(db);
			Bill b2 = v2.getFullVote().getBill(db);
			
			Double v1cs = cs.cosineSimilarity(mainWordFrequency, getWordFrequency(b1));
			Double v2cs = cs.cosineSimilarity(mainWordFrequency, getWordFrequency(b2));
						
			return v2cs.compareTo(v1cs);
		}
		
		public Map<CharSequence, Integer> getWordFrequency(Bill bill){
			Map<CharSequence, Integer> wordMap = new HashMap<>();
			
			if(bill == null){
				return wordMap;
			}
			
			String text = bill.getTitle();
			
			if(text == null){
				return wordMap;
			}
			
			String[] words = text.split(" ");
			for(String word : words){
				if(wordMap.containsKey(word)){
					wordMap.put(word, wordMap.get(word)+1);
				}
				else{
					wordMap.put(word, 1);
				}
			}
			return wordMap;
		}
		
	}
}

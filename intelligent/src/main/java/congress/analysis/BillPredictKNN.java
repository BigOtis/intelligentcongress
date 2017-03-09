package congress.analysis;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.text.beta.similarity.CosineSimilarity;

import congress.items.IndividualVote;
import congress.mongo.facade.MongoFacade;
import utils.JSONUtils;

/**
 * This experiment tests the effectiveness of KNN on 
 * on full bill text and outputs the results
 * @author Phillip Lopez - pgl5711@rit.edu
 *
 */
public class BillPredictKNN {

	public static void main(String args[]){
		
		MongoFacade db = MongoFacade.getInstance();
		List<IndividualVote> votes = db.queryAllPassageVotes();
		System.out.println("Found: " + votes.size() + " votes");
				
		Map<String, List<IndividualVote>> legislatorVoteMap = createLegislatorVoteMap(votes);
		
		// Total Number of votes for each Senator
		for(String name : legislatorVoteMap.keySet()){
			System.out.println("Name: " + name + "\t# Votes: " 
				+ legislatorVoteMap.get(name).size());
		}
		
		BillPredictKNN knn = new BillPredictKNN();
		
		// Do KNN on a text file
//		int textYea = 0;
//		int textNay = 0;
//		Map<CharSequence, Integer> toGuess = getWordFrequency(JSONUtils.getFileAsString(new File("ACAReplace.txt")));
//
//		for(String individual : legislatorVoteMap.keySet()){
//			boolean result = knn.doKNNForText(votes, toGuess, 5);
//			if(result){
//				textYea++;
//				System.out.println(individual + " : " + "YEA");
//			}
//			else{
//				textNay++;
//				System.out.println(individual + " : " + "NAY");
//			}
//		}
//		
//		System.out.println("Yea: " + textYea + " Nay: " + textNay);
		
		// Count total number yea / nay votes
		int yea = 0;
		int nay = 0;
		for(String individual : legislatorVoteMap.keySet()){
			
			List<IndividualVote> indVotes = legislatorVoteMap.get(individual);
			for(IndividualVote vote : indVotes){
				String voteType = vote.getVoteType();
				if(IndividualVote.VOTE_YEA.equals(voteType)){
					yea++;
				}
				else if(IndividualVote.VOTE_NAY.equals(voteType)){
					nay++;
				}
			}
			
		}

		System.out.println("Total yea: " + yea);
		System.out.println("Total nay: " + nay);

		// Test our training set with KNN
		int numCorrect = 0;
		int numVotes = 0;
		for(String individual : legislatorVoteMap.keySet()){
			System.out.println("Predicting votes for: " + individual);
			List<IndividualVote> indVotes = legislatorVoteMap.get(individual);
			
			int numVotesFor = 0;
			int numCorrectFor = 0;
			
			for(IndividualVote vote : indVotes){
				
				if(numVotes % 25 == 0){
					System.out.println("\tNum Total: " + numVotes);
					System.out.println("\tNum Correct: " + numCorrect);
				}
				
				String voteType = vote.getVoteType();
				if(IndividualVote.VOTE_YEA.equals(voteType) || 
						IndividualVote.VOTE_NAY.equals(voteType)){
				
					boolean expected = IndividualVote.VOTE_YEA.equals(voteType);
					List<IndividualVote> votesCopy = new ArrayList<>(indVotes);
					boolean result = knn.doKNN(votesCopy, vote, 11);
					
					if(result == expected){
						numCorrect++;
						numCorrectFor++;
					}
					numVotes++;
					numVotesFor++;
				}
			}
			
			System.out.println("Num Total For " + individual + " : " + numVotesFor);
			System.out.println("Num Correct For " + individual + " : " + numCorrectFor);
		}
		
		System.out.println("Num Total: " + numVotes);
		System.out.println("Num Correct: " + numCorrect);

	}
	
	/**
	 * Predicts whether or not the given vote is a yay or nay
	 * based on the previous vote results
	 * @param votes - previous votes for a single congressperson
	 * @param toGuess - the vote to guess
	 * @return true for yea, false for nay
	 */
	public boolean doKNN(List<IndividualVote> votes, IndividualVote toGuess, int k){
				
		k = k + 1;
		setupCossim(votes, toGuess);
		BillVoteComparator bvc = new BillVoteComparator();
		Collections.sort(votes, bvc);
		int yea = 0;
		int nay = 0;
		
		for(int i = 1; i < k && i < votes.size(); i++){
			IndividualVote iv = votes.get(i);
			
			// Congress person voted yes
			if(IndividualVote.VOTE_YEA.equals(iv.getVoteType())){
				yea++;
			}
			// Congress person voted no
			else if(IndividualVote.VOTE_NAY.equals(iv.getVoteType())){
				nay++;
			}
			// Congress person didn't vote this time, try the next best one
			else{
				k++;
			}
		}
		return yea > nay;
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
	
	public void setupCossim(List<IndividualVote> votes, IndividualVote toGuess){
		
		Map<CharSequence, Integer> guessFreq = toGuess.getFullVote().getBillWordFrequency();
		for(IndividualVote vote : votes){
			Map<CharSequence, Integer> voteFreq = vote.getFullVote().getBillWordFrequency();
			CosineSimilarity cs = new CosineSimilarity();
			vote.cossim = cs.cosineSimilarity(guessFreq, voteFreq);
		}
	}
	
	/**
	 * A comparator used to compare IndividualVotes 
	 * using their bill word frequency.
	 * 
	 * Uses the cosine similarity between vectors to
	 * determine how closely related the bills are
	 *
	 */
	public class BillVoteComparator implements Comparator<IndividualVote>{
		
		@Override
		public int compare(IndividualVote v1, IndividualVote v2) {						
			return v2.cossim.compareTo(v1.cossim);
		}	
	}
	
	public boolean doKNNForText(List<IndividualVote> votes, Map<CharSequence, Integer> toGuess, int k){
		
		k = k + 1;
		setupCossim(votes, toGuess);
		BillVoteComparator bvc = new BillVoteComparator();
		Collections.sort(votes, bvc);
		int yea = 0;
		int nay = 0;
		
		for(int i = 1; i < k; i++){
			IndividualVote iv = votes.get(i);
			
			System.out.println("\tT: [" + iv.getFullVote().getBillName() + "]\t" + iv.cossim);
			// Congress person voted yes
			if(IndividualVote.VOTE_YEA.equals(iv.getVoteType())){
				yea++;
			}
			// Congress person voted no
			else if(IndividualVote.VOTE_NAY.equals(iv.getVoteType())){
				nay++;
			}
			// Congress person didn't vote this time, try the next best one
			else{
				k++;
			}
		}
		return yea > nay;	
	}
	
	public static Map<CharSequence, Integer> getWordFrequency(String text){
		Map<CharSequence, Integer> wordMap = new HashMap<>();
				
		String[] words = text.split(" ");
		for(String word : words){
			word = word.replaceAll("[^a-zA-Z\\s]", "").replaceAll(" ", "");
			if(word.length() < 3){
				// Don't add short words
			}
			else if(wordMap.containsKey(word)){
				wordMap.replace(word, wordMap.get(word)+1);
			}
			else{
				wordMap.put(word, 1);
			}
		}
		return wordMap;
	}
	
	public void setupCossim(List<IndividualVote> votes, Map<CharSequence, Integer> guessFreq){
		for(IndividualVote vote : votes){
			Map<CharSequence, Integer> voteFreq = vote.getFullVote().getBillWordFrequency();
			CosineSimilarity cs = new CosineSimilarity();
			vote.cossim = cs.cosineSimilarity(guessFreq, voteFreq);
		}
	}
}

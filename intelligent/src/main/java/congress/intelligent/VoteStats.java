package congress.intelligent;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import congress.items.Bill;
import congress.items.IndividualVote;
import congress.items.Vote;
import utils.JSONUtils;

public class VoteStats {
	
	public int billVotes = 0;
	
	public static void main( String[] args ){
		
		VoteStats vs = new VoteStats();
//		vs.printStats("s113", "2013");
//		vs.printStats("s113", "2014");
//		vs.printStats("s114", "2015");
//		vs.printStats("s114", "2016");	
//		vs.getIndividualVoteStats("S270", "s114", "2016");
		Map<String, List<IndividualVote>> personVotes = vs.loadAllVotes("s114", "2016");
		vs.countVoteTypes(personVotes.get("S270"));
	}
	
	public void countVoteTypes(List<IndividualVote> votes){
		// Schumer
//		System.out.println("Total Number of votes: " + votes.size());
//		Set<String> yeaSubjects = new HashSet<>();
//		Set<String> naySubjects = new HashSet<>();
//
//		int yea = 0;
//		int nay = 0;
//		int nv = 0;
//		int p = 0;
//		for(IndividualVote vote : votes){
//			if(vote.getVoteType().equals(IndividualVote.VOTE_YEA)){
//				Bill bill = vote.getFullVote().getBill();
//				if(bill != null){
//					yeaSubjects.add(bill.getTopSubject());
//				}
//				yea++;
//			}
//			if(vote.getVoteType().equals(IndividualVote.VOTE_NAY)){
//				Bill bill = vote.getFullVote().getBill();
//				if(bill != null){
//					naySubjects.add(bill.getTopSubject());
//				}
//				nay++;
//			}
//			if(vote.getVoteType().equals(IndividualVote.VOTE_NOT_VOTING)){
//				nv++;
//			}
//			if(vote.getVoteType().equals(IndividualVote.VOTE_PRESENT)){
//				p++;
//			}
//		}
//		System.out.println("Yea: " + yea);
//		System.out.println("Nay: " + nay);
//		System.out.println("Not Voting: " + nv);
//		System.out.println("Present: " + p);
//		System.out.println("Yea Subj: " + yeaSubjects);
//		System.out.println("Nay Subj: " + naySubjects);



	}
	
	public Map<String, List<IndividualVote>> loadAllVotes(String congress, String year){
		
		Map<String, List<IndividualVote>> personVotes = new HashMap<>();
		
		File votesDir = new File(congress + "_" + year + "_votes");
		// Not Voting - Nay - Yea
		for(File voteFile : votesDir.listFiles()){
			JSONObject voteJSON = JSONUtils.getJSONObject(voteFile);
			Vote vote = new Vote(voteJSON);
			for(IndividualVote iVote : vote.getIndividualVotes()){
				mapAddToList(personVotes, iVote.getID(), iVote);
			}
		}
		
		return personVotes;
	}
	
	public void getIndividualVoteStats(String id, String congress, String year){
		File votesDir = new File(congress + "_" + year + "_votes");
		// Not Voting - Nay - Yea
		for(File voteFile : votesDir.listFiles()){
			JSONObject vote = JSONUtils.getJSONObject(voteFile);
			JSONObject voteBill = JSONUtils.getVoteBill(vote);
			if(voteBill != null){
				JSONObject votes = vote.getJSONObject("votes");
				JSONArray ary = votes.getJSONArray("Yea");
				for(int i = 0; i < ary.length(); i++){
					JSONObject personVote = (JSONObject) ary.get(i);
					if(personVote.getString("id").equals(id)){
						System.out.println("Yea");
					}
				}
			}
		}
	}

	public void printStats(String congress, String year){
		
		Map<String, Integer> billVotesCount = new HashMap<>();
		//File billsDir = new File(congress + "_" + year + "_bills");
		File votesDir = new File(congress + "_" + year + "_votes");

		for(File voteFile : votesDir.listFiles()){
			JSONObject vote = JSONUtils.getJSONObject(voteFile);
			JSONObject voteBill = JSONUtils.getVoteBill(vote);
			if(voteBill != null){
				putPlus(billVotesCount, voteBill.getInt("number")+"");
				billVotes += 1;
			}
		}
		
		printMap(billVotesCount);
	}
	
	public void printMap(Map<?,?> map){
		for(Object key : map.keySet()){
			System.out.println(key + " : " + map.get(key));
		}
	}
	
	public void mapAddToList(Map<String, List<IndividualVote>> map, String key, IndividualVote value){
		if(map.get(key) == null){
			List<IndividualVote> list = new ArrayList<>();
			list.add(value);
			map.put(key, list);
		}
		else{
			map.get(key).add(value);
		}
	}
	
	public void putPlus(Map<String, Integer> map, String key){
		if(map.get(key) == null){
			map.put(key, 1);
		}
		else{
			map.put(key,map.get(key)+1);
		}
	}
	
}

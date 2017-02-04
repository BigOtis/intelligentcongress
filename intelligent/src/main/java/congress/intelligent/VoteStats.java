package congress.intelligent;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import utils.JSONUtils;

public class VoteStats {
	
	public int billVotes = 0;
	
	public static void main( String[] args ){
		
		VoteStats vs = new VoteStats();
		vs.printStats("s113", "2013");
		vs.printStats("s113", "2014");
		vs.printStats("s114", "2015");
		vs.printStats("s114", "2016");	
		
		System.out.println("Total number bill votes: " + vs.billVotes);
	}

	public void printStats(String congress, String year){
		
		Map<String, Integer> billVotesCount = new HashMap<>();
		File billsDir = new File(congress + "_" + year + "_bills");
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
	
	public void putPlus(Map<String, Integer> map, String key){
		if(map.get(key) == null){
			map.put(key, 1);
		}
		else{
			map.put(key,map.get(key)+1);
		}
	}
	
}

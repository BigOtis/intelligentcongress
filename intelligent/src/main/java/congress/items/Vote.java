package congress.items;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import utils.JSONUtils;

public class Vote {
	
	public static final String[] voteTypeNames = new String[]{"Yea", "Nay", "Present", "Not Voting"};

	private JSONObject voteJSON;
	
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
		return "s" + bill.getInt("number") + "-" + bill.get("congress");
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
	
	public Bill getBill(){
		
		File dir = new File(getCongressName() + "_bills");
		File[] files = dir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				String billName = getBillName();
				if(pathname.getName().equals(billName)){
					return true;
				}
				return false;
			}
		});
		if(files.length == 0){
			System.err.println("Bill not found: " + getBillName());
			return null;
		}
		
		return new Bill(JSONUtils.getJSONObject(files[0]));
	}
}

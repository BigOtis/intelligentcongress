package congress.items;

import org.json.JSONObject;

public class IndividualVote {
	
	public static final String VOTE_YEA = "Yea";
	public static final String VOTE_NAY = "Nay";
	public static final String VOTE_PRESENT = "Present";
	public static final String VOTE_NOT_VOTING = "Not Voting";

	private JSONObject vote;
	private String voteType;
	private Vote fullVote;
	
	public IndividualVote(Vote fullVote, String voteType, JSONObject IndividualVote){
		this.fullVote = fullVote;
		this.vote = IndividualVote;
		this.voteType = voteType;
	}
	
	public Vote getFullVote(){
		return fullVote;
	}
	
	public String getVoteType(){
		return voteType;
	}
		
	public String getDisplayName(){
		return vote.getString("display_name");
	}
	
	public String getFirstName(){
		return vote.getString("first_name");
	}
	
	public String getID(){
		return vote.getString("id");
	}
	
	public String getParty(){
		return vote.getString("party");
	}
	
	public String getState(){
		return vote.getString("state");
	}
}

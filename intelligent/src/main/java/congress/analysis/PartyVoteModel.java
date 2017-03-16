package congress.analysis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import congress.items.Bill;
import congress.items.IndividualVote;
import congress.mongo.facade.MongoFacade;
import congress.mongo.facade.MongoFacade.Party;

public class PartyVoteModel {

	public static void main(String args[]){
		
		MongoFacade mongo = MongoFacade.getInstance();
		Map<String, List<IndividualVote>> votesMap = mongo.createLegislatorVoteMap(mongo.queryAllPassageVotes());
		List<IndividualVote> votes = createVotesList(votesMap);
			
		int total = 0;
		int correct = 0;
		for(IndividualVote vote : votes){

			boolean guessYes = votedYes(vote);
			if(vote.getVoteType().equals(IndividualVote.VOTE_YEA) && guessYes){
				correct++;
				total++;
			}
			else if(vote.getVoteType().equals(IndividualVote.VOTE_NAY) && !guessYes){
				correct++;
				total++;
			}
			else{
				if(vote.getVoteType().equals(IndividualVote.VOTE_YEA) || 
						vote.getVoteType().equals(IndividualVote.VOTE_NAY)){
					total++;
				}
			}
		}
		
		System.out.println("Total: " + total);
		System.out.println("Correct: " + correct);

	}
	
	static double threshold = .4;
	
	public static boolean votedYes(IndividualVote vote){
		
		if(vote != null){
			return true;
		}
		
		String party = vote.getParty();
		Bill bill = vote.getFullVote().getBill();
		
		double reps = bill.getNumRepublicanSponsors();
		double dems = bill.getNumDemocraticSponsors();
		Party mainParty = bill.getSponsorParty();
		
		if(party.equals("R") && Party.REPUBLICAN == mainParty){
			return true;
		}
		else if(party.equals("D") && Party.DEMOCRAT == mainParty){
			return true;
		}
		else if(party.equals("I") && Party.INDEPENDENT == mainParty){
			return true;
		}
		
		double repPercent = reps / (reps + dems);
		double demsPercent = reps / (reps + dems);
		
		if(repPercent >= threshold || demsPercent >= threshold){
			return true;
		}

		return false;
	}
	
	public static List<IndividualVote> createVotesList(Map<String, List<IndividualVote>> votesMap){
		List<IndividualVote> votes = new ArrayList<>();
		for(String key : votesMap.keySet()){
			votes.addAll(votesMap.get(key));
		}
		return votes;
	}
}

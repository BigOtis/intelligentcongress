package congress.export;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bson.Document;

import com.mongodb.client.MongoCollection;

import congress.items.IndividualVote;
import congress.mongo.facade.MongoFacade;

public class ExportBillText {

	public static void main(String [] args) throws FileNotFoundException{
		
		MongoFacade facade = MongoFacade.getInstance();
		MongoCollection<Document> billText = facade.db.getCollection("SenateBillText");
		Map<String, List<IndividualVote>> map = facade.createLegislatorIDVoteMap(facade.queryAllPassageVotes());
		
		// Schumer
		String id = "S270";
		List<IndividualVote> votes = map.get(id);
		Document congressperson = facade.getLegislatorByLisID(id);
		String lastName = ((Document) congressperson.get("name")).getString("last");
		
		File dir = new File(lastName + "_" + id);
		dir.mkdir();
		File yesDir = new File(lastName + "_" + id + "\\yes") ;
		yesDir.mkdir();
		File noDir = new File(lastName + "_" + id + "\\no") ;
		noDir.mkdir();
		
		Set<String> allYes = new HashSet<>();
		Set<String> allNo = new HashSet<>();

		Set<String> doubleVotes = new HashSet<>();
		
		for(IndividualVote vote : votes){
			String bill_id = vote.getFullVote().getBillName();
			if(vote.getVoteType() == IndividualVote.VOTE_YEA){
				if(allNo.contains(bill_id)){
					doubleVotes.add(bill_id);
				}
				allYes.add(bill_id);
			}
			
			if(vote.getVoteType() == IndividualVote.VOTE_NAY){
				if(allYes.contains(bill_id)){
					doubleVotes.add(bill_id);
				}
				allNo.add(bill_id);
			}
		}
		
		PrintWriter pw = new PrintWriter(new File(lastName + "_" + id + "_votes.txt"));
		for(IndividualVote vote : votes){
			
			String bill_id = vote.getFullVote().getBillName();
			
			if(!doubleVotes.contains(bill_id)){
				Document bill = billText.find(new Document("bill_id", bill_id)).first();
			    String line = "\"" + bill.getString("bill_text").replaceAll("[^A-Za-z0-9]"," ").replaceAll("\\s+", " ").substring(120) + "\"";
				if(line != null){
					line = line.replaceAll("[^A-Za-z0-9]"," ").replaceAll("\\s+", " ");
					if(vote.getVoteType() == IndividualVote.VOTE_YEA){
						line = "[yea]" + "[" + bill_id + "]" + "[" + line + "]";
						pw.println(line);
					}
					
					if(vote.getVoteType() == IndividualVote.VOTE_NAY){
						line = "[nay]" + "[" + bill_id + "]" + "[" + line + "]";
						pw.println(line);
					}
				}
			}
		}
		
		pw.flush();
		pw.close();
	}
}

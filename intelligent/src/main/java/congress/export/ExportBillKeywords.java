package congress.export;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;

import com.mongodb.client.MongoCollection;

import congress.items.IndividualVote;
import congress.mongo.facade.MongoFacade;

public class ExportBillKeywords {

	public static void main(String [] args) throws FileNotFoundException{
		
		MongoFacade facade = MongoFacade.getInstance();
		MongoCollection<Document> watsonData = facade.db.getCollection("WatsonBills");
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
		
			
		for(IndividualVote vote : votes){
			
			String bill_id = vote.getFullVote().getBillName();
			
			if(!doubleVotes.contains(bill_id)){
				
				Document doc = watsonData.find(new Document("bill_id", bill_id)).first();
				
				List<String> kwList = new ArrayList<>();
				List<Document> keywords = (List<Document>) doc.get("keywords");
				for(Document kw : keywords){
					String[] kws = kw.getString("text").split(" ");
					for(String word : kws){
						word = word.toLowerCase().replaceAll("[^A-Za-z]","");
						if(word.length() > 3){
							kwList.add(word);
						}
					}
				}
				
				File billFile = null;
				if(vote.getVoteType() == IndividualVote.VOTE_YEA){
					billFile = new File(yesDir.getPath() + "\\" + bill_id + ".txt");
				}
				
				if(vote.getVoteType() == IndividualVote.VOTE_NAY){
					billFile = new File(noDir.getPath() + "\\" + bill_id + ".txt");
				}
				
				if(billFile != null){
					PrintWriter pw = new PrintWriter(billFile);
					pw.print(StringUtils.join(kwList, " "));
					pw.flush();
					pw.close();
				}
			}
		}
	}
	
}

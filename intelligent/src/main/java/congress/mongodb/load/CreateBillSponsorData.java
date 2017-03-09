package congress.mongodb.load;

import java.util.List;

import org.bson.Document;

import com.mongodb.client.MongoCollection;

import congress.mongo.facade.MongoFacade;
import congress.mongo.facade.MongoFacade.Party;

/**
 * Goes through all bills and creates some metadata
 * about the sponsors and the party they serve
 * 
 * @author Phillip Lopez - pgl5711@rit.edu
 */
public class CreateBillSponsorData {

	public static void main(String args[]){
		
		MongoFacade mongo = MongoFacade.getInstance();
		MongoCollection<Document> bills = mongo.db.getCollection("SenateBills");
		for(Document bill : bills.find()){
			
			Document sponsor = (Document) bill.get("sponsor");
			String sponsor_id = sponsor.getString("bioguide_id");
			Party party = mongo.getLegislatorParty(sponsor_id);
			
			int republicans = 0;
			int democrats = 0;
			int independents = 0;
			
			List<Document> cosponsors = (List<Document>) bill.get("cosponsors");
			for(Document cosponsor : cosponsors){
				
				String cosponsor_id = cosponsor.getString("bioguide_id");
				Party cosponsor_party = mongo.getLegislatorParty(cosponsor_id);
				switch(cosponsor_party){
					case DEMOCRAT: 		democrats++; break;
					case REPUBLICAN:	republicans++; break;
					default:			independents++;
				}
			}
			
			bill.append("rep_sponsors", republicans);
			bill.append("dem_sponsors", democrats);
			bill.append("ind_sponsors", independents);
			bill.append("sponsor_party", party.toString());
			
			bills.replaceOne(new Document("_id", bill.get("_id")), bill);
		}
		
	}
	
}

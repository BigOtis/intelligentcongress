package congress.sponsors;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.mongodb.client.MongoCollection;

import congress.mongo.facade.MongoFacade;

public class CountSponsors {

	MongoFacade facade = MongoFacade.getInstance();
	MongoCollection<Document> bills = facade.db.getCollection("SenateBills");
	MongoCollection<Document> legislators = facade.db.getCollection("Legislators");

	public static void main(String args[]){
		
		CountSponsors cs = new CountSponsors();
		cs.updateLegislatorSponsorInfo();
		
	}
	
	public void updateLegislatorSponsorInfo(){
		
		for(Document bill : bills.find()){
			
			String bill_id = bill.getString("bill_id");

			Document sponsor = (Document) bill.get("sponsor");
			addBillToLegislator(sponsor, bill_id);
			
			List<Document> cosponsors = (List<Document>) bill.get("cosponsors");
			if(cosponsors != null){
				System.out.println(bill_id + ":\t Num:\t" + (cosponsors.size()+1));
				for(Document cosponsor : cosponsors){
					addBillToLegislator(cosponsor, bill_id);
				}
			}
		}
		
	}
	
	public void addBillToLegislator(Document sponsor_doc, String bill_id){
		
		String bio_id = sponsor_doc.getString("bioguide_id");
		Document legislator = facade.getLegislatorByBioID(bio_id);
		if(legislator != null){
			Object obj = legislator.get("sponsored_bills");
			List<String> sponsored = new ArrayList<String>();
			if(obj != null && obj instanceof List<?>){
				sponsored.addAll((List<String>) obj);
			}
			if(!sponsored.contains(bill_id)){
				sponsored.add(bill_id);
			}
			legislator.append("sponsored_bills", sponsored);
			legislators.findOneAndReplace(new Document("_id", legislator.get("_id")), legislator);
		}
		else{
			System.err.println("Legislator not found: " + bio_id);
		}
	}
}

package congress.items;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import congress.mongo.facade.MongoFacade;
import congress.mongo.facade.MongoFacade.Party;

/**
 * I'm just a bill...
 * @author pgl57
 *
 */
public class Bill {

	private JSONObject billJSON;
	
	public Bill(Document doc){
		this.billJSON = new JSONObject(doc.toJson());
	}
	
	public Bill (JSONObject billJSON){
		this.billJSON = billJSON;
	}
	
	public String getBillID(){
		return billJSON.getString("bill_id");
	}
	
	public int getCongressNum(){
		return billJSON.getInt("congress");
	}
	
	public String getTopSubject(){
		try{
			return billJSON.getString("subjects_top_term");
		}
		catch(Exception e){
			return "";
		}
	}
	
	public String getTitle(){
		return billJSON.getString("official_title");
	}
	
	public String getPopularTitle(){
		return billJSON.getString("popular_title");
	}
	
	public List<String> getSubjects(){
		JSONArray ary = billJSON.getJSONArray("subjects");
		List<String> strAry = new ArrayList<>();
		for(int i = 0; i < ary.length(); i++){
			strAry.add((String) ary.get(i));
		}
		return strAry;
	}
	
	public String getSummaryText(){
		try{
			return billJSON.getJSONObject("summary").getString("text");
		}
		catch(Exception e){
			System.err.println("No summary text");
			return null;
		}
	}
	
	public String getIntroducedDate(){
		return billJSON.getString("introduced_at");
	}
	
	public int getNumRepublicanSponsors(){
		return billJSON.getInt("rep_sponsors");
	}
	
	public int getNumDemocraticSponsors(){
		return billJSON.getInt("dem_sponsors");

	}
	
	public Party getSponsorParty(){
		return MongoFacade.getParty(billJSON.getString("sponsor_party"));
	}
}

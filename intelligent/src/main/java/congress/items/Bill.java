package congress.items;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

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
		return billJSON.getJSONObject("summary").getString("text");
	}
	
	public String getIntroducedDate(){
		return billJSON.getString("introduced_at");
	}
}

package congress.analysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;

import com.mongodb.client.MongoCollection;

import congress.mongo.facade.MongoFacade;

public class TopBillKeywords {
	
	public static List<String> stopWords = Arrays.asList(new String[]{
			"united states",
			"U S Government Publishing",
			"Congressional Bills",
			"Congress assembled SECTION",
			"SHORT TITLE",
			"subsection",
			"section",
			"Session S ",
			"senate",
			"Secretary",
			"114th CONGRESS",
			"United States Code",
			"Office",
			"113th CONGRESS",
			"House",
			"Committee",
			"Introduced",
			"paragraph",
			"U S C",
			"America",
			"title",
			"Internal Revenue Code",
			"date",
			"enactment",
			"general",
			"purposes",
			"subparagraph",
			"purposes ",
			"Public Law",
			"Representatives",
			"end",
			"General ",
			"following",
			"United States Code",
			"Act", 
			"DOC", 
			"term",
			"following new paragraph",
			"following new subsection",
			"clause",
			"Mr ",
			"case",
			"percent",
			"department",
			"Date ",
			"U S C ",
	});

	public static void main(String args[]){
		
		MongoFacade mongo = new MongoFacade();
		MongoCollection<Document> wbills = mongo.db.getCollection("WatsonBills");
		
		Map<String, Integer> wordCount = new HashMap<>();	
		int i = 0;
		
		for(Document doc : wbills.find()){
			if((i++) % 1000 == 0){
				System.out.println("At bill " + (i-1));
			}
			List<Document> keywords = (List<Document>) doc.get("keywords");
			List<Document> cleanedWords = new ArrayList<>();
			for(Document kw : keywords){
				String text = kw.getString("text");
				if(!stopWords.contains(text)){
					String cleaned = text.replaceAll("[^A-Za-z0-9]+", " ").replaceAll("\\s+", " ");;
					Document cleanDoc = new Document();
					cleanDoc.append("text", cleaned);
					cleanDoc.append("relevance", kw.get("relevance"));
					cleanedWords.add(cleanDoc);
					if(wordCount.containsKey(text)){
						wordCount.put(cleaned, wordCount.get(text) + 1);
					}
					else{
						wordCount.put(cleaned, 1);
					}
				}
			}
			doc.replace("keywords", cleanedWords);
			wbills.findOneAndReplace(new Document("_id", doc.get("_id")), doc);
		}
		
		List<String> words = new ArrayList<>(wordCount.keySet());
		Collections.sort(words, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return wordCount.get(o1).compareTo(wordCount.get(o2));
			}
		});
		
		for(String word : words){
			System.out.println(word + ":\t\t" + wordCount.get(word));
		}	
	}
}

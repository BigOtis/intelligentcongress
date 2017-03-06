package congress.mongodb.load;

import org.bson.Document;

import com.mongodb.client.MongoCollection;

import congress.mongo.facade.MongoDBFacade;

public class CreateWordCountVector {
	
	public static void main(String args[]){
		
		MongoDBFacade facade = MongoDBFacade.getInstance();
		MongoCollection<Document> collection = facade.db.getCollection("SenateBillText");
		
		int count = 1;
		System.out.println("Adding word counts for bills...");
		for(Document doc : collection.find()){
			if((count++ % 100) == 0){
				System.out.println("Processing bill number: " + (count-1));
			}
			if(!doc.containsKey("wordCounts")){
				String text = doc.getString("bill_text");
				Document wordCount = getWordFrequency(text);
				doc.append("wordCounts", wordCount);
				collection.replaceOne(new Document().append("bill_id", doc.getString("bill_id")), doc);
			}
		}
		System.out.println("All done...");	
	}
	
	public static Document getWordFrequency(String text){
		Document wordMap = new Document();
				
		String[] words = text.split(" ");
		for(String word : words){
			word = word.replaceAll("[^a-zA-Z\\s]", "").replaceAll(" ", "");
			if(word.length() < 3){
				// Don't add short words
			}
			else if(wordMap.containsKey(word)){
				wordMap.replace(word, wordMap.getInteger(word)+1);
			}
			else{
				wordMap.append(word, 1);
			}
		}
		return wordMap;
	}

}

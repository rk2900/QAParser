package entityLinking.parse;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import paser.QuestionFrame;
import baseline.Entity;
import entityLinking.client.entityClient;
import entityLinking.client.entityClientConst;
import entityLinking.db.entityDB;

public class responseParser {
	public static HashSet<String> blackEntityPrefixSet;
	static{
		blackEntityPrefixSet = new HashSet<String>();
		blackEntityPrefixSet.add("List of");
	}
	
	private entityDB db;
	public responseParser(String dbName){
		db = new entityDB(dbName);
	}
	
	public responseParser(){
		
	}
	
	public LinkedList<Entity> setEntityList(QuestionFrame qf,entityClientConst.TOOLKIT toolkit){
		LinkedList<Entity> entities = qf.entityList;
		String content = qf.question;
		String response = entityClient.queryAPI(content, toolkit);
		if(response == null){
			System.err.println(qf.id + ": request failed.");
			return entities;
		}
		switch (toolkit) {
		case MINERDIS:
			JSONObject responseJson = null;
			JSONArray spots = null;
			try {
				responseJson = new JSONObject(response);
				if (!responseJson.has("detectedTopics")) {
					System.err.println(qf.id + ": no detectedTopics");
					return entities;
				}
				spots = responseJson.getJSONArray("detectedTopics");
				System.err.println(response);
				for (int j = 0; j < spots.length(); ++j) {
					JSONObject spot = spots.getJSONObject(j);
					String candTitle = spot.getString("title");
					
					boolean isBlack = false;
					for (String blackLabel : blackEntityPrefixSet) {
						if(candTitle.startsWith(blackLabel)){
							isBlack = true;
							break;
						}
					}
					
					if(isBlack){
						continue;
					}
					
					double weight = spot.getDouble("weight");

					int startIndex=0, endIndex=0;
					JSONArray referArray = spot.getJSONArray("references");
					for (int c = 0; c < referArray.length(); ++c) {
						JSONObject refer = referArray.getJSONObject(c);
						if(c == 0){
							startIndex = refer.getInt("start");
							endIndex = refer.getInt("end");
						}else{
							if(startIndex > refer.getInt("start")){
								startIndex = refer.getInt("start");
							}
							if(endIndex < refer.getInt("end")){
								endIndex = refer.getInt("end");
							}
						}
					}
						
					IndexTransform transform = new IndexTransform(startIndex, endIndex, qf);
					if(transform.isMatched){
						if(Character.isUpperCase(qf.question.charAt(startIndex))){
							entities.add(new Entity(candTitle, transform.start, transform.end));
						}else{
							String entityText = qf.question.substring(startIndex, endIndex);
							String [] tempList = entityText.split(" ");
							if(tempList.length > 1){
								if(Character.isUpperCase(tempList[1].charAt(0))){
									entities.add(new Entity(candTitle, transform.start, transform.end));
									System.err.println(qf.id + ": the + Upper");
								}
							}else{
								System.err.println(qf.id + ": LowerCase");
							}
						}
					}else{
						System.err.println(qf.id + ": Entity not matched.");
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			break;
		default:
			System.err.println("null toolkit");
			break;
		}
		Collections.sort(entities);
		return entities;
	}
	
	public void loadMiner(int id,String response) {
		int questionID = id;
		int startIndex;
		int endIndex;
		int candID;
		String candTitle;
		double weight;

		JSONObject responseJson = null;
		JSONArray spots = null;
		try {
			responseJson = new JSONObject(response);
			if (!responseJson.has("detectedTopics")) {
				System.err.println(id + ": no detectedTopics");
				return;
			}
			spots = responseJson.getJSONArray("detectedTopics");
			for (int j = 0; j < spots.length(); ++j) {
				JSONObject spot = spots.getJSONObject(j);
				candID = spot.getInt("id");
				candTitle = URLEncoder.encode(spot.getString("title"),"UTF-8");
				weight = spot.getDouble("weight");

				JSONArray referArray = spot.getJSONArray("references");
				for (int c = 0; c < referArray.length(); ++c) {
					JSONObject refer = referArray.getJSONObject(c);
					startIndex = refer.getInt("start");
					endIndex = refer.getInt("end");

					db.insertMiner(questionID, startIndex, endIndex,
							candID, candTitle, weight);
//					System.err.println(questionID+" "+startIndex+" "+endIndex+" "+candID+" "+candTitle+" "+weight);
				}
			}
		} catch (JSONException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public void loadSpotlight1(int id, String response) {
		int questionID = id;
		int startIndex;
		int endIndex;
		String candTitle;
		String candUri;
		int support;
		double finalScore;
		double priorScore;
		double contextualScore;
		double percentageOfSecondRank;
		String types;

		JSONObject responseJson = null;
		JSONArray spots = null;
		try {
			responseJson = new JSONObject(response).getJSONObject("annotation");
			if (!responseJson.has("surfaceForm")) {
				System.err.println(id + ": no detectedTopics");
				return;
			}
			JSONObject spotsObject = responseJson.optJSONObject("surfaceForm");
			if(spotsObject != null){
				spots = new JSONArray();
				spots.put(spotsObject);
			}else{
				spots = responseJson.getJSONArray("surfaceForm");
			}
			
			for (int j = 0; j < spots.length(); ++j) {
				JSONObject spot = spots.getJSONObject(j);
				startIndex = Integer.parseInt(spot.getString("@offset"));
				String name = spot.getString("@name");
				endIndex = startIndex + name.length();
				
				if(!spot.has("resource")){
					continue;
				}
				
				JSONArray resources = null;
				JSONObject resourceObject = spot.optJSONObject("resource");
				if(resourceObject != null){
					resources = new JSONArray();
					resources.put(resourceObject);
				}else{
					resources = spot.getJSONArray("resource");
					for (int c = 0; c < resources.length(); ++c) {
						JSONObject resource = resources.getJSONObject(c);
						candTitle = resource.getString("@label");
						candUri = resource.getString("@uri");
						contextualScore = resource.getDouble("@contextualScore");
						support = resource.getInt("@support");
						finalScore = resource.getDouble("@finalScore");
						priorScore = resource.getDouble("@priorScore");
						percentageOfSecondRank = resource.getDouble("@percentageOfSecondRank");
						types = resource.getString("@types");
						db.insertSpotlight1(questionID, startIndex, endIndex, candTitle, candUri, support, finalScore, priorScore, contextualScore, percentageOfSecondRank, types);
					}
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}

	public void loadDexter(int id, String response) {
		int questionID = id;
		int startIndex;
		int endIndex;
		int candID;
		String candTitle;
		String candUri;
		String candDescription;
		double linkProbability;
		double commonness;
		int linkFrequency;
		int documentFrequency;
		int entityFrequency;

		JSONObject responseJson = null;
		JSONObject spots = null;
		try {
			responseJson = new JSONObject(response);
			if (!responseJson.has("entities")) {
				System.err.println(id + ": no detectedTopics");
				return;
			}
			spots = responseJson.getJSONObject("entities");
			Iterator<String> keys = spots.keys(); 
			
			while(keys.hasNext()){
				String key = keys.next();
				candID = Integer.parseInt(key);
				System.err.println(candID);
				String idResponse = entityClient.queryResult(entityClientConst.dexterURI2ID + candID);
				System.err.println(idResponse);
				JSONObject idRes = new JSONObject(idResponse);
				candDescription = URLEncoder.encode(idRes.getString("description"),"UTF-8");
				candUri = URLEncoder.encode(idRes.getString("url"),"UTF-8");
				candTitle = URLEncoder.encode(idRes.getString("title"),"UTF-8");
				
				
				JSONArray spot = spots.getJSONArray(key);
				for(int c=0; c<spot.length(); ++c){
					JSONObject current = spot.getJSONObject(c);
					linkProbability = current.getDouble("linkProbability");
					startIndex = current.getInt("start");
					endIndex = current.getInt("end");
					linkFrequency = current.getInt("linkFrequency");
					documentFrequency = current.getInt("documentFrequency");
					entityFrequency = current.getInt("entityFrequency");
					commonness = current.getDouble("commonness");
					System.err.println(current.toString());
					db.insertDexter(questionID, startIndex, endIndex, linkProbability, linkFrequency, documentFrequency, entityFrequency, commonness, candID, candTitle, candUri, candDescription);
				}
				
			}
		} catch (JSONException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

package entityLinking.parse;

import java.util.ArrayList;
import java.util.LinkedList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import entityLinking.client.entityClient;
import entityLinking.client.entityClientConst;
import entityLinking.db.entityDB;
import basic.FileOps;

public class responseParser {
	
	private entityDB db;
	public responseParser(String dbName){
		db = new entityDB(dbName);
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
				candTitle = spot.getString("title");
				weight = spot.getDouble("weight");

				JSONArray referArray = spot.getJSONArray("references");
				for (int c = 0; c < referArray.length(); ++c) {
					JSONObject refer = referArray.getJSONObject(c);
					startIndex = refer.getInt("start");
					endIndex = refer.getInt("end");

//					db.insertMiner(questionID, startIndex, endIndex,
//							candID, candTitle, weight);
					System.err.println(questionID+" "+startIndex+" "+endIndex+" "+candID+" "+candTitle+" "+weight);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

//	public void loadSpotlight() {
//
//		for (int i = 1; i <= 300; ++i) {
//			System.out.println(i + " start loading...");
//			String filename = path + i + ".txt";
//			String response = FileOps.LoadFile(filename);
//
//			int questionID = i;
//			int startIndex;
//			int endIndex;
//			String candTitle;
//			String candUri;
//			int support;
//			double finalScore;
//			double priorScore;
//			double contextualScore;
//			double percentageOfSecondRank;
//			String types;
//
//			JSONObject responseJson = null;
//			JSONArray spots = null;
//			try {
//				responseJson = new JSONObject(response).getJSONObject("annotation");
//				if (!responseJson.has("surfaceForm")) {
//					continue;
//				}
//				JSONObject spotsObject = responseJson.optJSONObject("surfaceForm");
//				if(spotsObject != null){
//					spots = new JSONArray();
//					spots.put(spotsObject);
//				}else{
//					spots = responseJson.getJSONArray("surfaceForm");
//				}
//				
//				for (int j = 0; j < spots.length(); ++j) {
//					JSONObject spot = spots.getJSONObject(j);
//					startIndex = Integer.parseInt(spot.getString("@offset"));
//					String name = spot.getString("@name");
//					endIndex = startIndex + name.length();
//					
//					if(!spot.has("resource")){
//						continue;
//					}
//					
//					JSONArray resources = null;
//					JSONObject resourceObject = spot.optJSONObject("resource");
//					if(resourceObject != null){
//						resources = new JSONArray();
//						resources.put(resourceObject);
//					}else{
//						resources = spot.getJSONArray("resource");
//						for (int c = 0; c < resources.length(); ++c) {
//							JSONObject resource = resources.getJSONObject(c);
//							candTitle = resource.getString("@label");
//							candUri = resource.getString("@uri");
//							contextualScore = resource.getDouble("@contextualScore");
//							support = resource.getInt("@support");
//							finalScore = resource.getDouble("@finalScore");
//							priorScore = resource.getDouble("@priorScore");
//							percentageOfSecondRank = resource.getDouble("@percentageOfSecondRank");
//							types = resource.getString("@types");
//							db.insertSpotlight(questionID, startIndex, endIndex, candTitle, candUri, support, finalScore, priorScore, contextualScore, percentageOfSecondRank, types);
//						}
//					}
//				}
//			} catch (JSONException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//
//		}
//		System.out.println("finished");
//	}
//
//	/**
//	 * 
//	 */
//	public void loadDexter() {
//		System.out.println("dexter result loading...");
//		String path = "./entity/dexter2/";
//		
//		Annotation annotation = new Annotation();
//
//		for (int i = 1; i <= 300; ++i) {
//			System.out.println(i + " start loading...");
//			String filename = path + i + ".txt";
//			String response = FileOps.LoadFile(filename);
//
//			int questionID = i;
//			int startIndex;
//			int endIndex;
//			int candID;
//			String candTitle;
//			String candUri;
//			String candDescription;
//			double linkProbability;
//			double commonness;
//			int linkFrequency;
//			int documentFrequency;
//			int entityFrequency;
//
//			JSONObject responseJson = null;
//			JSONObject spots = null;
//			try {
//				responseJson = new JSONObject(response);
//				if (!responseJson.has("entities")) {
//					continue;
//				}
//				spots = responseJson.getJSONObject("entities");
//				Iterator<String> keys = spots.keys(); 
//				
//				while(keys.hasNext()){
//					String key = keys.next();
//					candID = Integer.parseInt(key);
//					String idResponse = annotation.queryResult(AnnotationConst.dexterURI2ID + candID);
//					JSONObject idRes = new JSONObject(idResponse);
//					candDescription = idRes.getString("description");
//					candUri = idRes.getString("url").substring(28);
//					candTitle = idRes.getString("title");
//					
//					
//					JSONArray spot = spots.getJSONArray(key);
//					for(int c=0; c<spot.length(); ++c){
//						JSONObject current = spot.getJSONObject(c);
//						linkProbability = current.getDouble("linkProbability");
//						startIndex = current.getInt("start");
//						endIndex = current.getInt("end");
//						linkFrequency = current.getInt("linkFrequency");
//						documentFrequency = current.getInt("documentFrequency");
//						entityFrequency = current.getInt("entityFrequency");
//						commonness = current.getDouble("commonness");
//						
//						db.insertDexter(questionID, startIndex, endIndex, linkProbability, linkFrequency, documentFrequency, entityFrequency, commonness, candID, candTitle, candUri, candDescription);
//					}
//					
//				}
//			} catch (JSONException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//
//		}
//	}

}

package entityLinking.parse;

import java.util.ArrayList;
import java.util.LinkedList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import entityLinking.client.entityClient;
import entityLinking.client.entityClientConst;
import basic.FileOps;

public class responseParser {

	public static ArrayList<entity> spotlight(String response) throws JSONException{
		ArrayList<entity> list = new ArrayList<entity>();
		JSONObject responseJson = null;
		JSONArray spots = null;
		responseJson = new JSONObject(response);
		
		if(!responseJson.has("Resources")){
			return list;
		}
		
		spots = responseJson.getJSONArray("Resources");
		
		for(int i=0; i<spots.length(); ++i){
			JSONObject spot = spots.getJSONObject(i);
			String name = spot.getString("@URI").substring(28);
			int start = Integer.parseInt(spot.getString("@offset"));
			String mention = spot.getString("@surfaceForm");
			int end = start + mention.length();
			list.add(new entity(name, start, end, mention));
//			System.out.println(name+"\t"+start+"\t"+end+"\t"+mention);
		}
		return list;
	}
	
	public static ArrayList<entity> miner(String response) throws JSONException{
		ArrayList<entity> list = new ArrayList<entity>();
		JSONObject responseJson = null;
		JSONArray spots = null;
		responseJson = new JSONObject(response);
		
		if(!responseJson.has("detectedTopics")){
			return list;
		}
		spots = responseJson.getJSONArray("detectedTopics");
		String question = responseJson.getJSONObject("request").getString("source");
		for(int i=0; i<spots.length(); ++i){
			JSONObject spot = spots.getJSONObject(i);
			String name = spot.getString("title");
			int start = spot.getJSONArray("references").getJSONObject(0).getInt("start");
			int end = spot.getJSONArray("references").getJSONObject(0).getInt("end");
			String mention = question.substring(start, end);
			list.add(new entity(name, start, end, mention));
//			System.out.println(name+"\t"+start+"\t"+end+"\t"+mention);
		}
		return list;
	}
	
	public static ArrayList<entity> tagme(String response) throws JSONException{
		ArrayList<entity> list = new ArrayList<entity>();
		JSONObject responseJson = null;
		JSONArray spots = null;
		responseJson = new JSONObject(response);
		
		if(!responseJson.has("annotations")){
			return list;
		}
		spots = responseJson.getJSONArray("annotations");
		for(int i=0; i<spots.length(); ++i){
			JSONObject spot = spots.getJSONObject(i);
			String name = spot.getString("title");
			int start = spot.getInt("start");
			int end = spot.getInt("end");
			String mention = spot.getString("spot");
			list.add(new entity(name, start, end, mention));
//			System.out.println(name+"\t"+start+"\t"+end+"\t"+mention);
		}
		return list;
	}
	
	public static ArrayList<entity> dexter(String response) throws JSONException{
		ArrayList<entity> list = new ArrayList<entity>();
		JSONObject responseJson = null;
		JSONArray spots = null;
		responseJson = new JSONObject(response);
		
		if(!responseJson.has("spots")){
			return list;
		}
		spots = responseJson.getJSONArray("spots");
		entityClient dexAnnotation = new entityClient();
		for(int i=0; i<spots.length(); ++i){
			JSONObject spot = spots.getJSONObject(i);
			int id = spot.getInt("entity");
			int start = spot.getInt("start");
			int end = spot.getInt("end");
			String mention = spot.getString("mention");
			
			String idResponse = dexAnnotation.queryResult(entityClientConst.dexterURI2ID + id);
			JSONObject idJson = new JSONObject(idResponse);
			String name = idJson.getString("title");
			list.add(new entity(name, start, end, mention));
//			System.out.println(name+"\t"+start+"\t"+end+"\t"+mention);
		}
		return list;
	}
	
	public static void main(String[] args) throws JSONException {
		// TODO Auto-generated method stub

		String path = "./entity/";
		String spotlight = path + "spotlight/";
		String miner = path + "minerLinking/";
		String tagme = path + "tagmeLinking/";
		String dexter = path + "dexter2/";
		
		String result = path + "LinkingResult/";
		
		LinkedList<String> texts = FileOps.LoadFilebyLine("./train/questions");
		for(int i=1; i<=300; ++i){
			String filename = spotlight + i +".txt";
			String response = FileOps.LoadFile(filename);
			ArrayList<entity> Dbpedia = spotlight(response);
			
			filename = miner + i +".txt";
			response = FileOps.LoadFile(filename);
			ArrayList<entity> Wikipediaminer = miner(response);
			
			filename = tagme + i +".txt";
			response = FileOps.LoadFile(filename);
			ArrayList<entity> Tagme = tagme(response);
			
			filename = dexter + i +".txt";
			response = FileOps.LoadFile(filename);
			ArrayList<entity> Dexter = dexter(response);
			
			StringBuilder sb = new StringBuilder();
			sb.append(texts.get(i-1));
			sb.append("\n\n");
			
			sb.append("DBpedia Spotlight:\n");
			for (entity e : Dbpedia) {
				sb.append(e.getMention());
				sb.append("\t");
				sb.append(e.getStart());
				sb.append("\t");
				sb.append(e.getEnd());
				sb.append("\t");
				sb.append(e.getName());
				sb.append("\n");
			}	
			sb.append("\n");
			sb.append("Wikipedia Miner:\n");
			for (entity e : Wikipediaminer) {
				sb.append(e.getMention());
				sb.append("\t");
				sb.append(e.getStart());
				sb.append("\t");
				sb.append(e.getEnd());
				sb.append("\t");
				sb.append(e.getName());
				sb.append("\n");
			}
			sb.append("\n");
			sb.append("Dexter2:\n");
			for (entity e : Dexter) {
				sb.append(e.getMention());
				sb.append("\t");
				sb.append(e.getStart());
				sb.append("\t");
				sb.append(e.getEnd());
				sb.append("\t");
				sb.append(e.getName());
				sb.append("\n");
			}
			sb.append("\n");
			sb.append("TagMe:\n");
			for (entity e : Tagme) {
				sb.append(e.getMention());
				sb.append("\t");
				sb.append(e.getStart());
				sb.append("\t");
				sb.append(e.getEnd());
				sb.append("\t");
				sb.append(e.getName());
				sb.append("\n");
			}
			sb.append("\n");
			
			FileOps.SaveFile(result+i+".txt", sb.toString());
//			if(i==2){
//				break;
//			}
			
			
		}
	}

}

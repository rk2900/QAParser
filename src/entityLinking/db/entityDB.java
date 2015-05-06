package entityLinking.db;

import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class entityDB {

	Connection conn;
	
	public entityDB(String dbName){
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(
							"jdbc:mysql://172.16.7.87:3306/"+dbName+"?characterEncoding=utf8",
							"qald", "qald");
		} catch (Exception ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
	}
	
	public void insertMiner(
			int questionID, int startIndex, int endIndex, 
			int candID, String candTitle, double weight){
		try {
			PreparedStatement stat = conn.prepareStatement(
					"INSERT INTO `miner`(`questionID`, `startIndex`,`endIndex`,`candID`,`candTitle`,`weight`) VALUES (?,?,?,?,?,?)");
			stat.setInt(1, questionID);
			stat.setInt(2, startIndex);
			stat.setInt(3, endIndex);
			stat.setInt(4, candID);
			stat.setString(5, candTitle);
			stat.setDouble(6, weight);
			stat.executeUpdate();
		} catch (Exception ex) {
		}
	}
	
	public void insertSpotlight(
			int questionID, int startIndex, int endIndex, 
			String candTitle, String candUri, int support,
			double finalScore, double priorScore, double contextualScore,
			double percentageOfSecondRank, String types){
		try {
			PreparedStatement stat = conn.prepareStatement(
					"INSERT INTO `spotlight2`(`questionID`, `startIndex`,`endIndex`,`candTitle`,`candUri`,`support`,`finalScore`,`priorScore`,`contextualScore`,`percentageOfSecondRank`,`types`) VALUES (?,?,?,?,?,?,?,?,?,?,?)");
			stat.setInt(1, questionID);
			stat.setInt(2, startIndex);
			stat.setInt(3, endIndex);
			stat.setString(4, candTitle);
			stat.setString(5, candUri);
			stat.setInt(6, support);
			stat.setDouble(7, finalScore);
			stat.setDouble(8, priorScore);
			stat.setDouble(9, contextualScore);
			stat.setDouble(10, percentageOfSecondRank);
			stat.setString(11, types);
			stat.executeUpdate();
		} catch (Exception ex) {
		}
	}
	
	public void insertDexter(
			int questionID, int startIndex, int endIndex,
			double linkProbability, int linkFrequency,
			int documentFrequency, int entityFrequency,
			double commonness, int candID, String candTitle,
			String candUri, String candDescription ){
		try {
			PreparedStatement stat = conn.prepareStatement(
					"INSERT INTO `dexter`(`questionID`, `startIndex`,`endIndex`,`linkProbability`, `linkFrequency`, `documentFrequency`,`entityFrequency`,`commonness`,`candID`,`candTitle`,`candUri`,`candDescription`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)");
			stat.setInt(1, questionID);
			stat.setInt(2, startIndex);
			stat.setInt(3, endIndex);
			stat.setDouble(4, linkProbability);
			stat.setInt(5, linkFrequency);
			stat.setInt(6, documentFrequency);
			stat.setInt(7, entityFrequency);
			stat.setDouble(8, commonness);
			stat.setInt(9, candID);
			stat.setString(10, candTitle);
			stat.setString(11, candUri);
			stat.setString(12, candDescription);
			stat.executeUpdate();
		} catch (Exception ex) {
		}
	}

	public void insertTrain(
			int question,int start,int end, String title,
			double weight, double linkprobability, double commonness,
			int linkfrequency, int documentfrequency, int entityfrequency,
			double finalscore, int support, double priorscore, double contextscore,
			double percentage, double finalscore2, int support2, double priorscore2,
			double contextscore2, double percentage2,int label
			){
		try {
			PreparedStatement stat = conn.prepareStatement(
					"INSERT INTO `train`(`question`, `start`,`end`,`title`,`weight`,`linkprobability`, `commonness`,`linkfrequency`, `documentfrequency`,`entityfrequency`,`finalscore`,`support`,`priorscore`,`contextscore`,`percentage`,`finalscore2`,`support2`,`priorscore2`,`contextscore2`,`percentage2`,`label`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
			stat.setInt(1, question);
			stat.setInt(2, start);
			stat.setInt(3, end);
			stat.setString(4, title);
			stat.setDouble(5, weight);
			stat.setDouble(6, linkprobability);
			stat.setDouble(7, commonness);
			stat.setInt(8, linkfrequency);
			stat.setInt(9, documentfrequency);
			stat.setInt(10, entityfrequency);
			stat.setDouble(11, finalscore);
			stat.setInt(12, support);
			stat.setDouble(13, priorscore);
			stat.setDouble(14, contextscore);
			stat.setDouble(15, percentage);
			stat.setDouble(16, finalscore2);
			stat.setInt(17, support2);
			stat.setDouble(18, priorscore2);
			stat.setDouble(19, contextscore2);
			stat.setDouble(20, percentage2);
			stat.setInt(21, label);
			
			stat.executeUpdate();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public ResultSet select(String query){
		Statement stat;
		try {
			stat = conn.createStatement();
			ResultSet r=stat.executeQuery(query);
			return r;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public ArrayList<String[]> selectMark(){
		ArrayList<String[]> res=new ArrayList<String[]>();
		try {
			String cmd="SELECT * FROM mark WHERE pattern = 1";
			Statement stat=conn.createStatement();
			ResultSet r=stat.executeQuery(cmd);
			
			for (;r.next();){
				String entity = r.getString(5);
				if(entity.length() != 0){
					entity = entity.replace(" ", "_");
					String wiki = "http://en.wikipedia.org/wiki/"+entity;
					res.add(new String[]{r.getString(2),r.getString(3),r.getString(4),wiki});
				}
			}
		} catch (Exception e) {}
		return res;
	}
	
	
	public static void main(String[] args) {

	}

}

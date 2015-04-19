package umbc;

import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;

import basic.FileOps;

public class umbcDB {

	Connection conn;
	
	public umbcDB(){
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(
							"jdbc:mysql://localhost:3306/similarity?characterEncoding=utf8",
							"root", "");
		} catch (Exception ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
	}
	
	private void insert(String word1, String word2, double umbc){
		word1 = URLEncoder.encode(word1);
		word2 = URLEncoder.encode(word2);
		PreparedStatement stat;
		try {
			stat = conn.prepareStatement(
					"INSERT INTO `umbc`(`word1`,`word2`,`umbc`) VALUES (?,?,?)");
			stat.setString(1, word1);
			stat.setString(2, word2);
			stat.setDouble(3, umbc);
			stat.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void update(String word1, String word2, double umbc){
		word1 = URLEncoder.encode(word1);
		word2 = URLEncoder.encode(word2);
		
		Statement stat;
		StringBuilder query = new StringBuilder();
		query.append("update umbc set umbc = ");
		query.append(umbc);
		query.append(" where word1 = \"");
		query.append(word1);
		query.append("\" and word2 = \"");
		query.append(word2);
		query.append("\"");
//		System.out.println(query.toString());
		try {
			stat = conn.createStatement();
			int r=stat.executeUpdate(query.toString());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private boolean isContain(String word1, String word2){
		word1 = URLEncoder.encode(word1);
		word2 = URLEncoder.encode(word2);
		
		boolean mark = false;
		Statement stat;
		StringBuilder query = new StringBuilder();
		query.append("select * from umbc where word1 = \"");
		query.append(word1);
		query.append("\" and word2 = \"");
		query.append(word2);
		query.append("\"");
//		System.out.println(query.toString());
		try {
			stat = conn.createStatement();
			ResultSet r=stat.executeQuery(query.toString());
			if(r.next()){
				mark = true;
			}else{
				mark = false;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return mark;
	}
	
	public void insertWords(String word1, String word2){
		if(!isContain(word1, word2) && !isContain(word2, word1)){
			insert(word1, word2, -1);
		}
	}
	
	public void updateValue(String word1, String word2, double value){
		if(isContain(word1, word2)){
			update(word1, word2, value);
		}else{
			update(word2, word1, value);
		}
	}
	
	public ResultSet getAllPairs(){
		Statement stat;
		ResultSet r = null;
		StringBuilder query = new StringBuilder();
		query.append("select * from umbc");
		try {
			stat = conn.createStatement();
			r=stat.executeQuery(query.toString());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return r;
	}
	
	private double getOrderedScore(String word1, String word2){
		double score = -1;
		word1 = URLEncoder.encode(word1);
		word2 = URLEncoder.encode(word2);

		Statement stat;
		StringBuilder query = new StringBuilder();
		query.append("select umbc from umbc where word1 = \"");
		query.append(word1);
		query.append("\" and word2 = \"");
		query.append(word2);
		query.append("\"");
//		System.out.println(query.toString());
		try {
			stat = conn.createStatement();
			ResultSet r=stat.executeQuery(query.toString());
			if(r.next()){
				score = r.getDouble(1);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return score;
	}
	
	public double getScore(String word1, String word2){
		if(isContain(word1, word2)){
			return getOrderedScore(word1, word2);
		}else{
			return getOrderedScore(word2, word1);
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
	}

}

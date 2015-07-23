package umbc;

import similarity.UMBC;

public class umbcTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		umbcDB db = new umbcDB();
//		db.updateValue("In", "Link", -1);
//		System.out.println(UMBC.getSimilarity("Link", "In"));
		System.out.println(db.getScore("people", "country"));
	}

}

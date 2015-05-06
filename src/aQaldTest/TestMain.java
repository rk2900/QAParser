package aQaldTest;

import knowledgebase.ClientManagement;
import finder.Pipeline;
import finder.Pipeline.DataSource;

public class TestMain {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Pipeline pipeline = new Pipeline(DataSource.TEST);
		baseline.Main.setEntity(pipeline);
		try {
			ClientManagement.getAgModel();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		
	}
}

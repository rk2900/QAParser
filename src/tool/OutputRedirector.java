package tool;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class OutputRedirector {
	
	private static PrintStream console;
	private static PrintStream out;
	
	public static void openFileOutput(String filePath) {
		BufferedOutputStream bw = null;
		try {
			bw = new BufferedOutputStream(new FileOutputStream(filePath, false));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		console = System.out;
		out = new PrintStream(bw);
		System.setOut(out);
	}
	
	public static void closeFileOutput() {
		out.close();
		System.setOut(console);
	}
}

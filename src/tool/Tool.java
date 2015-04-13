package tool;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

public class Tool {
	public static void printList(LinkedList<String> list) {
		for (String string : list) {
			System.out.print(string);
			System.out.print("\t");
		}
		System.out.println();
	}
	
	public static void printList(ArrayList<String> list) {
		for (String string : list) {
			System.out.print(string);
			System.out.print("\t");
		}
		System.out.println();
	}
	
	public static void printList(HashSet<String> set) {
		for (String string : set) {
			System.out.print(string);
			System.out.print("\t");
		}
		System.out.println();
	}
}

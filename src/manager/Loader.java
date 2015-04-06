package manager;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import basic.FileOps;

public class Loader {
	String filePath = new String("./data/qald-5_train.xml");
	Document doc = null;
	
	private void load() {
		if(FileOps.exist(filePath)) {
			File xmlFile = new File(filePath);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			try {
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				doc = dBuilder.parse(xmlFile);
			} catch (SAXException | IOException e) {
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			}
			System.out.println("File '"+filePath+"' loaded.");
		} else {
			System.out.println("File not exist.");
		}
	}
	
	public Loader() {
		load();
	}
	
	public Loader(String fileString) {
		if(fileString.length() > 0) {
			filePath = fileString;
		}
		load();
	}
	
	public Document getDocument() {
		return doc;
	}
}

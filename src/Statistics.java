import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Statistics {
	private int tableNumber;
	private Map<Integer, Integer> row2Count;
	private Map<Integer, Integer> column2Count;
	private int nullNumber;
	private String docsPath;
	private Map<Integer, Integer> distinctElementForTable;
	
	public Statistics() {
		this.tableNumber = 0;
		this.nullNumber = 0;
		this.row2Count = new HashMap<Integer, Integer>();
		this.column2Count = new HashMap<Integer, Integer>();
		this.distinctElementForTable = new HashMap<Integer, Integer>();
		this.docsPath = "C:\\Users\\antod\\OneDrive\\Desktop\\statisticheDataset";
	}
	
	public void generateStatistics() throws IOException {
		System.out.println("Inizio esecuzione");
		Path dir = Paths.get(this.docsPath);
		File jsonFile = null;
		if (Files.isDirectory(dir)) {
            File[] files = dir.toFile().listFiles((pathname) -> pathname.getName().endsWith(".json"));
            if (files != null) {
                jsonFile = files[0];
            }
        }
		FileReader fileReader = new FileReader(jsonFile.toPath().toString());
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line = null;
        String currentId = null;
        while(true) {
        	line = bufferedReader.readLine();
            if (line == null) {
                break;
            }
            JsonParser parser = new JsonParser();
            JsonObject jsonObject = parser.parse(line).getAsJsonObject();

            // Estrai l'ID dall'oggetto JSON
//            String id = jsonObject.get("id").getAsString();
            Set<String> distinctElem = new HashSet<String>();
            Integer countDistincElem = 0;
            
            this.tableNumber++;
            
//            if (!id.equals(currentId) && currentId != null) {
//            	this.tableNumber++; 	
//            }
            
            JsonObject maxDimensions = jsonObject.getAsJsonObject("maxDimensions");
            Integer row = Integer.parseInt(maxDimensions.get("row").toString());
            Integer column = Integer.parseInt(maxDimensions.get("column").toString());
            if(this.column2Count.containsKey(column)) {
            	Integer value = this.column2Count.get(column);
            	value += 1;
            	this.column2Count.put(column, value++);
            }
            else {
            	this.column2Count.put(column, 1);
            }
            
            if(this.row2Count.containsKey(row)) {
            	Integer value = this.row2Count.get(row);
            	value += 1;
            	this.row2Count.put(row, value);
            }
            else {
            	this.row2Count.put(row, 1);
            }
            
            JsonArray cellsArray = jsonObject.getAsJsonArray("cells");
            for (JsonElement cellElement : cellsArray) {
                JsonObject cellObject = cellElement.getAsJsonObject();
                if(cellObject.get("type").getAsString().equals("EMPTY")) {
                	this.nullNumber++;
            	}
                if(!cellObject.get("type").getAsString().equals("EMPTY")
                		&& !cellObject.get("type").getAsString().equals("HEADER")) {
                	String entry = cellObject.get("cleanedText").getAsString();
                	if(!distinctElem.contains(entry)) {
                		countDistincElem += 1;
                		distinctElem.add(entry);
                	}
                }
            }
            
            if(this.distinctElementForTable.containsKey(countDistincElem)) {
            	Integer value = this.distinctElementForTable.get(countDistincElem);
            	value += 1;
            	this.distinctElementForTable.put(countDistincElem, value);
            }
            else {
            	this.distinctElementForTable.put(countDistincElem, 1);
            }
//            currentId = id;
        }
        
//        tableNumber++;
        this.saveData(column2Count, row2Count, tableNumber, nullNumber, distinctElementForTable);
        bufferedReader.close();
        fileReader.close();
	}
	
	public void saveData(Map<Integer, Integer> column2Count, Map<Integer, Integer> row2Count, 
			int tableNumber, int nullNumber, Map<Integer, Integer> distinctElementForTable) {
		String filePath = "C:\\Users\\antod\\OneDrive\\Desktop\\statisticheDataset\\valori.txt";

        try (FileWriter fileWriter = new FileWriter(filePath)) {
            String line = "Distribuzione colonne: " + System.lineSeparator();
            fileWriter.write(line);
            
            int sumColumn = 0;
            int sumRow = 0;
            int sumDistinctElem = 0;
            
        	for (Map.Entry<Integer, Integer> entry : column2Count.entrySet()) {
        		sumColumn += entry.getKey() * entry.getValue();
                line = entry.getKey() + ": " + entry.getValue() + System.lineSeparator();
                fileWriter.write(line);
            }
        	line = "Distribuzione righe: " + System.lineSeparator();
            fileWriter.write(line);
            for (Map.Entry<Integer, Integer> entry : row2Count.entrySet()) {
            	sumRow += entry.getKey() * entry.getValue();
                line = entry.getKey() + ": " + entry.getValue() + System.lineSeparator();
                fileWriter.write(line);
            }
            
            line = "Distribuzione elementi distinti: " + System.lineSeparator();
            fileWriter.write(line);
            for (Map.Entry<Integer, Integer> entry : distinctElementForTable.entrySet()) {
            	sumDistinctElem += entry.getKey() * entry.getValue();
                line = entry.getKey() + ": " + entry.getValue() + System.lineSeparator();
                fileWriter.write(line);
            }
            
            line = "Numero di tabelle: " + tableNumber + System.lineSeparator();
            fileWriter.write(line);
            line = "Numero medio valori NULL per tabella: " + (float) nullNumber/tableNumber + System.lineSeparator();
            fileWriter.write(line);
            // Ottieni l'insieme dei valori dalla mappa
            line = "Numero medio di colonne: " + (float) sumColumn/tableNumber + System.lineSeparator();
            fileWriter.write(line);
            
            line = "Numero medio di righe: " + (float) sumRow/tableNumber + System.lineSeparator();
            fileWriter.write(line);
            
            line = "Numero medio di elementi distinti per tabella: " + (float) sumDistinctElem/tableNumber + System.lineSeparator();
            fileWriter.write(line);
            System.out.println("Dati scritti nel file di testo con successo.");
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Fine esecuzione");
	}
	
	public static void searchStatistics(Map<String, Integer> document2Score, List<String> result, 
			String terms, int k) {
		String filePath = "target/test.txt";

        try (FileWriter fileWriter = new FileWriter(filePath)) {
            String line = "Tutti i documenti ritornati dati i seguenti termini: " + terms + "." + System.lineSeparator();
            fileWriter.write(line);
            
        	for (Map.Entry<String, Integer> entry : document2Score.entrySet()) {
                line = entry.getKey()+ ": " + entry.getValue() + System.lineSeparator();
                fileWriter.write(line);
            }
        	line = "Le migliori k colonne ritornate sono invece le seguenti: " + System.lineSeparator();
            fileWriter.write(line);
            for(String doc: result) {
            	line = doc+ ": " + document2Score.get(doc) + System.lineSeparator();
                fileWriter.write(line);
            }
            
            System.out.println("Dati scritti nel file di testo con successo.");
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Statistiche generate");
	}
}

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class JSONDataProcessor implements DataProcessor {
	
	private FileReader fileReader;
	private BufferedReader bufferedReader;
	
	@Override
	public Document processHTMLTable(String htmlTable) {
		// Implementa la logica per l'elaborazione di tabelle HTML se necessario.
		return null;
	}

	@Override
	public void processJSONData(Path jsonFilePath, IndexWriter writer) throws IOException {
        this.fileReader = new FileReader(jsonFilePath.toString());
        this.bufferedReader = new BufferedReader(fileReader);
		String table = null;
		int table_number = 0;
		while(true) {
			// in line ho la tabella
			table = bufferedReader.readLine();
			//se la line è vuota la tabella è finita e bisogna passare alla successiva
			if (table == null) break;

			JsonElement jsonTree = JsonParser.parseString(table);
			JsonObject jsonTable = jsonTree.getAsJsonObject();

			//mappa dove memorizzo le celle per colonna
			Map<Integer, String> column2cell = new HashMap<>();

			JsonArray cells = jsonTable.getAsJsonArray("cells");
			for (JsonElement cell : cells) {

				JsonObject jsonobject = cell.getAsJsonObject();
				//se non è un header è contenuto di interesse
				if (!jsonobject.get("isHeader").getAsBoolean()){
					JsonObject coordinates = jsonobject.get("Coordinates").getAsJsonObject();
					Integer column = coordinates.get("column").getAsInt();
					String content = jsonobject.get("cleanedText").getAsString();
					if (!content.equals("")) {
						if(column2cell.containsKey(column)) {
							//lo spazio separa le varie celle nel documento
							column2cell.put(column, column2cell.get(column) + " " + content);
						}
						else {
							column2cell.put(column, content);
						}
					}
				}
			}

			if(!column2cell.isEmpty()) {
	            JsonParser parser = new JsonParser();
	            JsonObject jsonObject = parser.parse(table).getAsJsonObject();
				String table_id = jsonObject.get("id").getAsString();
				// questo ciclo viene eseguito per ogni tabella
				for(Integer col : column2cell.keySet()) {
					// creo un documento per ogni colonna
					Document doc = new Document();
					doc.add(new TextField("table_id", table_id, Field.Store.YES));
					doc.add(new TextField("column_table", "doc_"+table_number+"_column"+col.toString(), Field.Store.YES));
					doc.add(new TextField("column_content",column2cell.get(col), Field.Store.YES));
					writer.addDocument(doc);
                    table_number += 1;
				}
			}

		}
		writer.commit();

		this.bufferedReader.close();
		this.fileReader.close();
	}

}

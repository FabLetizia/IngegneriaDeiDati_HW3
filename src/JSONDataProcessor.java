import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JSONDataProcessor implements DataProcessor {
    @Override
    public Document processHTMLTable(String htmlTable) {
        // Implementa la logica per l'elaborazione di tabelle HTML se necessario.
        return null;
    }

    @Override
    public void processJSONData(Path jsonFilePath, IndexWriter writer) throws IOException {
        FileReader fileReader = new FileReader(jsonFilePath.toString());
        BufferedReader bufferedReader = new BufferedReader(fileReader);

        List<Document> documents = new ArrayList<>();
        String line = null;
        String currentId = null;
        Map<String, Map<Integer, List<String>>> idToColumnData = new HashMap<>();

        while (true) {
            line = bufferedReader.readLine();
            if (line == null) {
                break;
            }

            // Analizza il contenuto JSON in un oggetto JSON
            JsonParser parser = new JsonParser();
            JsonObject jsonObject = parser.parse(line).getAsJsonObject();

            // Estrai l'ID dall'oggetto JSON
            String id = jsonObject.get("id").getAsString();

            if (!id.equals(currentId) && currentId != null) {
                Document luceneDoc = new Document();
                Map<Integer, List<String>> columnData = idToColumnData.get(currentId);
                for (Map.Entry<Integer, List<String>> entry : columnData.entrySet()) {
                    int column = entry.getKey();
                    List<String> columnValues = entry.getValue();
                    String fieldName = "column_" + column;
                    String fieldValue = String.join(" ", columnValues);
                    luceneDoc.add(new TextField(fieldName, fieldValue, Field.Store.YES));
                }
                idToColumnData.remove(currentId);
                writer.addDocument(luceneDoc);
                //documents.add(luceneDoc);
            }

            // Crea una mappa per le colonne se è un nuovo ID
            if (!idToColumnData.containsKey(id)) {
                idToColumnData.put(id, new HashMap<>());

            }

            // Estrai i dati che desideri indicizzare (es. cleanedText) e aggrega per colonna
            JsonArray cellsArray = jsonObject.getAsJsonArray("cells");
            for (JsonElement cellElement : cellsArray) {
                JsonObject cellObject = cellElement.getAsJsonObject();
                if (cellObject.has("cleanedText") && !cellObject.get("type").getAsString().equals("EMPTY")) {
                    String cleanedText = cellObject.get("cleanedText").getAsString();
                    int column = cellObject.getAsJsonObject("Coordinates").get("column").getAsInt();

                    Map<Integer, List<String>> columnData = idToColumnData.get(id);
                    columnData.computeIfAbsent(column, k -> new ArrayList<>()).add(cleanedText);
                }
            }
            currentId = id;
        }
        Document luceneDoc = new Document();
        Map<Integer, List<String>> columnData = idToColumnData.get(currentId);
        for (Map.Entry<Integer, List<String>> entry : columnData.entrySet()) {
            int column = entry.getKey();
            List<String> columnValues = entry.getValue();
            String fieldName = "column_" + column;
            String fieldValue = String.join(" ", columnValues);
            luceneDoc.add(new TextField(fieldName, fieldValue, Field.Store.YES));
        }
        idToColumnData.remove(currentId);
        writer.addDocument(luceneDoc);
        writer.commit();
        bufferedReader.close();
        fileReader.close();
    }
}
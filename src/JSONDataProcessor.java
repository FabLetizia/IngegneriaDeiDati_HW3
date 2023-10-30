import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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
    public Document processJSONData(File jsonFile) throws IOException {
        Gson gson = new Gson();
        Document luceneDoc = new Document();

        // Leggi il contenuto del file JSON
        String jsonData = Files.readString(jsonFile.toPath()); // Utilizza la classe Files per semplificare la lettura del file

        // Analizza il contenuto JSON in un oggetto JSON
        JsonObject jsonObject = gson.fromJson(jsonData, JsonObject.class);

        // Estrai l'array "cells" dall'oggetto JSON
        JsonArray cellsArray = jsonObject.getAsJsonArray("cells");

        Map<Integer, List<String>> columnData = new HashMap<>();

        // Itera sugli oggetti JSON nell'array "cells" e cerca i campi "cleanedText","Coordinates" e "isHeader"
        for (JsonElement cellElement : cellsArray) {
            JsonObject cellObject = cellElement.getAsJsonObject();
            if (cellObject.has("cleanedText") && !cellObject.get("type").getAsString().equals("EMPTY")) {
                String cleanedText = cellObject.get("cleanedText").getAsString();
                int column = cellObject.getAsJsonObject("Coordinates").get("column").getAsInt();
                boolean isHeader = cellObject.get("isHeader").getAsBoolean();

                if (!isHeader) {
                    columnData.computeIfAbsent(column, k -> new ArrayList<>()).add(cleanedText);
                }
            }
        }

        // Creazione dei campi nel documento Lucene
        for (Map.Entry<Integer, List<String>> entry : columnData.entrySet()) {
            List<String> columnValues = entry.getValue();
            String fieldName = "column_" + entry.getKey();
            //combina i valori in una singola stringa
            String fieldValue = String.join(" ", columnValues);
            luceneDoc.add(new TextField(fieldName, fieldValue, Field.Store.YES));
        }

        return luceneDoc;
    }
}

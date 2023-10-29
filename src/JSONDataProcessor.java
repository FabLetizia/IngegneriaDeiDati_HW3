import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class JSONDataProcessor implements DataProcessor{
    @Override
    public Document processHTMLTable(String htmlTable) {
        return null;
    }

    @Override
    public Document processJSONData(File jsonFile) throws IOException {
        Gson gson = new Gson();
        Document luceneDoc = new Document();

        // Leggi il contenuto del file JSON
        try (BufferedReader reader = new BufferedReader(new FileReader(jsonFile))) {
            StringBuilder jsonData = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonData.append(line);
            }

            // Analizza il contenuto JSON in un oggetto JSON
            JsonObject jsonObject = gson.fromJson(jsonData.toString(), JsonObject.class);

            // Estrai l'array "cells" dall'oggetto JSON
            JsonArray cellsArray = jsonObject.getAsJsonArray("cells");

            // Itera sugli oggetti JSON in "cells" e cerca il campo "cleanedText"
            for (JsonElement cellElement : cellsArray) {
                JsonObject cellObject = cellElement.getAsJsonObject();
                if (cellObject.has("cleanedText")) {
                    String cleanedText = cellObject.get("cleanedText").getAsString();
                    luceneDoc.add(new TextField("cleanedText", cleanedText, Field.Store.YES));
                }
            }
        }

        return luceneDoc;
    }



}

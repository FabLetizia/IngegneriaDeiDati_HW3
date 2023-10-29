import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.lucene.document.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class JSONDataProcessor implements DataProcessor {
    @Override
    public Document processHTMLTable(String htmlTable) {
        return null;
    }

    @Override
    public Document processJSONData(File jsonFile) throws IOException {
        Gson gson = new Gson();
        Document luceneDoc = new Document();

        // Estrai il nome del file (preso come ID del documento)
        String documentID = jsonFile.getName();
        luceneDoc.add(new StringField("documentID", documentID, Field.Store.YES));

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
            StringBuilder extractedText = new StringBuilder();
            for (JsonElement cellElement : cellsArray) {
                JsonObject cellObject = cellElement.getAsJsonObject();
                if (cellObject.has("cleanedText")) {

                    String cleanedText = cellObject.get("cleanedText").getAsString();
                    int columnID = cleanedText.hashCode();
                    extractedText.append(cleanedText).append(" ");
                    luceneDoc.add(new IntField("columnID", columnID, Field.Store.YES));
                }
            }
            luceneDoc.add(new TextField("extractedText", extractedText.toString(), Field.Store.YES));
        }

        return luceneDoc;
    }
}

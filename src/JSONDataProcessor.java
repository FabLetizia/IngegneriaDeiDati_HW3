import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;

public class JSONDataProcessor implements DataProcessor{
    @Override
    public Document processHTMLTable(String htmlTable) {
        return null;
    }

    @Override
    public Document processJSONData(String jsonData) {
        Gson gson = new Gson();
        Document luceneDoc = new Document();
        JsonArray jsonArray = gson.fromJson(jsonData, JsonArray.class);
        for (JsonElement element : jsonArray) {
            JsonObject jsonObject = element.getAsJsonObject();
            String id = jsonObject.get("_id").getAsString();
            String cleanedText = jsonObject.get("cleanedText").getAsString();
            luceneDoc.add(new TextField("cleanedText", cleanedText, Field.Store.YES));
            luceneDoc.add(new StringField("jsonDocumentID", id, Field.Store.YES));
        }
        return luceneDoc;
    }

}

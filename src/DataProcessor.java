import org.apache.lucene.document.Document;

public interface DataProcessor {
    Document processHTMLTable(String htmlTable);
    Document processJSONData(String jsonData);
}

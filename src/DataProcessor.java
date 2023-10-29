import org.apache.lucene.document.Document;

import java.io.IOException;

public interface DataProcessor {
    Document processHTMLTable(String htmlTable);
    Document processJSONData(String jsonData) throws IOException;
}

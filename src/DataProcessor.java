import org.apache.lucene.document.Document;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface DataProcessor {
    Document processHTMLTable(String htmlTable);
    List<Document> processJSONData(File jsonData) throws IOException;
}

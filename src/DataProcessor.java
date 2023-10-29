import org.apache.lucene.document.Document;

import java.io.File;
import java.io.IOException;

public interface DataProcessor {
    Document processHTMLTable(String htmlTable);
    Document processJSONData(File jsonData) throws IOException;
}

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;

import java.io.IOException;
import java.nio.file.Path;

public interface DataProcessor {
    Document processHTMLTable(String htmlTable);
    void processJSONData(Path jsonFile, IndexWriter writer) throws IOException;
}

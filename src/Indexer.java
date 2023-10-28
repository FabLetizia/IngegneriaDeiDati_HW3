import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.it.ItalianAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Indexer {
    private String indexPath = "/Users/alessandropesare/software/GitHub/IngegneriaDeiDati_HW3/target";
    private String docsPath = "/Users/alessandropesare/software/GitHub/IngegneriaDeiDati_HW3/src/resources";
    private DataProcessor dataProcessor;

    public Indexer(DataProcessor dataProcessor) {
        this.dataProcessor = dataProcessor;
    }

    public void indexDocuments(IndexWriter writer, Path dir) throws IOException {
        if (Files.isDirectory(dir)) {
            File[] files = dir.toFile().listFiles((pathname) -> pathname.getName().endsWith(".html"));
            if (files != null) {
                for (File file : files) {
                    System.out.println(file.getName());
                    Document luceneDoc = dataProcessor.processHTMLTable(readTextFile(file));
                    writer.addDocument(luceneDoc);
                }
                writer.commit();
            }
        }
    }

    private Analyzer createAnalyzer() {
        CharArraySet contenutoStopWords = new CharArraySet(Arrays.asList("in","dei","di","il","la","lo","gli","dell'"), true);
        Analyzer titoloAnalyzer = new WhitespaceAnalyzer();
        Analyzer contenutoAnalyzer = new StandardAnalyzer(contenutoStopWords);
        Analyzer defaultAnalyzer = new ItalianAnalyzer();

        Map<String, Analyzer> perFieldAnalyzers = new HashMap<>();
        perFieldAnalyzers.put("titolo", titoloAnalyzer);
        perFieldAnalyzers.put("contenuto", contenutoAnalyzer);

        return new PerFieldAnalyzerWrapper(defaultAnalyzer, perFieldAnalyzers);
    }

    private String readTextFile(File file) throws IOException {
        /* Leggiamo il contenuto del file .txt e lo restituiamo come una stringa, utilizzo File Reader
         ottimizziamo con un bufferReader la lettura del file minimizzando le operazioni di IO con una
         lettura bufferizzata*/

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
            StringBuilder text = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                text.append(line).append("\n");
            }
            System.out.println(text.toString());
            return text.toString();
        }
    }

    public static void main(String[] args) throws IOException {
        DataProcessor dataProcessor = new HTMLDataProcessor();
        Indexer indexer = new Indexer(dataProcessor);

        Directory directory = FSDirectory.open(Paths.get(indexer.indexPath));
        Analyzer analyzer = indexer.createAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter writer = new IndexWriter(directory, config);

        System.out.println("Inizio lettura dati");
        long startTime = System.currentTimeMillis();
        indexer.indexDocuments(writer, Paths.get(indexer.docsPath));
        writer.close();
        long endTime = System.currentTimeMillis();
        long time = endTime - startTime;

        System.out.println("Indicizzazione completata con successo. L'indice è stato salvato in " + indexer.indexPath);
        System.out.println("Il tempo trascorso per l'indicizzazione dei file è: " + time + " millisecondi");
    }
}

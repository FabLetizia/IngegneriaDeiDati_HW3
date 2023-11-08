import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.codecs.simpletext.SimpleTextCodec;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;


public class Indexer {
    private String indexPath;
    private String docsPath;

    private Directory directory;
    private DataProcessor dataProcessor;

    public Indexer(DataProcessor dataProcessor) {
        this.dataProcessor = dataProcessor;
        this.indexPath = "target/index";
        this.docsPath = "/Users/alessandropesare/software/GitHub/IngegneriaDeiDati_HW3/src/tabellejson";
        initialize();
    }

    public Indexer(DataProcessor dataProcessor, String indexPath, String docsPath) {
        this.dataProcessor = dataProcessor;
        this.indexPath = indexPath;
        this.docsPath = docsPath;
        initialize();
    }

    private void initialize() {
        try {
            this.directory = FSDirectory.open(Paths.get(this.getIndexPath()));
            System.out.println(this.directory);
            Map<String,Analyzer> perFieldAnalyzers = new HashMap<>();
            perFieldAnalyzers.put("table_id", new StandardAnalyzer());
            perFieldAnalyzers.put("column_table", new StandardAnalyzer());
            perFieldAnalyzers.put("column_content", new EnglishAnalyzer());
    		Analyzer analyzer = new PerFieldAnalyzerWrapper(new StandardAnalyzer(),perFieldAnalyzers);
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
    		config.setCodec(new SimpleTextCodec());

            IndexWriter writer = new IndexWriter(directory, config);
            System.out.println("Inizio lettura dati e costruzione indice");
            long startTime = System.currentTimeMillis();
            System.out.println(this.docsPath);
            indexDocuments(writer, Paths.get(this.getDocsPath()));
            long endTime = System.currentTimeMillis();
            long time = endTime - startTime;
            System.out.println("Indicizzazione completata con successo. L'indice è stato salvato in " + this.getIndexPath());
            System.out.println("Il tempo trascorso per l'indicizzazione dei file è: " + time + " millisecondi");
        } catch (IOException e) {
            System.out.println("Qualcosa è andato storto!");
            throw new RuntimeException(e);
        }
    }

    public String getIndexPath() {
        return this.indexPath;
    }

    public String getDocsPath() {
        return this.docsPath;
    }
    public Directory getDirectory() {
        return this.directory;
    }

    public void indexDocuments(IndexWriter writer, Path dir) throws IOException {
        if (Files.isDirectory(dir)) {
            File[] files = dir.toFile().listFiles((pathname) -> pathname.getName().endsWith(".json"));
            if (files != null) {
                for (File file : files) {
                    System.out.println(file.getName());
                    dataProcessor.processJSONData(file.toPath(),writer);
                }
                writer.close();
            }
        }
    }
}
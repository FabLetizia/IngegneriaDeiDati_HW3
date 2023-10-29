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

    public class Indexer {
        private String indexPath;
        private String docsPath;

        private Directory directory;
        private DataProcessor dataProcessor;

        public Indexer(DataProcessor dataProcessor) {
            this.dataProcessor = dataProcessor;
            //this.indexPath = "/Users/alessandropesare/software/GitHub/IngegneriaDeiDati_HW3/target";
            this.indexPath = "/target";
            //this.docsPath = "/Users/alessandropesare/software/GitHub/IngegneriaDeiDati_HW3/src/resources";
            this.docsPath = "src/resources";
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
                IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
                IndexWriter writer = new IndexWriter(directory, config);
                System.out.println("Inizio lettura dati e costruzione indice");
                long startTime = System.currentTimeMillis();
                indexDocuments(writer, Paths.get(this.getDocsPath()));
                writer.close();
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
                        Document luceneDoc = dataProcessor.processJSONData(readTextFile(file));
                        writer.addDocument(luceneDoc);
                        System.out.println(luceneDoc.toString());
                    }
                    writer.commit();
                }
            }
        }
    /*private Analyzer createAnalyzer() {
        CharArraySet contenutoStopWords = new CharArraySet(Arrays.asList("in", "dei", "di", "il", "la", "lo", "gli", "dell'"), true);
        Analyzer titoloAnalyzer = new WhitespaceAnalyzer();
        Analyzer contenutoAnalyzer = new StandardAnalyzer(contenutoStopWords);
        Analyzer defaultAnalyzer = new ItalianAnalyzer();

        Map<String, Analyzer> perFieldAnalyzers = new HashMap<>();
        perFieldAnalyzers.put("titolo", titoloAnalyzer);
        perFieldAnalyzers.put("contenuto", contenutoAnalyzer);

        return new PerFieldAnalyzerWrapper(defaultAnalyzer, perFieldAnalyzers);
    }*/

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
}

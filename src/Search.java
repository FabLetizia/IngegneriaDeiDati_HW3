public class Search {

    public static void main(String[] args) {
        DataProcessor dataProcessor = new JSONDataProcessor();
        Indexer indexer = new Indexer(dataProcessor);
        /*
        try {
            IndexReader reader = DirectoryReader.open(indexer.getDirectory());
            IndexSearcher searcher = new IndexSearcher(reader);
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                System.out.print("Inserisci una query(insieme di colonne) cioè una serie di parole chiave separate da spazi, oppure 'exit' per uscire: ");
                String userInput = br.readLine();
                if (userInput.equalsIgnoreCase("exit")) {
                    break;
                }

                // Split della stringa dell'utente in parole chiave
                String[] keywords = userInput.split(" ");

                // Creazione di una query booleana per cercare tutte le parole chiave nei campi desiderati
                BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();
                for (String keyword : keywords) {
                    Query termQueryTitolo = new TermQuery(new Term("titolo", keyword));
                    Query termQueryContenuto = new TermQuery(new Term("contenuto", keyword));
                    booleanQueryBuilder.add(termQueryTitolo, BooleanClause.Occur.SHOULD); // "SHOULD" indica l'OR logico
                    booleanQueryBuilder.add(termQueryContenuto, BooleanClause.Occur.SHOULD);
                }
                Query userQuery = booleanQueryBuilder.build();

                System.out.println("Esecuzione di query: " + userQuery.toString());

                // Esegui la query
                TopDocs hits = searcher.search(userQuery, 3);
                if (hits.scoreDocs.length == 0) {
                    System.out.println("Nessun documento trovato");
                }
                for (int j = 0; j < hits.scoreDocs.length; j++) {
                    ScoreDoc scoreDoc = hits.scoreDocs[j];
                    Document doc = searcher.doc(scoreDoc.doc);
                    System.out.println("doc" + scoreDoc.doc + ":" + doc.get("titolo") + " (" + scoreDoc.score + ")");
                }
            }
            // Chiudi il reader
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }
}
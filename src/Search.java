import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Search {

    private Query colonna_input;
    private Map<Integer, List<Object>> column2score;

    private int id;

    public int getId() {
        return id;
    }

    public Query getColonna_input() {
        return colonna_input;
    }

    public Map<Integer, List<Object>> getColumn2score() {
        return column2score;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Search(Query colonna_input, Map<Integer, List<Object>> column2score) {
        this.colonna_input = colonna_input;
        this.column2score = column2score;
        this.id = 0;
    }


    public static void main(String[] args) throws IOException {
        JSONDataProcessor dataProcessor = new JSONDataProcessor();
        Indexer indexer = new Indexer(dataProcessor);
        Directory directory = indexer.getDirectory();
        IndexReader reader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher(reader);
        Analyzer analyzer = new StandardAnalyzer();
        String contenutoColonna = "Bridge"; // Sostituisci con il valore fornito dall'utente
        String[] terms = contenutoColonna.split(" ");
        BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();
        for(String term : terms){
            Query termQuery = new TermQuery(new Term(term));
            booleanQueryBuilder.add(termQuery, BooleanClause.Occur.SHOULD);
        }
        BooleanQuery query = booleanQueryBuilder.build();

        Search search = new Search(query,new HashMap<>());

        TopDocs allDocs = searcher.search(query,Integer.MAX_VALUE);
        for(ScoreDoc scoreDoc: allDocs.scoreDocs){
            int docId = scoreDoc.doc;
            Document document = reader.document(docId);

            for(IndexableField field : document.getFields()){
                String fieldValue = field.stringValue();
                mergeList(search.getColumn2score(),search.getColonna_input(),fieldValue,search.getId());
                search.setId(search.getId()+1);
            }
        }
        //dopo aver popolato e ordinato la mappa column2score restituiamo le top k colonne
        int max_score = 0;
        int j = 0;
        List<String> results = new ArrayList<>();
        int id_max_score = 0;
        // k=3 è il numero di colonne di interesse
        while(j<=3){
            for(int i = 0; i<= search.getColumn2score().size(); i++){
                int scoreAttuale = (int) search.getColumn2score().get(i).get(0);
                if(scoreAttuale>max_score){
                    max_score = scoreAttuale;
                    id_max_score = i;
                }
            }
            results.add((String) search.getColumn2score().get(id_max_score).get(1));
            j++;
        }


    }

    private static void mergeList(Map<Integer, List<Object>> column2score, Query query, String colonna, int id) {
        String[] terminiQuery = query.toString().split(" ");
        int punteggio = 0;

        for (String termine : terminiQuery) {
            if (colonna.contains(termine)) {
                punteggio++;
            }
        }
        if(punteggio>0) {
            List<Object> scoreAndColumn = new ArrayList<>();
            scoreAndColumn.add(punteggio);
            scoreAndColumn.add(colonna);
            column2score.put(id, scoreAndColumn);
        }
    }


}
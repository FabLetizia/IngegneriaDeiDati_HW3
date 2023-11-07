import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;

public class Search {
	
	private static Directory directory;
	private static IndexReader reader;
	private static IndexSearcher searcher;
	
	
	private List<String> searchDocument(String colonna, int k) throws IOException{
		String[] terms = colonna.split(" ");
		BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();
		for(String term : terms){
			Query termQuery = new TermQuery(new Term("column_content",term));
			booleanQueryBuilder.add(termQuery, BooleanClause.Occur.SHOULD);
		}
		BooleanQuery query = booleanQueryBuilder.build();

		List<String> results = new ArrayList<>();
		
		TopDocs allDocs = searcher.search(query,k);
		for(ScoreDoc scoreDoc: allDocs.scoreDocs){
			int docId = scoreDoc.doc;
			Document document = reader.document(docId);
			results.add(document.toString());
			System.out.println(document);		
		}
		return results;
		
	}
	

	private List<String> mergeList(String colonna, int k) throws IOException {
		BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();
		String[] terms = colonna.split(" ");
		for(String term : terms){
			Query termQuery = new TermQuery(new Term("column_content",term));

			booleanQueryBuilder.add(termQuery, BooleanClause.Occur.SHOULD);
			BooleanQuery query = booleanQueryBuilder.build();

			Map<BooleanQuery, List<Document>> query2documents = new HashMap<>();
			List<Document> documents = new ArrayList<>();
			TopDocs allDocs = searcher.search(query,Integer.MAX_VALUE);
			for(ScoreDoc scoreDoc: allDocs.scoreDocs){
				int docId = scoreDoc.doc;
				Document document = reader.document(docId);
				System.out.println(document);
				documents.add(document);
						
			}
			query2documents.put(query, documents);
		}
		
		Map<Document, Integer> document2score = new HashMap<>();
		for(Document doc : document2score.keySet()) {
			document2score.compute(doc, (key, value) -> (value == null) ? 1 : value + 1);
		}
		
		// Converti la mappa in un elenco di voci e ordina per valore
        List<Map.Entry<Document, Integer>> sortedEntries = document2score.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toList());

        // Crea una mappa ordinata basata sul valore
        Map<Document, Integer> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<Document, Integer> entry : sortedEntries) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        // Ora hai una mappa (sortedMap) ordinata in base al valore
		List<String> results = new ArrayList<>();
        for(Document doc : sortedMap.keySet()) {
        	results.add(doc.toString());
        	k -= 1;
        	if(k == 0)
        		break;
        }
		
		return results;

	}
	

	public static void main(String[] args) throws IOException {
		JSONDataProcessor dataProcessor = new JSONDataProcessor();
		Indexer indexer = new Indexer(dataProcessor);
		directory = indexer.getDirectory();
		reader = DirectoryReader.open(directory);
		searcher = new IndexSearcher(reader);
		String contenutoColonna = "Bridge"; // Sostituisci con il valore fornito dall'utente
		Search searchColumn = new Search();
		List<String> resultsSearch = searchColumn.searchDocument(contenutoColonna, 5);
		List<String> resultsMerge = searchColumn.mergeList(contenutoColonna, 5);
		System.out.println(resultsSearch.size());
		System.out.println(resultsMerge.size());
	}


}
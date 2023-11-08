import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

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
		System.out.println("Algoritmo searchDocument: ");
		for(ScoreDoc scoreDoc: allDocs.scoreDocs){
			int docId = scoreDoc.doc;
			Document document = reader.document(docId);
			results.add(document.toString());
			System.out.println(document.getField("column_table"));
			System.out.println("\n");		


		}
		return results;
		
	}
	

	private List<String> mergeList(String colonna, int k) throws IOException {
		BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();
		String[] terms = colonna.split(" ");
		Map<String, List<Document>> query2documents = new HashMap<>();
		System.out.println("Algoritmo mergeList: ");
		for(String term : terms){
			Query termQuery = new TermQuery(new Term("column_content",term));

			booleanQueryBuilder.add(termQuery, BooleanClause.Occur.SHOULD);
			BooleanQuery query = booleanQueryBuilder.build();
			List<Document> documents = new ArrayList<>();
			TopDocs allDocs = searcher.search(query,Integer.MAX_VALUE);
			for(ScoreDoc scoreDoc: allDocs.scoreDocs){
				int docId = scoreDoc.doc;
				Document document = reader.document(docId);
				documents.add(document);
						
			}
			
			query2documents.put(term, documents);
		}
		//preoccupati di inizializzare i valori a 0
		Map<Document, Integer> document2score = new HashMap<>();

		for (List<Document> documentList : query2documents.values()) {
			for (Document doc : documentList) {
				// Inizializzare il conteggio per ogni documento a zero
				document2score.put(doc, 0);
			}
		}

		for(String term : query2documents.keySet()) {
			List<Document> documents = query2documents.get(term);
			for (Document doc : documents) {
				int currentCount = document2score.get(doc);
				document2score.put(doc, currentCount + 1);
			}
		}
		
		// Converti la mappa in un elenco di voci e ordina per valore
		List<Map.Entry<Document, Integer>> sortedEntries = document2score.entrySet().stream()
				.sorted(Map.Entry.<Document, Integer>comparingByValue().reversed())
				.collect(Collectors.toList());


		// Crea una mappa ordinata basata sul valore
        Map<Document, Integer> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<Document, Integer> entry : sortedEntries) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        // Ora hai una mappa (sortedMap) ordinata in base al valore
		List<String> results = new ArrayList<>();
		List<Document> documentResult = new ArrayList<>();
        for(Document doc : sortedMap.keySet()) {
        	results.add(doc.toString());
        	documentResult.add(doc);
        	System.out.println(doc.getField("column_table"));
			System.out.println("\n");
        	k -= 1;
        	if(k == 0)
        		break;
        }
        Statistics.searchStatistics(document2score, documentResult, colonna, k);
		
		return results;

	}
	

	public static void main(String[] args) throws IOException {
		System.out.println("*****START*****");
		JSONDataProcessor dataProcessor = new JSONDataProcessor();
		Indexer indexer = new Indexer(dataProcessor);
		directory = indexer.getDirectory();
	//  directory = FSDirectory.open(Paths.get("target/index"));
		reader = DirectoryReader.open(directory);
		searcher = new IndexSearcher(reader);
		String contenutoColonna = "dual"; // Sostituisci con il valore fornito dall'utente
		System.out.println("Inizio ricerca dei termini inseriti");
        long startTime1 = System.currentTimeMillis();
		Search searchColumn = new Search();
		List<String> resultsSearch = searchColumn.searchDocument(contenutoColonna, 5);
		System.out.println(resultsSearch.size());
		long endTime1 = System.currentTimeMillis();
        long time1 = endTime1 - startTime1;
        System.out.println("Il tempo trascorso per la ricerca searchDocument è: " + time1 + " millisecondi");
        long startTime2 = System.currentTimeMillis();
        List<String> resultsMerge = searchColumn.mergeList(contenutoColonna, 5);
		System.out.println(resultsMerge.size());
		long endTime2 = System.currentTimeMillis();
        long time2 = endTime2 - startTime2;
        System.out.println("Il tempo trascorso per la ricerca mergeList è: " + time2 + " millisecondi");
	}
	
}
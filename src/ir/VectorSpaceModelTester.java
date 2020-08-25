package ir;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;

/**
 * the tester class.
 * @author swapneel
 */
public class VectorSpaceModelTester {
	
	
	// extrating the maximum n key, value pairs by comparing the values from a hash map.
	private static <K, V extends Comparable<? super V>> List<Entry<K, V>> 
    findGreatest(Map<K, V> map, int n)
{
    Comparator<? super Entry<K, V>> comparator = 
        new Comparator<Entry<K, V>>()
    {
        @Override
        public int compare(Entry<K, V> e0, Entry<K, V> e1)
        {
            V v0 = e0.getValue();
            V v1 = e1.getValue();
            return v0.compareTo(v1);
        }
    };
    PriorityQueue<Entry<K, V>> highest = 
        new PriorityQueue<Entry<K,V>>(n, comparator);
    for (Entry<K, V> entry : map.entrySet())
    {
        highest.offer(entry);
        while (highest.size() > n)
        {
            highest.poll();
        }
    }

    List<Entry<K, V>> result = new ArrayList<Map.Entry<K,V>>();
    while (highest.size() > 0)
    {
        result.add(highest.poll());
    }
    return result;
}
	public static void main(String[] args) throws IOException {
		
		//pre processing importing the documents into the vector space and calculating tfidf values 
		ArrayList<Document> documents = new ArrayList<Document>();
		
		BufferedReader brTest = new BufferedReader(new FileReader("stocks/profiles.txt"));
		while(brTest.readLine()!=null) {
		    String text = brTest.readLine();
		    String arr[] = text.split(" ", 2);
		    String company = arr[0];
		    String description = arr[1];
		    String filename = company + ".txt";
		    BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
		    writer.write(description);
		    writer.close();

		    Document document = new Document(filename);
		    documents.add(document);
		}
		// taking the percent growth after 60 days 
		String line;
		HashMap<String,Double> stockPrices = new HashMap<String,Double>();
		BufferedReader br = new BufferedReader(new FileReader("stocks/history_60d.csv"));
		String currSymbol = "";
		Double start = 0.0;
		Double end = 0.0;
		br.readLine();
		while ((line = br.readLine()) != null) {
		    String[] cols = line.split(",");
		    if (!cols[1].equals(currSymbol)) {
		    	stockPrices.put(currSymbol, (start - end) / end);
		    	currSymbol = cols[1];
		    	start = Double.parseDouble(cols[4]);
		    	end = Double.parseDouble(cols[4]);
		    } else {
		    	end = Double.parseDouble(cols[4]);
		    }
		}
		//goes into each document and multiples each tfidf value by the percent growth then adding it 
		//into a hashmap that conatins a aggregated value of each word.
		Corpus corpus = new Corpus(documents);
		VectorSpaceModel vectorSpace = new VectorSpaceModel(corpus);
		HashMap<String,Double> answer = new HashMap<String,Double>();
		
		for (Document d: vectorSpace.getTfIdfWeights().keySet()) {
			String filename = d.getFileName();
			filename = filename.substring(0, filename.length() - 4);
			if (stockPrices.keySet().contains(filename)) {
				Double factor = stockPrices.get(filename);
				HashMap<String,Double> tfidf = vectorSpace.getTfIdfWeights().get(d);
				for (String s: tfidf.keySet()) {
					Double old = tfidf.get(s);
					tfidf.replace(s, old * factor);
					if (answer.containsKey(s)) {
						Double superold = answer.get(s); 
						answer.replace(s, superold + old * factor);
					} else {
						answer.put(s, old * factor);
					}
				}
			}
			
		}
		//get n highest
		int n = 10;
		List<Entry<String, Double>> greatest = findGreatest(answer, n);
		for (Entry<String,Double> e :greatest) {
			System.out.println(e.getKey());
		}
		
		
		
	}

}

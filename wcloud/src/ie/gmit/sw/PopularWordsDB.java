package ie.gmit.sw;

import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class PopularWordsDB {
	
	public Map<String, Integer> popularWordsDB = new ConcurrentHashMap<>();
	// Use a ConcurrentSkipListSet because it contains only unique elements and it
	// is thread safe.
	public Set<String> ignoreWords = new ConcurrentSkipListSet<>();
	
	 public LinkedHashMap<String, Integer> sortedDB(int maxiNumDisplay) {
		int counter = 0;
		// LinkedHashMap preserve the ordering of elements in which they are inserted
		LinkedHashMap<String, Integer> reverseSortedMap = new LinkedHashMap<>();
		// Use Comparator.reverseOrder() for reverse ordering
		popularWordsDB.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
				.forEachOrdered(x -> reverseSortedMap.put(x.getKey(), x.getValue()));
		Iterator<String> keySetIterator = reverseSortedMap.keySet().iterator();
		while (keySetIterator.hasNext() && counter < maxiNumDisplay) {
			counter++;
			//String key = keySetIterator.next();
		}

		return reverseSortedMap;
	}
	 
	    // extract each word from the title,heading and paragraph strings
		// and add it to the popularWordsDB after removing the ignore words
		public void addWordsToDB(String wordString) {
			wordString = wordString.replaceAll("[^a-zA-Z0-9\\s]", "").toLowerCase();
			// Split the string at the spaces
			String[] titleWords = wordString.trim().split("\\s+");
			for (String titleWord : titleWords) {
				if (!ignoreWords.contains(titleWord) && !titleWord.matches("-?\\d+")) {
					// -----------------------------------
					// Initialize the word frequency to 1.
					// Increment the word frequency if the word is
					// already in the database.
					// ------------------------------------
					int frequency = 1;
					if (popularWordsDB.containsKey(titleWord)) {
						frequency += popularWordsDB.get(titleWord).intValue();
					}
					// --------------------------------
					// At the word at the end if it is
					// not already in the database.
					// --------------------------------
					popularWordsDB.put(titleWord, frequency);
				}
			}
		}
}

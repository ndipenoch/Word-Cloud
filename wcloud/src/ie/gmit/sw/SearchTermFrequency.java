package ie.gmit.sw;

public class SearchTermFrequency {

	public int getTitleFrequency(String s, String searchTerm) {
		int termPresntCount = 0;
		// remove all non-alphanumeric characters from the
		// string except spaces and convert it to lower case.
		s = s.replaceAll("[^a-zA-Z0-9\\s]", "").toLowerCase();
		// Split the string at the spaces
		String[] stringWords = s.trim().split("\\s+");
		for (String word : stringWords) {
			// Increment the counter if you find the word
			if (searchTerm.contains(word)) {
				termPresntCount++;
			}
		}
		return termPresntCount;
	}

	public int getHeadingFrequency(String s, String searchTerm) {
		int termPresntCount = 0;
		// remove all non-alphanumeric characters from the
		// string except spaces and convert it to lower case.
		s = s.replaceAll("[^a-zA-Z0-9\\s]", "").toLowerCase();
		// Split the string at the spaces
		String[] stringWords = s.trim().split("\\s+");
		for (String word : stringWords) {
			// Increment the counter if you find the word
			if (searchTerm.contains(word)) {
				termPresntCount++;
			}
		}
		// System.out.println("Headings score \t" + termPresntCount);
		return termPresntCount;
	}

	public int getParagraphFrequency(String s, String searchTerm) {
		int termPresntCount = 0;
		// remove all non-alphanumeric characters from the
		// string except spaces and convert it to lower case.
		s = s.replaceAll("[^a-zA-Z0-9\\s]", "").toLowerCase();
		// Split the string at the spaces
		String[] stringWords = s.trim().split("\\s+");
		for (String word : stringWords) {
			// Increment the counter if you find the word
			if (searchTerm.contains(word)) {
				termPresntCount++;
			}
		}
		// System.out.println("Pargraphs score \t" + termPresntCount);
		return termPresntCount;
	}
}

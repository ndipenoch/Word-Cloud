package ie.gmit.sw;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class NodeParser implements Runnable {
	private static int maxSearch = 50;
	private static final int TITLE_WEIGHT = 50;
	private static final int HEADING_WEIGHT = 20;
	private static final int PARAGRAPH_WEIGHT = 1;
	private String term;
	public int maximumDisplay = 30;
	public int aiPredictType = 2;
	public int visitedNodes = 0;
	public int MaxSearchDepth = -999;

	// Store all the node you visit
	// Starting by the parent node(URL),
	// pass by the user.
	private Set<String> closed = new ConcurrentSkipListSet<>();
	public WordFrequencyList wfl;

	// For each of the links visited
	// score them and store them in a Queue
	// started with the most important or those with the best scores
	// i.e sorted by score
	private Queue<DocumentNode> queue = new PriorityQueue<>(Comparator.comparing(DocumentNode::getScore));

	NeuralNetwork nn = new NeuralNetwork();
	SearchTermFrequency stf = new SearchTermFrequency();
	FuzzyLogic fl = new FuzzyLogic();
	PopularWordsDB pwdb = new PopularWordsDB();

	public NodeParser(Set<String> ignoreWordSet, String fclFile, String searchTerm, String threadID, int maxEdge,
			int maxDisplay, int aiType, int goalThreshold) throws IOException {

		maxSearch = maxEdge;
		maximumDisplay = maxDisplay;
		aiPredictType = aiType;
		int numTopSearch = 0;

		if (aiPredictType == 1) {
			// load the neuralNetwork DB
			// or create a model
			nn.loadNNDB();
		}
		// List of sorted words in descending order
		LinkedHashMap<String, Integer> WordFrequency = new LinkedHashMap<>();

		// Load the Ignore words to a setMap
		pwdb.ignoreWords = ignoreWordSet;
		// Convert the search term to lower case
		this.term = searchTerm.toLowerCase();
		try {
			// Document doc = Jsoup.connect(url).get();
			Document doc = Jsoup.connect("https://duckduckgo.com/html/?q=" + searchTerm).get();
			Elements results = doc.getElementById("links").getElementsByClass("results_links");
			for (Element result : results) {
				numTopSearch++;
				Element title = result.getElementsByClass("links_main").first().getElementsByTag("a").first();
				String url = title.attr("href");
				Document document = Jsoup.connect(url).get();
				int score = getHeuristicScore(document, term, fclFile);
				closed.add(url);
				// Score the URL/link pass by the user
				// and add it to the queue.
				queue.offer(new DocumentNode(doc, score));

				// Process the URL/Links and subsequent pages/links
				// in the URL pass by the user and add them to the queue
				process(fclFile);
				if (numTopSearch >= goalThreshold) {
					break;
				}

			}

		} catch (Exception e) {
			System.out.println("----------Invalid Url/Link-------------");
		}

		// Sort the words in the database by value in descending
		// order with the most occur words at the top
		WordFrequency = pwdb.sortedDB(maximumDisplay);
		visitedNodes = closed.size();
		// branching factor = x^1/d
		double power = 1.0 / (MaxSearchDepth);
		// where x = number of visited nodes and d = search depth.
		double branchingFactor = Math.pow(visitedNodes, power);
		// round the branching factor to two decimal places.
		branchingFactor = Math.round(branchingFactor * 100.0) / 100.0;
		wfl = new WordFrequencyList(WordFrequency, threadID, maxDisplay, MaxSearchDepth, branchingFactor);

	}

	public void process(String fclFile) {
		int searchDepthCnt = 0;
		while (!queue.isEmpty() && searchDepthCnt < maxSearch) {
			DocumentNode node = queue.poll();
			Document doc = node.getDocument();
			Elements edges = doc.select("a[href]");
			// increment the search depth count
			searchDepthCnt++;
			if (searchDepthCnt > MaxSearchDepth) {
				MaxSearchDepth = searchDepthCnt;
			}
			// for each link get the links/pages/absolute URL or children node
			for (Element e : edges) {
				try {
					String link = e.absUrl("href");
					// check if the URL or child node is not already visit
					// i.e not in the close list
					// Check if the node or URL is not Empty
					// Check if we are within the given range or size.
					if (link != null && closed.size() < maxSearch && !closed.contains(link)) {
						try {
							closed.add(link);
							Document child = Jsoup.connect(link).get();
							int score = getHeuristicScore(child, term, fclFile);
							queue.offer(new DocumentNode(child, score));
						} catch (IOException e1) {

						}
					}
				} catch (Exception e1) {
					System.out.println("--------------URL/LINK is Invalid-----------------------");
				}
			}
		}
	}

	private int getHeuristicScore(Document doc, String searchTeam, String fclFile) {
		int score = 0;
		int titleScore = 0;
		int headingScore = 0;
		int paragraphScore = 0;
		String body = "";
		String title = "";
		String hTags = "";

		// Get the words in the tile
		title = doc.title();
		// System.out.println(closed.size()+ " <----> " + title);
		titleScore += stf.getTitleFrequency(title, searchTeam) * TITLE_WEIGHT;

		// Get the words in the heading
		// Multiple headings tag used
		Elements headings = doc.select("h1, h2, h3, h4, h5, h6");
		for (Element heading : headings) {
			hTags = heading.text();
			// System.out.println("\t"+ h1);
			headingScore += stf.getHeadingFrequency(hTags, searchTeam) * HEADING_WEIGHT;
		}
		// Get the words in the body
		try {
			body = doc.body().text();
			paragraphScore += stf.getParagraphFrequency(body, searchTeam) * PARAGRAPH_WEIGHT;
		} catch (Exception e) {

		}

		if (aiPredictType == 2) {
			// call getFuzzyHeuristic method
			// return the score
			score = fl.getFuzzyHeuristic(titleScore, headingScore, paragraphScore, fclFile);
		} else {
			// call neuralNetworkPrediction method
			// predict and return the score
			score = nn.neuralNetworkPrediction(titleScore, headingScore, paragraphScore);
		}

		// getHeuristicScore does not neccessary need to return any thing.
		// if score is greater than say 100 then call the index
		if (score >= 15) {
			index(title, hTags, body);
		}

		return score;
	}

	private void index(String title, String heading, String paragraph) {
		pwdb.addWordsToDB(title);
		pwdb.addWordsToDB(heading);
		pwdb.addWordsToDB(paragraph);

	}

	public class WordFrequencyList {
		private LinkedHashMap<String, Integer> SortedMap;
		private String threadID;
		private int maxDisplay;
		private int searchDepthCnt;
		private double branchingFactor;

		public LinkedHashMap<String, Integer> getSortedMap() {
			return SortedMap;
		}

		public void setSortedMap(LinkedHashMap<String, Integer> sortedMap) {
			SortedMap = sortedMap;
		}

		public String getThreadID() {
			return threadID;
		}

		public void setThreadID(String threadID) {
			this.threadID = threadID;
		}

		public int getMaxDisplay() {
			return maxDisplay;
		}

		public void setMaxDisplay(int maxDisplay) {
			this.maxDisplay = maxDisplay;
		}

		public int getSearchDepthCnt() {
			return searchDepthCnt;
		}

		public void setSearchDepthCnt(int searchDepthCnt) {
			this.searchDepthCnt = searchDepthCnt;
		}

		public double getBranchingFactor() {
			return branchingFactor;
		}

		public void setBranchingFactor(double branchingFactor) {
			this.branchingFactor = branchingFactor;
		}

		public WordFrequencyList(LinkedHashMap<String, Integer> sortedMap, String threadID, int maxDisplay,
				int searchDepthCnt, double branchingFactor) {
			super();
			SortedMap = sortedMap;
			this.threadID = threadID;
			this.maxDisplay = maxDisplay;
			this.searchDepthCnt = searchDepthCnt;
			this.branchingFactor = branchingFactor;
		}

	}

	private class DocumentNode {
		private Document document;
		private int score;

		public Document getDocument() {
			return document;
		}

		public int getScore() {
			return score;
		}

		public void setScore(int score) {
			this.score = score;
		}

		public DocumentNode(Document document, int score) {
			super();
			this.document = document;
			this.score = score;
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}

}

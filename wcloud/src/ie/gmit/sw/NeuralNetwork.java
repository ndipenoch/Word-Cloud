package ie.gmit.sw;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class NeuralNetwork {
	
	    // create a neural network model or database with all possible outcomes.
		// Then search or predict from the database or model.
		// Since the model or database contains all the outcomes or possibilities
		// You are guarantee at 100% to find a result thus the prediction will have 100
		// accuracy rate.
		public Map<String, Integer> neuralNetworkDB = new ConcurrentHashMap<>();
	
	    public int neuralNetworkPrediction(int paragraphsScore, int titlesScore, int headingsScore) {

		//Make the rules to transform the score.
		if (paragraphsScore > 100) {
			paragraphsScore = 2;
		} else if (paragraphsScore > 1) {
			paragraphsScore = 1;
		} else {
			paragraphsScore = 0;
		}

		if (titlesScore > 120) {
			titlesScore = 2;
		} else if (titlesScore > 60) {
			titlesScore = 1;
		} else {
			titlesScore = 0;
		}

		if (headingsScore > 150) {
			headingsScore = 2;
		} else if (headingsScore > 70) {
			headingsScore = 1;
		} else {
			headingsScore = 0;
		}

		String predictKey = String.valueOf(paragraphsScore) + String.valueOf(titlesScore)
				+ String.valueOf(headingsScore);
		int predictedScore = 0;
		for (Entry<String, Integer> entry : neuralNetworkDB.entrySet()) {
			if (entry.getKey().equals(predictKey)) {
				predictedScore = entry.getValue();
			}
		}

		return predictedScore;
	}
	    
	    // populate the db or create the model
		// with all possible combination and score.
		// 0 is poor, 1 is good and 2 is excellent.
		// The first column, is the paragraph.
		// The second column is the title.
		// The third column is the heading.
		// Score is either 0 bad, 15 for medium and 20 for excellent.
		// So, for example 121 will give you a medium score of 15 and
		// 210 will give you an excellent score 20.
		public void loadNNDB() {
			neuralNetworkDB.put("000", 0);
			neuralNetworkDB.put("100", 15);
			neuralNetworkDB.put("200", 20);
			neuralNetworkDB.put("010", 0);
			neuralNetworkDB.put("020", 15);
			neuralNetworkDB.put("001", 0);
			neuralNetworkDB.put("101", 15);
			neuralNetworkDB.put("102", 15);
			neuralNetworkDB.put("110", 15);
			neuralNetworkDB.put("111", 15);
			neuralNetworkDB.put("022", 15);
			neuralNetworkDB.put("120", 20);
			neuralNetworkDB.put("121", 15);
			neuralNetworkDB.put("122", 20);
			neuralNetworkDB.put("112", 15);
			neuralNetworkDB.put("201", 20);
			neuralNetworkDB.put("202", 20);
			neuralNetworkDB.put("210", 20);
			neuralNetworkDB.put("220", 20);
			neuralNetworkDB.put("221", 20);
			neuralNetworkDB.put("222", 20);

		}

}

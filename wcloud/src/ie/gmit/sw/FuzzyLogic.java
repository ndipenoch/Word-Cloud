package ie.gmit.sw;

import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.FunctionBlock;
import net.sourceforge.jFuzzyLogic.rule.Variable;

public class FuzzyLogic {
	
	public int getFuzzyHeuristic(int title, int headings, int body, String fclFile) {
		FIS fis = FIS.load(fclFile, true);

		FunctionBlock fb = fis.getFunctionBlock("wcloud");

		// Set inputs
		fis.setVariable("title", title);
		fis.setVariable("headings", headings);
		fis.setVariable("paragraphs", body);

		// Evaluate
		fis.evaluate();

		// Show output variable's chart
		Variable score = fb.getVariable("score");

		int finalFuzzyScore = (int) Math.round(score.getLatestDefuzzifiedValue());

		return finalFuzzyScore;
	}

}

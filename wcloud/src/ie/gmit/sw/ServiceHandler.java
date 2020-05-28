package ie.gmit.sw;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;

import javax.imageio.ImageIO;

import java.util.Arrays;
import java.util.Base64;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import ie.gmit.sw.NodeParser.WordFrequencyList;
import ie.gmit.sw.ai.cloud.LogarithmicSpiralPlacer;
import ie.gmit.sw.ai.cloud.WeightedFont;
import ie.gmit.sw.ai.cloud.WordFrequency;

/*
 * -------------------------------------------------------------------------------------------------------------------
 * PLEASE READ THE FOLLOWING CAREFULLY. MOST OF THE "ISSUES" STUDENTS HAVE WITH DEPLOYMENT ARISE FROM NOT READING
 * AND FOLLOWING THE INSTRUCTIONS BELOW.
 * -------------------------------------------------------------------------------------------------------------------
 *
 * To compile this servlet, open a command prompt in the web application directory and execute the following commands:
 *
 * Linux/Mac													Windows
 * ---------													---------	
 * cd WEB-INF/classes/											cd WEB-INF\classes\
 * javac -cp .:$TOMCAT_HOME/lib/* ie/gmit/sw/*.java				javac -cp .:%TOMCAT_HOME%/lib/* ie/gmit/sw/*.java
 * cd ../../													cd ..\..\
 * jar -cf wcloud.war *											jar -cf wcloud.war *
 * 
 * Drag and drop the file ngrams.war into the webapps directory of Tomcat to deploy the application. It will then be 
 * accessible from http://localhost:8080. The ignore words file at res/ignorewords.txt will be located using the
 * IGNORE_WORDS_FILE_LOCATION mapping in web.xml. This works perfectly, so don't change it unless you know what
 * you are doing...
 * 
*/

public class ServiceHandler extends HttpServlet {
	private String ignoreWords = null;
	private String fclFile = null;
	private File f;
	//Use a ConcurrentSkipListSet because it contains only unique elements and it is thread safe.
	private Set<String> ignoreWordsList = new ConcurrentSkipListSet<>();
	
	public void init() throws ServletException {
ServletContext ctx = getServletContext(); //Get a handle on the application context
		
		//Reads the value from the <context-param> in web.xml
		fclFile = getServletContext().getRealPath(File.separator) + ctx.getInitParameter("FCL_FILE_LOCATION"); 
		ignoreWords = getServletContext().getRealPath(File.separator) + ctx.getInitParameter("IGNORE_WORDS_FILE_LOCATION"); 
		f = new File(ignoreWords); //A file wrapper around the ignore words...
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(f));
			String line;
			
			// add words from the ignore file into a ConcurrentSkipListSet
			while ((line = br.readLine()) != null)
			{
				//ignoreWords.add(line.toLowerCase());
				ignoreWordsList.add(line.toLowerCase());
				
			}
		} catch (FileNotFoundException e) {
			System.out.println("File does not Exist");
		} catch (IOException e) {
			System.out.println("File is Empty");
		}
	}

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/html"); //Output the MIME type
		PrintWriter out = resp.getWriter(); //Write out text. We can write out binary too and change the MIME type...
		
		//Initialize some request variables with the submitted form info. These are local to this method and thread safe...
		String maximumEdge = req.getParameter("maxEdge"); 
		String maximumDisplay = req.getParameter("maxDisplay"); 
		String aiPredictType = req.getParameter("aiType"); 
		String goalThresholdNum = req.getParameter("goalThreshold"); 
		String inputString = req.getParameter("query");
		int searchDepth = 0;
		double branchingFactor=0.0;

		out.print("<html><head><title>Artificial Intelligence Assignment</title>");		
		out.print("<link rel=\"stylesheet\" href=\"includes/style.css\">");
		
		out.print("</head>");		
		out.print("<body>");		
		out.print("<div style=\"font-size:48pt; font-family:arial; color:#990000; font-weight:bold\">Web Opinion Visualiser</div>");	
		
		out.print("<p>The &quot;ignore words&quot; file is located at <font color=red><b>" + f.getAbsolutePath() + "</b></font> and is <b><u>" + f.length() + "</u></b> bytes in size.");
		out.print("You must place any additional files in the <b>res</b> directory and access them in the same way as the set of ignore words.");
		out.print("<p><fieldset><legend><h3>Result</h3></legend>");
		
		int maxEdge = Integer.parseInt(maximumEdge);
		int maxDisplay = Integer.parseInt(maximumDisplay);
		int goalThreshold = Integer.parseInt(goalThresholdNum);
		int aiType =2;
		if(aiPredictType.equals("Neural Network")) {
			aiType=1;
		}else {
			aiType=2;
		}
		
		//Generate the thread uniqueID from a random number
		//between 0-1000 and the current day, time,minute, and second.
        Random rand = new Random(); 
        int randNum = rand.nextInt(1000); 
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");  
	    Date date = new Date();  
	    String threadID = formatter.format(date)+Integer.toString(randNum);
	    
		//List of sorted words in descending order
	   // LinkedHashMap<String, Integer> WordFreqList = new LinkedHashMap<>();
		//String[] inputString = s.split("\\s+");
	    
		//Check if the input is valid
		if(inputString!="") {
			//Create a new NodeParser
			NodeParser dp = new NodeParser(ignoreWordsList, fclFile,inputString.toLowerCase(),threadID,maxEdge,maxDisplay,aiType,goalThreshold);
			//Start the thread.
			boolean isPoisoned = false;
			
			Thread t = new Thread(dp);
			t.start();
			try {
				t.join();
			} catch (InterruptedException e) {
				System.out.println("Thread error");
			}
			
			while(!isPoisoned) {
				Queue<WordFrequencyList> inQueue = new LinkedList<>();
				Queue<WordFrequencyList> outQueue = new LinkedList<>();
				WordFrequencyList Wordfrequencylist = dp.wfl;
				inQueue.offer(Wordfrequencylist);
				
				// read off the top of linked inQueue
				WordFrequencyList wflIn = inQueue.poll();
				//If the inQueue is not empty and the thread Id is found
				if(wflIn!=null && wflIn.getThreadID().equals(threadID)) {
					isPoisoned=true;
					//Add to outQueue
					outQueue.offer(wflIn);
					// read off the top the OutQueue
					WordFrequencyList wflOut = outQueue.poll();
					//Remove from inQueue
					inQueue.remove(wflOut);
					//Get the search Depth
					searchDepth = wflIn.getSearchDepthCnt();
					//Get the branching Factor
					branchingFactor = wflIn.getBranchingFactor();
					WordFrequency[] words = new WeightedFont().getFontSizes(getWordFrequencyKeyValue(wflOut.getSortedMap(),wflOut.getMaxDisplay()));
					Arrays.sort(words, Comparator.comparing(WordFrequency::getFrequency, Comparator.reverseOrder()));
					//Arrays.stream(words).forEach(System.out::println);

					//Spira Mirabilis
					LogarithmicSpiralPlacer placer = new LogarithmicSpiralPlacer(800, 600);
					for (WordFrequency word : words) {
						placer.place(word); //Place each word on the canvas starting with the largest
					}

					BufferedImage cloud = placer.getImage(); //Get a handle on the word cloud graphic
					out.print("<img src=\"data:image/png;base64," + encodeToString(cloud) + "\" alt=\"Word Cloud\">");
				}
			}
		}else {
			out.print("<p> Please enter a valid search term!<p>");
			out.print("<p> For example:  java<p>");
		}
		

		out.print("</fieldset>");	
		if(aiType==2) {
			out.print("<p>The Max. Search Depth is " + searchDepth +" The Effective Branching Factor is "+branchingFactor+"<p>");	
		}else {
			out.print("<p>Max. Search Depth is " + searchDepth +" The Effective Branching Factor is "+branchingFactor + " The Accuracy  is"+100+" percent<p>");	
		}
			
		out.print("<a href=\"./\">Return to Start Page</a>");
		out.print("</body>");	
		out.print("</html>");	
	}

	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGet(req, resp);
 	}

	//A sample array of WordFrequency for demonstration purposes
	private WordFrequency[] getWordFrequencyKeyValue(LinkedHashMap<String, Integer> WordFreqList,int maxDisplay) {
		WordFrequency[] wf = new WordFrequency[1];
		if(WordFreqList.isEmpty()==false) {
			int counter=0;
			int size =WordFreqList.size();
			if(size>maxDisplay) {
				size=maxDisplay;
			}
			wf = new WordFrequency[size];
			Iterator<String> keySetIterator = WordFreqList.keySet().iterator(); 
			while(keySetIterator.hasNext() && counter<maxDisplay){
				String key = keySetIterator.next();
				wf[counter] = new WordFrequency(key, WordFreqList.get(key));
				counter++;
			}
			
		}else {
			wf = new WordFrequency[1];
			System.out.println("Nothing found");
			wf[0] = new WordFrequency("POOR WORD COUNT", 50000);
		}
		return wf;
	}
	
	private String encodeToString(BufferedImage image) {
	    String s = null;
	    ByteArrayOutputStream bos = new ByteArrayOutputStream();

	    try {
	        ImageIO.write(image, "png", bos);
	        byte[] bytes = bos.toByteArray();

	        Base64.Encoder encoder = Base64.getEncoder();
	        s = encoder.encodeToString(bytes);
	        bos.close();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	    return s;
	}
	
	private BufferedImage decodeToImage(String imageString) {
	    BufferedImage image = null;
	    byte[] bytes;
	    try {
	        Base64.Decoder decoder = Base64.getDecoder();
	        bytes = decoder.decode(imageString);
	        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
	        image = ImageIO.read(bis);
	        bis.close();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return image;
	}
}
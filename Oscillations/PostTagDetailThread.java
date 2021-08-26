import java.util.ArrayList;
import java.util.Collections;

public class PostTagDetailThread extends Thread{
	private MultiThreadCrawler host;
	private String threadName, initialString, one;
	private int totalStrings, stepLimit, cores;
	private PostTag iterator;
	private ArrayList<int[]> list;
	private ArrayList<int[]> oscillations;
	private ArrayList<String[]> oscillStrings;
	private ArrayList<String> seedHistory;
	public PostTagDetailThread(String a, int x, int y, int z, int c, MultiThreadCrawler q) {
		threadName = a;
		totalStrings = y;
		stepLimit = z;
		cores = c;
		host = q;
		initialString = "";
		one = "1";
		for(int j=0; j<x; j++)
			initialString = initialString + "1";
		iterator = new PostTag(initialString);
		list = new ArrayList<int[]>();
		oscillations = new ArrayList<int[]>();
		oscillStrings = new ArrayList<String[]>();
	}

	public String getInitialString() {
		return initialString;
	}

	public void run() {
		System.out.println ("Thread " +
                Thread.currentThread().getId() +
                " is running");
		for(int i=0; i<totalStrings; i++) {
			int x = iterator.getInitString().length();
			String a = iterator.getInitString();
			seedHistory = new ArrayList<String>();
			//System.out.println(iterator.getInitString());
			int count = 0;
			while(a!=""&&count<stepLimit) {
				String antecedent = iterator.iteratePostTag(a);
				//System.out.println(antecedent+"   "+antecedent.length()+"   "+count+" "+ x);
				if(iterator.getInitString().length()==1) {
					list.add(new int[2]);
					list.get(list.size()-1)[0] = x;
					list.get(list.size()-1)[1] = count;
					count=stepLimit;
				}
				for(int j=0; j<seedHistory.size(); j++) {
					if(antecedent.contentEquals(seedHistory.get(j))) {
						System.out.println("String reached an oscillation.");
						oscillations.add(new int[3]); // [seed string length, steps to a repeat, oscillation period]
						oscillations.get(oscillations.size()-1)[0] = x;
						oscillations.get(oscillations.size()-1)[1] = count;
						oscillations.get(oscillations.size()-1)[2] = (seedHistory.size()-1) - j;
						oscillStrings.add(new String[2]);
						oscillStrings.get(oscillStrings.size()-1)[0] = Integer.toString(x);
						oscillStrings.get(oscillStrings.size()-1)[1] = antecedent;
						count=stepLimit;
					}
				}
				seedHistory.add(antecedent);
				iterator.setInitString(antecedent);
				a = iterator.getInitString();
				count++;
			}
			System.out.println(x);
			initialString = initialString + String.join("", Collections.nCopies(cores, one));
			iterator.setInitString(initialString);
		}
	//	System.out.println(threadName + " output:");
	/**	for(int i = 0; i < list.size(); i++) {
		    System.out.println(list.get(i)[0]+": "+list.get(i)[1]);
		}
		System.out.println();
		System.out.println("Oscillating Seeds: ")
		for(int j = 0; j < oscillations.size(); j++){
				System.out.println(oscillations.get(j)[0]+": "+oscillations.get(j)[1]+", "+oscillations.get(j[2]));
		}**/
		host.appendResults(list, oscillations, oscillStrings);
		host.incrementThreadClosed();
	}
}

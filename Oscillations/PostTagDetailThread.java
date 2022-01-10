import java.util.ArrayList;
import java.util.Collections;
import java.util.*;

public class PostTagDetailThread extends Thread{
	private MultiThreadCrawler host;
	private String threadName, initialString, one, antecedent;
	private int totalStrings, stepLimit, cores;
	private PostTag iterator;
	private ArrayList<int[]> list, oscillations;
	private ArrayList<String[]> oscillStrings;
	private ArrayList<String> seedHistory;
	private ArrayList<Integer> exhaustedStepLimit;
	private Map<Integer, ArrayList<String>> stringBuckets;
	private boolean dataOffloaded;
	private Random gen;
	public PostTagDetailThread(String a, int x, int y, int z, int c, MultiThreadCrawler q) {
		threadName = a;
		totalStrings = y;
		stepLimit = z;
		cores = c;
		host = q;
		initialString = "";
		one = "1";
		dataOffloaded = false;
		gen = new Random();
		for(int j=0; j<x; j++)
			initialString = initialString + "1";
		iterator = new PostTag(initialString);
		list = new ArrayList<int[]>();
		oscillations = new ArrayList<int[]>();
		exhaustedStepLimit = new ArrayList<Integer>();
		oscillStrings = new ArrayList<String[]>();
	}

	public void printOscillStrings(){
		for(String[] a: oscillStrings)
			System.out.println(""+a[0]+", "+a[1]);
	}

	public void printOscillations(){
		for(int[] a: oscillations)
			System.out.println(""+a[0]+", "+a[1]+", "+a[2]);
	}
	public void printStringBuckets(){
		System.out.println("Start bucket set.");
		for(ArrayList<String> a : stringBuckets.values()){
			for(String b : a)
				System.out.println(b);
			System.out.println();
		}
		System.out.println("End bucket set.");
	}

	public boolean dataOffloaded(){
		return dataOffloaded;
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
			stringBuckets = new HashMap<Integer, ArrayList<String>>();
			for(int count=0; a!=""&&count<stepLimit; count++) {
				antecedent = iterator.iteratePostTag(a);
				if(iterator.getInitString().length()==1) {
					System.out.println("String reached null string");
					list.add(new int[2]);
					list.get(list.size()-1)[0] = x-1;
					list.get(list.size()-1)[1] = count+1;
					list.add(new int[2]);
					list.get(list.size()-1)[0] = x;
					list.get(list.size()-1)[1] = count;
					list.add(new int[2]);
					list.get(list.size()-1)[0] = x;
					list.get(list.size()-1)[1] = count-1;
					count=stepLimit;
				}
				if(stringBuckets.containsKey(antecedent.length())) {
					for(String m : stringBuckets.get(antecedent.length())) { //antecedent string length buckets search reduce filter
						if(antecedent.equals(m)) {
							for(int j=seedHistory.size()-1; j>=0; j--) {
								if(antecedent.contentEquals(seedHistory.get(j))) {
									System.out.println("String reached an oscillation.");
									oscillations.add(new int[3]);
									oscillations.get(oscillations.size()-1)[0] = x-1;
									oscillations.get(oscillations.size()-1)[1] = count+2;
									oscillations.get(oscillations.size()-1)[2] = seedHistory.size()-1 - j; // Inferred result --
									oscillStrings.add(new String[2]);
									oscillStrings.get(oscillStrings.size()-1)[0] = Integer.toString(x-1);
									oscillStrings.get(oscillStrings.size()-1)[1] = antecedent;
									// ------------------------------------------------------------------------------------
									oscillations.add(new int[3]); // [seed string length, steps to a repeat, oscillation period]
									oscillations.get(oscillations.size()-1)[0] = x;
									oscillations.get(oscillations.size()-1)[1] = count+1;
									oscillations.get(oscillations.size()-1)[2] = seedHistory.size()-1 - j; // Crawler result --
									oscillStrings.add(new String[2]);
									oscillStrings.get(oscillStrings.size()-1)[0] = Integer.toString(x);
									oscillStrings.get(oscillStrings.size()-1)[1] = antecedent;
									// ------------------------------------------------------------------------------------
									oscillations.add(new int[3]); 
									oscillations.get(oscillations.size()-1)[0] = x+1;
									oscillations.get(oscillations.size()-1)[1] = count;
									oscillations.get(oscillations.size()-1)[2] = seedHistory.size()-1 - j; // Inferred result --
									oscillStrings.add(new String[2]);
									oscillStrings.get(oscillStrings.size()-1)[0] = Integer.toString(x+1);
									oscillStrings.get(oscillStrings.size()-1)[1] = antecedent;
									count=stepLimit;
								}
							}
						}
					}
				}
				if(stringBuckets.get(antecedent.length())==null)
					stringBuckets.put(antecedent.length(), new ArrayList<String>());
				stringBuckets.get(antecedent.length()).add(antecedent);
				seedHistory.add(antecedent);
				if(seedHistory.size()>500){ // limits history storage to cap ram use but will exclude instances of oscillations greater than size limit
					stringBuckets.get(seedHistory.get(0).length()).remove(seedHistory.get(0));
					seedHistory.remove(0);
				}
				if(count==stepLimit-1){
					System.out.println("String exhausted step limit");
					exhaustedStepLimit.add(initialString.length()-1);
					exhaustedStepLimit.add(initialString.length());
					exhaustedStepLimit.add(initialString.length()+1);
				}
				iterator.setInitString(antecedent);
				a = iterator.getInitString();
			}
			System.out.println(x);
			initialString = initialString + String.join("", Collections.nCopies(cores*3, one));
			iterator.setInitString(initialString);
		}
		while(!dataOffloaded){  // prevent simultaneous writes to host ArrayList by separate threads
			if(host.appendBusy()){ // manual alternative to synchronizedList
				try {
					Thread.sleep(1000*gen.nextInt(10));
				} catch (InterruptedException e) {
					System.out.println(e);
				}
			}
			else{
				host.appendResults(list, oscillations, oscillStrings, exhaustedStepLimit);
				dataOffloaded = true;
			}
		}
		host.incrementThreadClosed();
		System.out.println(threadName + " closed.");
	}
}

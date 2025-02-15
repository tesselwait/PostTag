import java.util.ArrayList;
import java.util.Collections;
import java.util.*;

public class PostTagDetailThread extends Thread{
	private MultiThreadCrawler host;
	private String threadName, initialString, one, antecedent;
	private int stepLimit, oscillHistoryCap, newSeed;
	private PostTag iterator;
	private ArrayList<int[]> list, oscillations;
	private ArrayList<String[]> oscillStrings;
	private String[] seedHistory;// string history as array saves O(n) remove(0) ArrayList calls.  Oscillations with period > oscillHistoryCap will run to step limit
	private ArrayList<Integer> exhaustedStepLimit;
	private HashSet<String> previousStrings;
	private boolean dataOffloaded;
	private Random gen;
	public PostTagDetailThread(String a, int z, int b, MultiThreadCrawler q) {
		threadName = a;
		stepLimit = z;
		host = q;
		initialString = "";
		one = "1";
		oscillHistoryCap = b;
		dataOffloaded = false;
		gen = new Random();
		iterator = new PostTag(initialString);
		list = new ArrayList<int[]>();
		oscillations = new ArrayList<int[]>();
		exhaustedStepLimit = new ArrayList<Integer>();
		oscillStrings = new ArrayList<String[]>();
	}

	public void runString(int q){
		int x = q;
		String a = String.join("", Collections.nCopies(x, one));
		seedHistory = new String[oscillHistoryCap];
		previousStrings = new HashSet<String>();
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
				list.get(list.size()-1)[0] = x+1;
				list.get(list.size()-1)[1] = count-1;
				count=stepLimit;
			}
			if(previousStrings.contains(antecedent)) {
				for(int j=0; j<oscillHistoryCap; j++) {
					if(seedHistory[(count-j)%oscillHistoryCap]!=null && antecedent.contentEquals(seedHistory[(count-j)%oscillHistoryCap])) {
						System.out.println("String reached an oscillation.");
						oscillations.add(new int[3]);
						oscillations.get(oscillations.size()-1)[0] = x-1;
						oscillations.get(oscillations.size()-1)[1] = count+2;
						oscillations.get(oscillations.size()-1)[2] = j; // Inferred result --
						oscillStrings.add(new String[2]);
						oscillStrings.get(oscillStrings.size()-1)[0] = Integer.toString(x-1);
						oscillStrings.get(oscillStrings.size()-1)[1] = antecedent;
						// ------------------------------------------------------------------------------------
						oscillations.add(new int[3]); // [seed string length, steps to a repeat, oscillation period]
						oscillations.get(oscillations.size()-1)[0] = x;
						oscillations.get(oscillations.size()-1)[1] = count+1;
						oscillations.get(oscillations.size()-1)[2] = j; // Crawler result --
						oscillStrings.add(new String[2]);
						oscillStrings.get(oscillStrings.size()-1)[0] = Integer.toString(x);
						oscillStrings.get(oscillStrings.size()-1)[1] = antecedent;
						// ------------------------------------------------------------------------------------
						oscillations.add(new int[3]); 
						oscillations.get(oscillations.size()-1)[0] = x+1;
						oscillations.get(oscillations.size()-1)[1] = count;
						oscillations.get(oscillations.size()-1)[2] = j; // Inferred result --
						oscillStrings.add(new String[2]);
						oscillStrings.get(oscillStrings.size()-1)[0] = Integer.toString(x+1);
						oscillStrings.get(oscillStrings.size()-1)[1] = antecedent;
						count=stepLimit;
						j=oscillHistoryCap;
					}
				}
			}
			previousStrings.add(antecedent);
			if(seedHistory[count%oscillHistoryCap]!= null) // limits history storage to cap ram use but will exclude instances of oscillations greater than size limit
				previousStrings.remove(seedHistory[count%oscillHistoryCap]);
			seedHistory[count%oscillHistoryCap]=antecedent;
			if(count==stepLimit-1){
				System.out.println("String exhausted step limit");
				exhaustedStepLimit.add(x-1);
				exhaustedStepLimit.add(x);
				exhaustedStepLimit.add(x+1);
			}
			iterator.setInitString(antecedent);
			a = iterator.getInitString();
		}
		System.out.println(x);
	}

	public void printOscillStrings(){
		for(String[] a: oscillStrings)
			System.out.println(""+a[0]+", "+a[1]);
	}

	public void printOscillations(){
		for(int[] a: oscillations)
			System.out.println(""+a[0]+", "+a[1]+", "+a[2]);
	}

	public void printPreviousStrings(){
		if(previousStrings!=null){
			Iterator itr = previousStrings.iterator();
			while(itr.hasNext())
				System.out.println(itr.next());
		}
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
		while(host.queueEmpty()==false){
			while(host.popBusy()){
				try{
					Thread.sleep(100);
				} catch (InterruptedException e){
					System.out.println(e);
				}
			}
			host.popClose();
			newSeed=host.popSeed();
			host.popOpen();
			runString(newSeed);
		}
		while(!dataOffloaded){  // prevent simultaneous writes to host ArrayList by separate threads
			if(host.appendBusy()){ // manual alternative to synchronizedList
				try {
					Thread.sleep(500*gen.nextInt(10));
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

import java.util.*;
import java.io.*;
public class MultiThreadCrawler{
	private ArrayList<int[]> results;
	private ArrayList<int[]> oscillationData;
	private ArrayList<String[]> oscillationStrings;
	private ArrayList<Integer> exhaustedStepLimit;
	private int threadsClosed;
	private boolean writeFlag;

	public MultiThreadCrawler(){
		results = new ArrayList<int[]>();
		oscillationData = new ArrayList<int[]>();
		oscillationStrings = new ArrayList<String[]>();
		exhaustedStepLimit = new ArrayList<Integer>();		
		threadsClosed=0;
	}
	public void appendResults(ArrayList<int[]> a, ArrayList<int[]> b, ArrayList<String[]> c, ArrayList<Integer> d){
		writeFlag = true;
		results.addAll(a);
		oscillationData.addAll(b);
		oscillationStrings.addAll(c);
		exhaustedStepLimit.addAll(d);
		writeFlag = false;
	}
	
	public boolean appendBusy(){
		return writeFlag;
	}

	public int getThreadsClosed(){
		return threadsClosed;
	}

	public void incrementThreadClosed(){
		threadsClosed++;
	}

	public ArrayList<int[]> getResults(){
		return results;
	}

	public ArrayList<int[]> getOscillationData(){
		return oscillationData;
	}

	public ArrayList<String[]> getOscillationStrings(){
		return oscillationStrings;
	}

	public ArrayList<Integer> getExhaustedStepLimit(){
		return exhaustedStepLimit;
	}
	
	public static void main(String[] args) {
		MultiThreadCrawler crawler = new MultiThreadCrawler();
		int cores = Runtime.getRuntime().availableProcessors(); // no oscillation limit & 16gb ram: 2 threads on 1,000,000 step limit.
		int threadSize = 210;
		int maxSteps = 4000000;
		// Starting with "1"*x seed strings.  Process will create sets of 3 equivalent strings immediately after first 0 digit at index 0 branch with (x: seed, y: steps) -> x + i, y - i for i = [0, 1, 2]
		// In seed string space this occurs on sets of 3 starting at (x+1) % 3 == 0.  This means given one result Q or R or S at (x+1)%3==[0, 1, 2] the two remaining results can be inferred given the first result.
		for(int i=0; i<cores; i++) { // given output occurs in sets of 3 - 1 step decrement pattern, possible to sample 1/3 seed values via subsitute (3*i) for (i) in 2nd parameter for 3x speed increase
			PostTagDetailThread object = new PostTagDetailThread("Thread"+i, (3*i)+3, threadSize, maxSteps, cores, crawler);
			object.start();
		}
		while(crawler.getThreadsClosed()<cores){
			try{
			Thread.sleep(60000);
			System.out.println("60 sec sleep.");
			}
			catch(Exception e){
				System.out.println(e);
			}
		}
		if(crawler.getThreadsClosed() == cores){
			crawler.getResults().sort(Comparator.comparingInt(a -> a[0]));
			System.out.println("Sort 1 complete.");
			crawler.getOscillationData().sort(Comparator.comparingInt(a -> a[0]));
			System.out.println("Sort 2 complete.");
			crawler.getOscillationStrings().sort(Comparator.comparingInt(a -> Integer.parseInt(a[0])));
			System.out.println("Sort 3 complete.");
			Collections.sort(crawler.getExhaustedStepLimit());
			System.out.println("Sort 4 complete.");
			try (PrintWriter output = new PrintWriter("PostTagOutput.txt")) {
				System.out.println("Results:");
				for(int[] b: crawler.getResults())
					output.println(b[0]+": "+b[1]);
				output.println();
				output.println();
				for(int[] b: crawler.getOscillationData())
					output.println(""+b[0]+", "+b[1]+", "+b[2]);
				output.println();
				output.println();
				for(String[] c: crawler.getOscillationStrings())
					output.println(c[0]+": "+c[1]);
				output.println();
				output.println();
				for(Integer d: crawler.getExhaustedStepLimit())
					output.println(d);
			}
			catch (FileNotFoundException e){
				System.out.println(e);
			}
		}
	}
}

import java.util.*;
import java.io.*;
public class MultiThreadCrawler{
	ArrayList<int[]> results;
	int threadsClosed;

	public MultiThreadCrawler(ArrayList<int[]> x){
		results = x;
		threadsClosed=0;
	}
	public void appendResults(ArrayList<int[]> a){
		for(int[] b: a)
			results.add(b);
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

	public static void main(String[] args) {
		MultiThreadCrawler crawler = new MultiThreadCrawler(new ArrayList<int[]>());
		int cores = Runtime.getRuntime().availableProcessors();
		int threadSize = 625;
		int maxSteps = 3000000;
		for(int i=0; i<cores; i++) {
			PostTagThread object = new PostTagThread("Thread"+i, i+3, threadSize, maxSteps, cores, crawler);
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
		crawler.getResults().sort(Comparator.comparingInt(a -> a[0]));
		try (PrintWriter output = new PrintWriter("PostTagOutput.txt")) {
			System.out.println("Results:");
			for(int[] b: crawler.getResults())
				output.println(b[0]+": "+b[1]);
		}
		catch (FileNotFoundException e){
			System.out.println(e);
		}
	}
}

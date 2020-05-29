public class MultiThreadCrawler {
	public static void main(String[] args) {
		int cores = Runtime.getRuntime().availableProcessors();
		int threadSize = 500; 
		int maxSteps = 1000000;
		for(int i=0; i<cores; i++) {
			PostTagThread object = new PostTagThread("Thread"+i, i+1, threadSize, maxSteps, cores);
			object.start();
		}
	}
}

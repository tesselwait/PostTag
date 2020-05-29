import java.util.ArrayList;
import java.util.Collections;

public class PostTagThread extends Thread{
	private String threadName, initialString, one;
	private int totalStrings, stepLimit, cores;
	private PostTag iterator;
	private ArrayList<int[]> list = new ArrayList<int[]>();
	public PostTagThread(String a, int x, int y, int z, int c) {
		threadName = a;
		totalStrings = y;
		stepLimit = z;
		cores = c;
		initialString = "";
		one = "1";
		for(int j=0; j<x; j++)
			initialString = initialString + "1";
		iterator = new PostTag(initialString);
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
			//System.out.println(iterator.getInitString());
			int count = 0;
			while(a!=""&&count<stepLimit) {
				String antecedent = iterator.iteratePostTag(iterator.getInitString());
				//System.out.println(antecedent+"   "+antecedent.length()+"   "+count+" "+x);
				if(iterator.getInitString().length()==1) {
					list.add(new int[2]);
					list.get(list.size()-1)[0] = x;
					list.get(list.size()-1)[1] = count;
				}
				iterator.setInitString(antecedent);
				a = iterator.getInitString();
				count++;
			}
			System.out.println(x);
			initialString = initialString +String.join("", Collections.nCopies(cores, one));
			iterator.setInitString(initialString);
		}
		System.out.println(threadName + " output:");
		for(int i = 0; i < list.size(); i++) {   
		    System.out.println(list.get(i)[0]+": "+list.get(i)[1]);
		}  
	}
}

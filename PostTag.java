public class PostTag {
	private String initString;
	public PostTag(String x) {
		initString = x;
	}
	
	public String iteratePostTag(String a) {
		if(a==null)
			System.exit(0);
		String c;
		if(a.charAt(0)=='1') {
			c = "1101";
		}
		else {
			if(a.charAt(0)=='0') {
				c = "00";
			}
			else
				c="";
		}
		String b = a+c;
		if(b.length()<=3)
			return "";
		return (b.substring(3));
	}
	
	public void setInitString(String b) {
		initString = b;
	}
	
	public String getInitString() {
		return initString;
	}
	
	public static void main(String[] args) {
		PostTag process = new PostTag("1111111111111111");
		String a = process.getInitString();
		System.out.println(process.getInitString());
		int count=0;
		while(a!=""&& count<2600) {
			String antecedent = process.iteratePostTag(process.getInitString());
			System.out.println(antecedent+"   "+antecedent.length()+"   "+count);
			process.setInitString(antecedent);
			a = process.getInitString();
			count++;
		}
	}
}

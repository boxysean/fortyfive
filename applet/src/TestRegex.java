import java.util.*;


public class TestRegex {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);
		
		while (in.hasNext()) {
			String s = in.next();
			
			System.out.println(s.matches("^\\s*-?\\s*[0-9]*\\s*/\\s*[0-9]+\\s*$"));
			
//			System.out.println(s.matches("^\\s*-?\\s*[0-9]*\\s/\\s*-?[0-9]+\\s*$"));

		}
	}

}

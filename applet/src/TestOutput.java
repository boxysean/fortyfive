
import java.io.*;


public class TestOutput {
	public static void main(String args[]) {
		try {
			PrintWriter out = new PrintWriter(new FileWriter("/tmp/tst"));
			out.println("het! " + System.currentTimeMillis());
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

package src;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;

public class CSVPrintStream extends PrintStream {

	public CSVPrintStream(OutputStream out) {
		super(out);
	}

	public void print(Object o) {
		super.print(o);
		super.print(",");
	}

	public void print(String s) {
		super.print(s);
		super.print(",");
	}

	public void print(int s) {
		super.print(s);
		super.print(",");
	}

	public void print(Object[] oa) {
		String temp = Arrays.toString(oa);
		String temp1 = temp.substring(1, temp.length() - 2);
		print(temp1);
	}

	public void pad() {
		super.print(",");
	}

	public void print(int[] oa) {
		String temp = Arrays.toString(oa);
		String temp1 = temp.substring(1, temp.length() - 2);
		print(temp1);
	}

	public void print(byte[] oa) {
		String temp = Arrays.toString(oa);
		String temp1 = temp.substring(1, temp.length() - 2);
		print(temp1);
	}

	public void println(byte[] oa) {
		String temp = Arrays.toString(oa);
		String temp1 = temp.substring(1, temp.length() - 2);
		println(temp1);
	}


}

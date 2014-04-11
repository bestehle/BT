package concurrent;

public class StaticClass {
	private static int counter = 0;
	public static Object obj;

	public static int getCounter() {
		return counter;
	}

	public static void incCounter() {
		counter++;
	}
}

package playground;

@SuppressWarnings("unused")
public class StateUnmodTestClass {
	private Object f;

	public StateUnmodTestClass() {
		f = new Object();
	}

	public Object publish() {

		Object object = f;

		f = object;

		return f;
	}
}
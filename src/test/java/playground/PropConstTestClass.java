package playground;

@SuppressWarnings("unused")
public class PropConstTestClass {
	private Object f;

	public PropConstTestClass() {
		f = null;

		Object object = f;

		f = object;
	}

	public Object publish() {

		Object object = f;

		f = object;

		return f;
	}
}
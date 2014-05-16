package concurrent;

public class StackConfinement {
	public static Object field;
	public static Object[] array;
	public Object f;

	public void method1() {
		Object object = new Object();

		StackConfinement.field = object;
		StackConfinement.array[0] = object;
	}

	public void method2() {
		Object object = new Object();

		Object[] array = new Object[1];

		array[0] = object;
	}

	public void method3() {
		Object object = new Object();

		StackConfinement.array[0] = object;
	}

	public void method4() {
		Object object = new Object();
		new StackConfinement().f = object;
	}

	public void method5() {
		AnotherClass anotherClass = new AnotherClass();
		StackConfinement.field = anotherClass;
	}

	public void method6() {
		StackConfinement.field = new AnotherClass();
	}

}

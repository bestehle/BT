package de.seerhein_lab.jic.analyzer;

import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.runner.RunWith;

import de.seerhein_lab.jic.Utils;
import de.seerhein_lab.jic.testutils.BugsExpected;
import de.seerhein_lab.jic.testutils.ClassAnalyzerRunner;
import de.seerhein_lab.jic.testutils.ClassAnalyzerRunner.BindAnalyzerMethod;
import de.seerhein_lab.jic.testutils.NoBugsExpected;
import edu.umd.cs.findbugs.BugInstance;

/**
 * Functional acceptance tests for the method properlyConstructed of the class
 * ClassAnalyzer.
 * 
 * TODO: JavaDoc
 * 
 * @see IsImmutableTestRunner
 */
// @Ignore("activate this test class when the method IsImmutable will be implemented.")
@RunWith(ClassAnalyzerRunner.class)
@SuppressWarnings("unused")
public class StackConfinementAcceptanceTest {
	private static Logger logger;

	static {
		try {
			logger = Utils.setUpLogger("StackConfinementAcceptanceTest", "log.txt", Level.ALL);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@BindAnalyzerMethod
	public static Collection<BugInstance> bindIsStackConfinement(ClassAnalyzer analyzer) {
		return analyzer.isStackConfined("de.seerhein_lab.jic.analyzer");
	}

	@BugsExpected
	public static class Story001_StaticField {
		public void assign() {
			TestClassStatic.klass = new TestClass();
		}
	}

	@NoBugsExpected
	public static class Story002_Local {
		public void assign() {
			Object local = new TestClass();
		}
	}

	@BugsExpected
	public static class Story003_Parameter {
		public void assign(TestClass obj) {
			obj.klass = new TestClass();
		}
	}

	@NoBugsExpected
	public static class Story004_ParameterToStatic {
		public void assign(TestClass obj) {
			TestClassStatic.klass = obj;
		}
	}

	@BugsExpected
	public static class Story005_LocalArray {
		public void assign() {
			Object[] array = new Object[5];
			array[0] = new TestClass();
		}
	}

	@NoBugsExpected
	public static class Story006_Array {
		private TestClass[] array;

		public void assign() {
			array = new TestClass[5];
		}
	}

	@BugsExpected
	public static class Story007_Array {
		private Object[] array;

		public void assign() {
			array = new Object[5];
			array[0] = new TestClass();
		}
	}

	@BugsExpected
	public static class Story008_ArrayUnintialiazed {
		private Object[] array;

		public void assign() {
			array[0] = new TestClass();
		}
	}

	@NoBugsExpected
	public static class Story009_NullPutfield {
		private Object obj;

		public void assign() {
			obj = null;
		}
	}

	@BugsExpected
	public static class Story010_InvokeMethod {

		public void assign() {
			Object o = new TestClass();
			TestClassStatic.assignToStaticField(o);
		}
	}

	@BugsExpected
	public static class Story010b_InvokeMethod {

		public void assign() {
			TestClass o = new TestClass();
			TestClassStatic.assignToStaticField(o);
		}
	}

	@NoBugsExpected
	public static class Story011_ReturnInstance {

		public Object assign() {
			return new TestClass();
		}
	}

	@NoBugsExpected
	public static class Story012_StoreParameter {
		private Object field;

		public void assign(TestClass obj) {
			field = obj;
		}
	}

	@BugsExpected
	public static class Story013_StoreToField {
		private Object field;

		public void assign(Object obj) {
			field = obj;
			field = new TestClass();
		}
	}

	@BugsExpected
	public static class Story014_StoreRecursive {
		private TestClass field;

		public void assign() {
			field = new TestClass();
			field.klass = new Object();
		}
	}

	@BugsExpected
	public static class Story015_StoreRecursive {
		private TestClass field;

		public void assign() {
			TestClass testClass = new TestClass();
			field = testClass;
			testClass.klass = new TestClass();
		}
	}

	@BugsExpected
	public static class Story016_StoreRecursive {
		private TestClass field;

		public void assign() {
			TestClass testClass = new TestClass();
			testClass.klass = new TestClass();
		}
	}

	@BugsExpected
	public static class Story017_StoreFromReturn {
		private TestClass field;

		public void assign() {
			field = TestClass.getInstance();
		}
	}

	@BugsExpected
	public static class Story018_StoreFromReturn2 {
		private TestClass field;

		public void assign() {
			field = TestClass.getInstance2();
		}
	}

	@BugsExpected
	public static class Story019_StoreAndReturn {
		private TestClass field;

		public TestClass assign() {
			field = new TestClass();
			return field;
		}
	}

	private static class TestClass {
		public Object[] array;
		public Object klass;
		public TestClass tc;
		public int i;

		public static TestClass getInstance() {
			return new TestClass();
		}

		public static TestClass getInstance2() {
			return getInstance();
		}
	}

	private static class TestClassStatic {
		public static Object[] array;
		public static Object klass;
		public static TestClass tc;
		public static int i;

		public static void assignToStaticField(Object obj) {
			klass = obj;
		}
	}
}

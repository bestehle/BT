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
		return analyzer.isStackConfined();
	}

	@BugsExpected
	public static class Story01_StaticField {
		public void assigne() {
			TestClassStatic.klass = new TestClass();
		}
	}

	@NoBugsExpected
	public static class Story02_Local {
		public void assigne() {
			Object local = new TestClass();
		}
	}

	@BugsExpected
	public static class Story03_Parameter {
		public void assigne(TestClass obj) {
			obj.klass = new TestClass();
		}
	}

	@BugsExpected
	public static class Story04_ParameterToStatic {
		public void assigne(TestClass obj) {
			TestClassStatic.klass = obj;
		}
	}

	@NoBugsExpected
	public static class Story05_LocalArray {
		public void assigne() {
			Object[] array = new Object[5];
			array[0] = new TestClass();
		}
	}

	@BugsExpected
	public static class Story06_Array {
		private Object[] array;

		public void assigne() {
			array = new Object[5];
		}
	}

	@BugsExpected
	public static class Story07_Array {
		private Object[] array;

		public void assigne() {
			array = new Object[5];
			array[0] = new TestClass();
		}
	}

	@BugsExpected
	public static class Story08_ArrayUnintialiazed {
		private Object[] array;

		public void assigne() {
			array[0] = new TestClass();
		}
	}

	@NoBugsExpected
	public static class Story09_NullPutfield {
		private Object obj;

		public void assigne() {
			obj = null;
		}
	}

	private static class TestClass {
		public Object[] array;
		public Object klass;
		public TestClass tc;
		public int i;
	}

	private static class TestClassStatic {
		public static Object[] array;
		public static Object klass;
		public static TestClass tc;
		public static int i;
	}
}

package playground;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.bcel.Constants;
import org.apache.bcel.Repository;
import org.apache.bcel.classfile.Attribute;
import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.InnerClass;
import org.apache.bcel.classfile.InnerClasses;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Utility;

import de.seerhein_lab.jic.AnalysisResult;
import de.seerhein_lab.jic.ClassRepository;
import de.seerhein_lab.jic.EvaluationResult;
import de.seerhein_lab.jic.Utils;
import de.seerhein_lab.jic.vm.HeapObject;
import edu.umd.cs.findbugs.SortedBugCollection;

public class ConfinementTestDriver {
	private static final String LOGFILEPATH = "log.txt";
	private static Logger logger;
	private static Set<EvaluationResult> results = new HashSet<EvaluationResult>();

	public static void main(String[] args) throws ClassNotFoundException, SecurityException,
			IOException {
		logger = Utils.setUpLogger("ConfinementTestDriver", LOGFILEPATH, Level.ALL);

		String class_name = "de.seerhein_lab.jic.ClassRepository";
		String classToAnalyze = "de.seerhein_lab.jic.analyzer.StackConfinementAcceptanceTest$TestClass";

		Set<JavaClass> classes = getClassWithInnerClasses(class_name);

		// Collection<JavaClass> classes = getClasses("de.seerhein_lab.jic");

		Set<AnalysisResult> analysisResults = analyze(classToAnalyze, classes);

		SortedBugCollection bugs = new SortedBugCollection();
		for (AnalysisResult analysisResult : analysisResults) {
			bugs.addAll(analysisResult.getBugs());
			results.addAll(analysisResult.getResults());
		}

		for (EvaluationResult result : results) {
			for (HeapObject object : result.getHeap().getObjects()) {

			}
			; // TODO
		}

		// logger.log(Level.SEVERE, "bugs: ");
		// for (BugInstance bug : bugs) {
		// logger.log(Level.SEVERE, " " + bug.getType() + " (" +
		// bug.getPriorityString() + ")");
		// }
		//
		// logger.log(Level.SEVERE, "end bugs");
	}

	public static Set<AnalysisResult> analyze(String classToCheck,
			Collection<JavaClass> classesToAnalyze) {
		ClassRepository repository = new ClassRepository();

		repository.analyzeClasses(classesToAnalyze);

		return repository.analyzeMethods(repository.getClass(classToCheck));
	}

	private static Set<JavaClass> getClassWithInnerClasses(String className) {
		Set<JavaClass> classes = new HashSet<JavaClass>();

		JavaClass clazz = null;
		try {
			clazz = Repository.lookupClass(className);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		classes.add(clazz);

		InnerClass[] innerClasses = new InnerClass[0];
		for (Attribute attribute : clazz.getAttributes()) {
			if (attribute instanceof InnerClasses) {
				innerClasses = ((InnerClasses) attribute).getInnerClasses();
			}
		}

		for (InnerClass innerClass : innerClasses) {
			try {
				classes.add(Repository.lookupClass(Utility.compactClassName(clazz.getConstantPool()
						.getConstantString(innerClass.getInnerClassIndex(),
								Constants.CONSTANT_Class))));
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (ClassFormatException e) {
				e.printStackTrace();
			}
		}

		return classes;
	}

	/**
	 * Scans all classes accessible from the context class loader which belong
	 * to the given package and subpackages.
	 * 
	 * @param packageName
	 *            The base package
	 * @return The classes
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	private static Collection<JavaClass> getClasses(String packageName)
			throws ClassNotFoundException, IOException {

		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

		String path = packageName.replace('.', '/');

		Enumeration<URL> resources = classLoader.getResources(path);

		List<File> dirs = new ArrayList<File>();

		while (resources.hasMoreElements())
			dirs.add(new File(resources.nextElement().getFile()));

		ArrayList<JavaClass> classes = new ArrayList<JavaClass>();
		for (File directory : dirs) {
			classes.addAll(findClasses(directory, packageName));
		}
		return classes;
	}

	/**
	 * Recursive method used to find all classes in a given directory and
	 * subdirs.
	 * 
	 * @param directory
	 *            The base directory
	 * @param packageName
	 *            The package name for classes found inside the base directory
	 * @return The classes
	 * @throws ClassNotFoundException
	 */
	private static List<JavaClass> findClasses(File directory, String packageName)
			throws ClassNotFoundException {
		List<JavaClass> classes = new ArrayList<JavaClass>();

		if (!directory.exists())
			return classes;

		File[] files = directory.listFiles();
		for (File file : files) {
			String fileName = file.getName();
			if (file.isDirectory())
				classes.addAll(findClasses(file, packageName + "." + fileName));
			else if (fileName.endsWith(".class"))
				classes.add(Repository.lookupClass(packageName + '.'
						+ fileName.substring(0, fileName.length() - 6)));

		}
		return classes;
	}

}

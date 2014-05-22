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
import org.apache.bcel.classfile.InnerClass;
import org.apache.bcel.classfile.InnerClasses;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Utility;

import de.seerhein_lab.jic.AnalysisResult;
import de.seerhein_lab.jic.ClassRepository;
import de.seerhein_lab.jic.DetailedClass;
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
		Set<JavaClass> classes = new HashSet<JavaClass>();

		JavaClass clazz = Repository
				.lookupClass("de.seerhein_lab.jic.analyzer.StackConfinementAcceptanceTest");

		classes.add(clazz);

		for (InnerClass innerClass : getInnerClasses(clazz)) {
			classes.add(Repository.lookupClass(Utility.compactClassName(clazz.getConstantPool()
					.getConstantString(innerClass.getInnerClassIndex(), Constants.CONSTANT_Class))));
		}

		Collection<JavaClass> classes2 = getClasses("de.seerhein_lab.jic.slot");

		for (Package p : Package.getPackages()) {
			logger.severe(p.getName());
			// TODO
		}

		SortedBugCollection bugs = new SortedBugCollection();

		ClassRepository repository = new ClassRepository();

		repository.generateCallGraph(classes);

		repository.printCallGraph();

		DetailedClass classToAnalyze = repository.getClass("de.seerhein_lab.jic.analyzer."
				+ "StackConfinementAcceptanceTest$TestClass");

		Set<AnalysisResult> analysisResults = repository.analyzeMethods(classToAnalyze);

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

	private static InnerClass[] getInnerClasses(JavaClass clazz) {
		for (Attribute attribute : clazz.getAttributes()) {
			if (attribute instanceof InnerClasses)
				return ((InnerClasses) attribute).getInnerClasses();
		}
		return new InnerClass[0];
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
		assert classLoader != null;
		String path = packageName.replace('.', '/');
		Enumeration<URL> resources = classLoader.getResources(path);
		List<File> dirs = new ArrayList<File>();
		while (resources.hasMoreElements()) {
			URL resource = resources.nextElement();
			dirs.add(new File(resource.getFile()));
		}
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
		if (!directory.exists()) {
			return classes;
		}
		File[] files = directory.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				assert !file.getName().contains(".");
				classes.addAll(findClasses(file, packageName + "." + file.getName()));
			} else if (file.getName().endsWith(".class")) {
				classes.add(Repository.lookupClass(packageName + '.'
						+ file.getName().substring(0, file.getName().length() - 6)));
			}
		}
		return classes;
	}

}

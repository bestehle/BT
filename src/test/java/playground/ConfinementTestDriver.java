package playground;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.bcel.classfile.JavaClass;

import de.seerhein_lab.jic.AnalysisResult;
import de.seerhein_lab.jic.ClassRepository;
import de.seerhein_lab.jic.EmercencyBrakeException;
import de.seerhein_lab.jic.Utils;
import de.seerhein_lab.jic.vm.HeapObject;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.MethodAnnotation;
import edu.umd.cs.findbugs.StringAnnotation;

public class ConfinementTestDriver {
	private static final String LOGFILEPATH = "log.txt";
	private static Logger logger;
	// private static Set<EvaluationResult> results = new
	// HashSet<EvaluationResult>();
	private static Set<String> stackConfinedClasses = new TreeSet<String>();
	private static Set<String> notStackConfinedClasses = new TreeSet<String>();
	private static Set<String> notAnalyzedClasses = new TreeSet<String>();
	private static Set<String> notInstantiatedClasses = new TreeSet<String>();

	public static void main(String[] args) throws ClassNotFoundException, SecurityException,
			IOException {
		logger = Utils.setUpLogger("ConfinementTestDriver", LOGFILEPATH, Level.SEVERE);

		// String class_name =
		// "de.seerhein_lab.jic.analyzer.StackConfinementAcceptanceTest";
		// String classToAnalyze = "de.seerhein_lab.jic.AnalysisResult";

		// Set<JavaClass> classes =
		// ClassRepository.getClassWithInnerClasses(class_name);

		// String classToAnalyze =
		// "de.seerhein_lab.jic.analyzer.StackConfinementAcceptanceTest$TestClass";
		Collection<JavaClass> classes = ClassRepository.getClasses("de.seerhein_lab.jic");
		// Collection<JavaClass> classes =
		// ClassRepository.getClasses("org.apache");

		// Collection<JavaClass> classes = ClassRepository.getClasses(args[1]);

		// Set<AnalysisResult> analysisResults = analyze(classToAnalyze,
		// classes);
		// logResults(classToAnalyze, analysisResults);

		analyzeAllClasses(classes);

	}

	public static Set<AnalysisResult> analyze(String classToCheck,
			Collection<JavaClass> classesToAnalyze) {
		ClassRepository repository = new ClassRepository();

		repository.analyzeClasses(classesToAnalyze);

		return repository.analyzeMethods(repository.getClass(classToCheck));
	}

	public static void analyzeAllClasses(Collection<JavaClass> classesToAnalyze) {
		Date start = new Date();
		logger.severe(start.toString());

		ClassRepository repository = new ClassRepository();

		repository.analyzeClasses(classesToAnalyze);

		for (JavaClass javaClass : classesToAnalyze) {
			try {
				HeapObject.resetCounter();
				logResults(javaClass.getClassName(),
						repository.analyzeMethods(repository.getClass(javaClass.getClassName())));
			} catch (EmercencyBrakeException e) {
				notAnalyzedClasses.add(javaClass.getClassName());

			} // catch (Exception e) {
			// notAnalyzedClasses.add(javaClass.getClassName());
			// logger.severe("FEHLER : " + e);
			// }
		}

		logSummary("Stack Confined Classes", stackConfinedClasses);
		logSummary("NOT Stack Confined Classes", notStackConfinedClasses);
		logSummary("NOT Instantiated Classes", notInstantiatedClasses);
		logSummary("Not analyzed Classes: Class to Complex", notAnalyzedClasses);

		Date end = new Date();
		logger.severe(end.toString() + "\t[ " + (end.getTime() - start.getTime()) / 1000 + " s]");
	}

	private static void logResults(String classToAnalyze, Set<AnalysisResult> analysisResults) {
		if (analysisResults.isEmpty()) {
			notInstantiatedClasses.add(classToAnalyze);
			return;
		}

		boolean stackConfined = true;
		logger.severe(String
				.format("\n###############################################################################\n"
						+ "#    %-65s        #"
						+ "\n#-----------------------------------------------------------------------------#\n#",
						classToAnalyze));
		// logger.severe("Stack Confinement of " + classToAnalyze);
		for (AnalysisResult analysisResult : analysisResults) {
			if (analysisResult.getBugs().isEmpty()) {
				logger.severe("#\t Stack Confined\t\t : " + analysisResult.getAnalyzedMethod());
			} else {
				for (BugInstance bugInstance : analysisResult.getBugs()) {
					MethodAnnotation method = bugInstance.getAnnotationWithRole(
							MethodAnnotation.class, "METHOD_DEFAULT");
					logger.severe("#\t NOT Stack Confined\t : " + method);
					logger.severe("#\t\t (Line "
							+ method.getSourceLines().getStartLine()
							+ ") -> "
							+ bugInstance.getAnnotationWithRole(StringAnnotation.class,
									"STRING_DEFAULT"));
				}
				stackConfined = false;
			}
		}
		logger.severe(String
				.format("#\n#-----------------------------------------------------------------------------#\n"
						+ "#    %-70s   #"
						+ "\n###############################################################################\n",
						classToAnalyze
								+ (stackConfined ? ":   Stack Confined" : ":   NOT Stack Confined")));

		if (stackConfined)
			stackConfinedClasses.add(classToAnalyze);
		else
			notStackConfinedClasses.add(classToAnalyze);

	}

	private static void logSummary(String list, Set<String> classes) {
		logger.severe(String
				.format("\n###############################################################################\n"
						+ "#    %2d   %-60s        #"
						+ "\n#-----------------------------------------------------------------------------#",
						classes.size(), list));
		for (String clazz : classes) {
			logger.severe(String.format("#    %-65s        #", clazz));
		}
		logger.severe("###############################################################################\n");
	}
}

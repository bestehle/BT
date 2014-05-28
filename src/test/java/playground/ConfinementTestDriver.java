package playground;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.bcel.classfile.JavaClass;

import de.seerhein_lab.jic.AnalysisResult;
import de.seerhein_lab.jic.ClassRepository;
import de.seerhein_lab.jic.EmercencyBrakeException;
import de.seerhein_lab.jic.EvaluationResult;
import de.seerhein_lab.jic.Utils;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.MethodAnnotation;
import edu.umd.cs.findbugs.StringAnnotation;

public class ConfinementTestDriver {
	private static final String LOGFILEPATH = "log.txt";
	private static Logger logger;
	private static Set<EvaluationResult> results = new HashSet<EvaluationResult>();

	public static void main(String[] args) throws ClassNotFoundException, SecurityException,
			IOException {
		logger = Utils.setUpLogger("ConfinementTestDriver", LOGFILEPATH, Level.ALL);

		// String class_name =
		// "de.seerhein_lab.jic.analyzer.StackConfinementAcceptanceTest";
		// String classToAnalyze =
		// "de.seerhein_lab.jic.analyzer.StackConfinementAcceptanceTest$TestClass";

		// Set<JavaClass> classes =
		// ClassRepository.getClassWithInnerClasses(class_name);

		String classToAnalyze = "de.seerhein_lab.jic.analyzer.StackConfinementAcceptanceTest$TestClass";
		Collection<JavaClass> classes = ClassRepository.getClasses("de.seerhein_lab.jic");

		// Set<AnalysisResult> analysisResults = analyze(classToAnalyze,
		// classes);

		analyzeAllClasses(classes);

		// logResults(classToAnalyze, analysisResults);

	}

	private static void logResults(String classToAnalyze, Set<AnalysisResult> analysisResults) {
		logger.severe("Stack Confinement of " + classToAnalyze);
		for (AnalysisResult analysisResult : analysisResults) {
			if (analysisResult.getBugs().isEmpty())
				logger.severe("\t Stack Confined\t\t : " + analysisResult.getAnalyzedMethod());
			else
				for (BugInstance bugInstance : analysisResult.getBugs()) {
					MethodAnnotation method = bugInstance.getAnnotationWithRole(
							MethodAnnotation.class, "METHOD_DEFAULT");
					logger.severe("\t NOT Stack Confined\t : " + method);
					logger.severe("\t\t (Line "
							+ method.getSourceLines().getStartLine()
							+ ") -> "
							+ bugInstance.getAnnotationWithRole(StringAnnotation.class,
									"STRING_DEFAULT"));
				}
		}
	}

	public static Set<AnalysisResult> analyze(String classToCheck,
			Collection<JavaClass> classesToAnalyze) {
		ClassRepository repository = new ClassRepository();

		repository.analyzeClasses(classesToAnalyze);

		return ClassRepository.analyzeMethods(repository.getClass(classToCheck));
	}

	public static void analyzeAllClasses(Collection<JavaClass> classesToAnalyze) {
		ClassRepository repository = new ClassRepository();

		repository.analyzeClasses(classesToAnalyze);

		for (JavaClass javaClass : classesToAnalyze) {
			try {
				logResults(javaClass.getClassName(), ClassRepository.analyzeMethods(repository
						.getClass(javaClass.getClassName())));
			} catch (EmercencyBrakeException e) {
				logger.severe(e.toString());
			}
		}
	}
}

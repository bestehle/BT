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

		String class_name = "de.seerhein_lab.jic.analyzer.StackConfinementAcceptanceTest";
		String classToAnalyze = "de.seerhein_lab.jic.analyzer.StackConfinementAcceptanceTest$TestClass";
		// String classToAnalyze = "de.seerhein_lab.jic.vm.Heap";

		Set<JavaClass> classes = ClassRepository.getClassWithInnerClasses(class_name);

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

}

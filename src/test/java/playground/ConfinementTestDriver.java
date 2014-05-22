package playground;

import java.io.IOException;
import java.util.HashSet;
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
import de.seerhein_lab.jic.CallGraph;
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

		// for (Package p : Package.getPackages()) {
		// logger.severe(p.getName());
		// // TODO
		// }

		SortedBugCollection bugs = new SortedBugCollection();

		CallGraph.generateCallGraph(classes);

		CallGraph.printCallGraph();

		DetailedClass classToAnalyze = DetailedClass.getClass("de.seerhein_lab.jic.analyzer."
				+ "StackConfinementAcceptanceTest$TestClass");

		Set<AnalysisResult> analysisResults = CallGraph.analyzeMethods(classToAnalyze);

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
}

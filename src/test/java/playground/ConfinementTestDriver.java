package playground;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.MethodGen;

import de.seerhein_lab.jic.AnalysisResult;
import de.seerhein_lab.jic.CallGraphHelper;
import de.seerhein_lab.jic.Class;
import de.seerhein_lab.jic.EvaluationResult;
import de.seerhein_lab.jic.Utils;
import de.seerhein_lab.jic.analyzer.BaseMethodAnalyzer;
import de.seerhein_lab.jic.analyzer.QualifiedMethod;
import de.seerhein_lab.jic.analyzer.confinement.ConfinementAnalyzer;
import de.seerhein_lab.jic.cache.AnalysisCache;
import de.seerhein_lab.jic.vm.HeapObject;
import edu.umd.cs.findbugs.SortedBugCollection;
import edu.umd.cs.findbugs.ba.ClassContext;

public class ConfinementTestDriver {
	private static final String LOGFILEPATH = "log.txt";
	private static Logger logger;
	private static Set<EvaluationResult> results;

	public static void main(String[] args) throws ClassNotFoundException, SecurityException,
			IOException {

		logger = Utils.setUpLogger("ConfinementTestDriver", LOGFILEPATH, Level.ALL);

		JavaClass clazz = Repository.lookupClass("concurrent.StackConfinement");

		for (Package p : Package.getPackages()) {
			logger.severe(p.getName());
			// TODO
		}

		SortedBugCollection bugs = new SortedBugCollection();

		CallGraphHelper.generateCallGraph(clazz);

		CallGraphHelper.printCallGraph();

		Class classToAnalyze = Class.getClass("concurrent.AnotherClass");

		AnalysisCache analysisCache = new AnalysisCache();

		for (QualifiedMethod method : classToAnalyze.getInstantiations()) {
			ClassContext classContextMock = mock(ClassContext.class);
			when(classContextMock.getJavaClass()).thenReturn(method.getJavaClass());

			MethodGen methodGen = new MethodGen(method.getMethod(), method.getJavaClass()
					.getClassName(), new ConstantPoolGen(method.getJavaClass().getConstantPool()));

			BaseMethodAnalyzer methodAnalyzer = new ConfinementAnalyzer(classContextMock,
					methodGen, analysisCache, 0, classToAnalyze);

			AnalysisResult result = methodAnalyzer.analyze();
			bugs.addAll(result.getBugs());
			results = result.getResults();
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
}

package playground;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.JavaClass;

import de.seerhein_lab.jic.CallGraphHelper;
import de.seerhein_lab.jic.Utils;
import edu.umd.cs.findbugs.SortedBugCollection;
import edu.umd.cs.findbugs.ba.ClassContext;

public class ConfinementTestDriver {
	private static final String LOGFILEPATH = "log.txt";
	private static Logger logger;

	public static void main(String[] args) throws ClassNotFoundException, SecurityException,
			IOException {

		logger = Utils.setUpLogger("ConfinementTestDriver", LOGFILEPATH, Level.ALL);

		JavaClass clazz = Repository.lookupClass("de.seerhein_lab.jic.analyzer.BaseVisitor");

		ClassContext classContextMock = mock(ClassContext.class);

		when(classContextMock.getJavaClass()).thenReturn(clazz);

		SortedBugCollection bugs = new SortedBugCollection();
		// bugs.addAll(new ClassAnalyzer(classContextMock, new
		// AnalysisCache()).isStackConfined());

		CallGraphHelper.generateCallGraph(clazz);

		CallGraphHelper.printCallGraph();

		// logger.log(Level.SEVERE, "bugs: ");
		// for (BugInstance bug : bugs) {
		// logger.log(Level.SEVERE, " " + bug.getType() + " (" +
		// bug.getPriorityString() + ")");
		// }
		//
		// logger.log(Level.SEVERE, "end bugs");
	}
}

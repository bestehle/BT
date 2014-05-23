package de.seerhein_lab.jic;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.NEW;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.Type;

import de.seerhein_lab.jic.analyzer.BaseMethodAnalyzer;
import de.seerhein_lab.jic.analyzer.QualifiedMethod;
import de.seerhein_lab.jic.analyzer.confinement.StackConfinementAnalyzer;
import de.seerhein_lab.jic.cache.AnalysisCache;
import edu.umd.cs.findbugs.ba.ClassContext;

public class ClassRepository {
	private static final Logger logger = Logger.getLogger("CallGraphHelper");
	private final Map<String, DetailedClass> classes = new HashMap<String, DetailedClass>();

	public void analyzeClasses(Set<JavaClass> classes) {
		for (JavaClass javaClass : classes) {
			analyzeClass(javaClass);
		}
	}

	public Collection<DetailedClass> getClasses() {
		return classes.values();
	}

	public DetailedClass getClass(String name) {
		if (!classes.containsKey(name)) {
			DetailedClass newClass = null;
			try {
				newClass = new DetailedClass(Repository.lookupClass(name), this);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			classes.put(name, newClass);
			return newClass;
		}
		return classes.get(name);
	}

	public DetailedClass getClass(JavaClass clazz) {
		if (!classes.containsKey(clazz.getClassName()))
			classes.put(clazz.getClassName(), new DetailedClass(clazz, this));
		return classes.get(clazz.getClassName());
	}

	public void analyzeClass(JavaClass jClazz) {
		DetailedClass clazz = getClass(jClazz);

		for (Method method : jClazz.getMethods()) {

			logger.severe("<---  " + method.getName() + method.getSignature() + " --->");

			MethodGen methodGen = new MethodGen(method, jClazz.getClassName(), new ConstantPoolGen(
					jClazz.getConstantPool()));

			ConstantPoolGen constantPool = methodGen.getConstantPool();

			if (methodGen.getMethod().getCode() == null)
				continue;

			InstructionHandle[] instructions = new InstructionList(methodGen.getMethod().getCode()
					.getCode()).getInstructionHandles();

			for (InstructionHandle ih : instructions) {
				if (ih.getInstruction() instanceof NEW) {
					NEW newInstruction = (NEW) ih.getInstruction();
					getClass(newInstruction.getLoadClassType(constantPool).getClassName())
							.addInstantiation(clazz.getMethod(method));

					logger.severe("new: " + newInstruction.getLoadClassType(constantPool));
				} else if (ih.getInstruction() instanceof InvokeInstruction) {
					InvokeInstruction invokeInstruction = (InvokeInstruction) ih.getInstruction();
					getClass(invokeInstruction.getLoadClassType(constantPool).getClassName())
							.getMethod(invokeInstruction.getMethodName(constantPool))
							.addCallingMethod(clazz.getMethod(method));

					logger.severe(ih.getInstruction().getName() + "   --    "
							+ invokeInstruction.getLoadClassType(constantPool) + "."
							+ invokeInstruction.getMethodName(constantPool));
				}
			}
		}
	}

	public void printCallGraph() {
		logger.severe("Ergebnis:\n");

		for (DetailedClass clazz : getClasses()) {
			logger.severe(clazz.getName());
			logger.severe("\tInstanzierungen: " + clazz.getInstantiations());
			for (QualifiedMethod method : clazz.getMethods().values()) {
				logger.severe("\t" + method + ": " + method.getCallingMethods());
			}
		}
	}

	public Set<AnalysisResult> analyzeMethods(DetailedClass classToAnalyze) {
		AnalysisCache analysisCache = new AnalysisCache();
		HashSet<AnalysisResult> results = new HashSet<AnalysisResult>();

		Queue<QualifiedMethod> queue = new LinkedList<QualifiedMethod>(
				classToAnalyze.getInstantiations());

		while (!queue.isEmpty()) {
			QualifiedMethod method = queue.remove();

			Type returnType = method.getMethod().getReturnType();
			if (returnType instanceof ObjectType
					&& ((ObjectType) returnType).getClassName().equals(classToAnalyze.getName())) {
				queue.addAll(method.getCallingMethods());
				continue;
			}

			ClassContext classContextMock = mock(ClassContext.class);
			when(classContextMock.getJavaClass()).thenReturn(method.getJavaClass());

			MethodGen methodGen = new MethodGen(method.getMethod(), method.getJavaClass()
					.getClassName(), new ConstantPoolGen(method.getJavaClass().getConstantPool()));

			BaseMethodAnalyzer methodAnalyzer = new StackConfinementAnalyzer(classContextMock,
					methodGen, analysisCache, 0, classToAnalyze);

			results.add(methodAnalyzer.analyze());
		}
		return results;
	}
}
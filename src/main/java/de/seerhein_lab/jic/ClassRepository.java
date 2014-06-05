package de.seerhein_lab.jic;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.bcel.Constants;
import org.apache.bcel.Repository;
import org.apache.bcel.classfile.Attribute;
import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.InnerClass;
import org.apache.bcel.classfile.InnerClasses;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.classfile.Utility;
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
	private static final Logger logger = Logger.getLogger("ClassRepository");
	private final Map<String, DetailedClass> classes = new HashMap<String, DetailedClass>();
	private final Set<String> analyzedClasses = new HashSet<String>();

	public Collection<DetailedClass> getClasses() {
		return classes.values();
	}

	public DetailedClass getClass(String name) {
		try {
			return getClass(Repository.lookupClass(name));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	public DetailedClass getClass(JavaClass clazz) {
		if (!classes.containsKey(clazz.getClassName())) {
			classes.put(clazz.getClassName(), new DetailedClass(clazz, this));
		}
		return classes.get(clazz.getClassName());
	}

	public void analyzeClasses(Collection<JavaClass> classes) {
		for (JavaClass javaClass : classes) {
			analyzeClass(javaClass);
		}
	}

	public void analyzeClass(JavaClass jClazz) {
		if (analyzedClasses.contains(jClazz.getClassName()))
			return;

		analyzedClasses.add(jClazz.getClassName());
		DetailedClass clazz = getClass(jClazz);

		for (Method method : jClazz.getMethods()) {

			MethodGen methodGen = new MethodGen(method, jClazz.getClassName(), new ConstantPoolGen(
					jClazz.getConstantPool()));

			ConstantPoolGen constantPool = methodGen.getConstantPool();

			if (methodGen.getMethod().getCode() == null)
				continue;

			InstructionHandle[] instructions = new InstructionList(methodGen.getMethod().getCode()
					.getCode()).getInstructionHandles();

			for (InstructionHandle ih : instructions) {
				if (ih.getInstruction() instanceof NEW) {
					NEW instruction = (NEW) ih.getInstruction();
					getClass(instruction.getLoadClassType(constantPool).getClassName())
							.addInstantiation(clazz.getMethod(method));

					// logger.severe("new: " +
					// instruction.getLoadClassType(constantPool));
				} else if (ih.getInstruction() instanceof InvokeInstruction) {
					InvokeInstruction instruction = (InvokeInstruction) ih.getInstruction();
					getClass(instruction.getLoadClassType(constantPool).getClassName()).getMethod(
							instruction.getMethodName(constantPool)).addCallingMethod(
							clazz.getMethod(method));

					// logger.severe(ih.getInstruction().getName() + "   --    "
					// + instruction.getLoadClassType(constantPool) + "."
					// + instruction.getMethodName(constantPool));
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

	public static Set<JavaClass> getClassWithInnerClasses(String className) {
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
	public static Collection<JavaClass> getClasses(String packageName) {

		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

		String path = packageName.replace('.', '/');

		Enumeration<URL> resources = null;
		try {
			resources = classLoader.getResources(path);
		} catch (IOException e) {
			e.printStackTrace();
		}

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
	private static List<JavaClass> findClasses(File directory, String packageName) {
		List<JavaClass> classes = new ArrayList<JavaClass>();

		if (!directory.exists())
			return classes;

		File[] files = directory.listFiles();
		for (File file : files) {
			String fileName = file.getName();
			if (file.isDirectory())
				classes.addAll(findClasses(file, packageName + "." + fileName));
			else if (fileName.endsWith(".class"))
				try {
					classes.add(Repository.lookupClass(packageName + '.'
							+ fileName.substring(0, fileName.length() - 6)));
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}

		}
		return classes;
	}

	public static Set<AnalysisResult> analyzeMethods(DetailedClass classToCheck) {
		Queue<QualifiedMethod> queue = new LinkedList<QualifiedMethod>(
				classToCheck.getInstantiations());

		logger.warning("Methods to Analyze " + queue.size());
		for (QualifiedMethod qualifiedMethod : queue) {
			logger.warning(qualifiedMethod.toString());
		}

		AnalysisCache analysisCache = new AnalysisCache();
		HashSet<AnalysisResult> results = new HashSet<AnalysisResult>();

		while (!queue.isEmpty()) {
			QualifiedMethod method = queue.remove();

			// logger.warning(String
			// .format("\n###############################################################################\n"
			// + "#         %-50s                  #"
			// +
			// "\n###############################################################################\n",
			// method.getJavaClass().getClassName() + "."
			// + method.getMethod().getName()));

			Type returnType = method.getMethod().getReturnType();
			if (returnType instanceof ObjectType
					&& ((ObjectType) returnType).getClassName().equals(classToCheck.getName())) {
				queue.addAll(method.getCallingMethods());
			}

			ClassContext classContextMock = mock(ClassContext.class);
			when(classContextMock.getJavaClass()).thenReturn(method.getJavaClass());

			MethodGen methodGen = new MethodGen(method.getMethod(), method.getJavaClass()
					.getClassName(), new ConstantPoolGen(method.getJavaClass().getConstantPool()));

			BaseMethodAnalyzer methodAnalyzer = new StackConfinementAnalyzer(classContextMock,
					methodGen, analysisCache, 0, classToCheck);

			results.add(methodAnalyzer.analyze());
		}
		return results;
	}

	public static Set<AnalysisResult> analyzeMethodsInClass(DetailedClass classToCheck,
			DetailedClass classToAnalyze) {

		Queue<QualifiedMethod> queue = new LinkedList<QualifiedMethod>(
				classToCheck.getInstantiations());

		AnalysisCache analysisCache = new AnalysisCache();
		HashSet<AnalysisResult> results = new HashSet<AnalysisResult>();

		while (!queue.isEmpty()) {
			QualifiedMethod method = queue.remove();

			Type returnType = method.getMethod().getReturnType();
			if (returnType instanceof ObjectType
					&& ((ObjectType) returnType).getClassName().equals(classToCheck.getName())) {
				queue.addAll(method.getCallingMethods());
			}

			if (!method.getJavaClass().equals(classToAnalyze.getJavaClass()))
				continue;

			ClassContext classContextMock = mock(ClassContext.class);
			when(classContextMock.getJavaClass()).thenReturn(method.getJavaClass());

			MethodGen methodGen = new MethodGen(method.getMethod(), method.getJavaClass()
					.getClassName(), new ConstantPoolGen(method.getJavaClass().getConstantPool()));

			BaseMethodAnalyzer methodAnalyzer = new StackConfinementAnalyzer(classContextMock,
					methodGen, analysisCache, 0, classToCheck);

			results.add(methodAnalyzer.analyze());
		}
		return results;

	}
}

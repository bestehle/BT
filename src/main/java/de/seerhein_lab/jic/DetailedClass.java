package de.seerhein_lab.jic;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;

import de.seerhein_lab.jic.analyzer.QualifiedMethod;

public class DetailedClass {
	private final static Map<String, DetailedClass> classes = new HashMap<String, DetailedClass>();

	private final JavaClass clazz;
	private final Map<String, QualifiedMethod> methods = new HashMap<String, QualifiedMethod>();
	private final Set<QualifiedMethod> instantiations = new HashSet<QualifiedMethod>();

	private DetailedClass(JavaClass clazz) {
		this.clazz = clazz;
		for (Method method : clazz.getMethods()) {
			methods.put(method.getName(), new QualifiedMethod(clazz, method));
		}
	}

	public static Collection<DetailedClass> getClasses() {
		return classes.values();
	}

	public static DetailedClass getClass(String name) {
		if (!classes.containsKey(name)) {
			DetailedClass newClass = null;
			try {
				newClass = new DetailedClass(Repository.lookupClass(name));
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			classes.put(name, newClass);
			return newClass;
		}
		return classes.get(name);
	}

	public static DetailedClass getClass(JavaClass clazz) {
		if (!classes.containsKey(clazz.getClassName()))
			classes.put(clazz.getClassName(), new DetailedClass(clazz));
		return classes.get(clazz.getClassName());
	}

	public Map<String, QualifiedMethod> getMethods() {
		return methods;
	}

	public QualifiedMethod getMethod(String method) {
		QualifiedMethod targetMethod = methods.get(method);

		while (targetMethod == null) {
			try {
				targetMethod = DetailedClass.getClass(clazz.getSuperClass()).getMethod(method);
			} catch (ClassNotFoundException e) {
				throw new AssertionError("targetMethod " + method + " not found in " + clazz
						+ " or its supertypes");
			}
		}

		return targetMethod;
	}

	public QualifiedMethod getMethod(Method method) {
		return methods.get(method.getName());
	}

	public void addMethod(QualifiedMethod method) {
		this.methods.put(method.toString(), method);
	}

	public Set<QualifiedMethod> getInstantiations() {
		return instantiations;
	}

	public void addInstantiation(QualifiedMethod method) {
		instantiations.add(method);
	}

	public String getName() {
		return clazz.getClassName();
	}
}

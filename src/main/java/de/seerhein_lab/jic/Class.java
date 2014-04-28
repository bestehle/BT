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

public class Class {
	private final static Map<String, Class> classes = new HashMap<String, Class>();

	private final JavaClass clazz;
	private final Map<String, QualifiedMethod> methods = new HashMap<String, QualifiedMethod>();
	private final Set<QualifiedMethod> instantiations = new HashSet<QualifiedMethod>();

	private Class(JavaClass clazz) {
		this.clazz = clazz;
		for (Method method : clazz.getMethods()) {
			methods.put(method.getName(), new QualifiedMethod(clazz, method));
		}
	}

	public static Collection<Class> getClasses() {
		return classes.values();
	}

	public static Class getClass(String name) {
		if (!classes.containsKey(name)) {
			Class newClass = null;
			try {
				newClass = new Class(Repository.lookupClass(name));
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			classes.put(name, newClass);
			return newClass;
		}
		return classes.get(name);
	}

	public static Class getClass(JavaClass clazz) {
		if (!classes.containsKey(clazz.getClassName()))
			classes.put(clazz.getClassName(), new Class(clazz));
		return classes.get(clazz.getClassName());
	}

	public Map<String, QualifiedMethod> getMethods() {
		return methods;
	}

	public QualifiedMethod getMethod(String method) {
		return methods.get(method);
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

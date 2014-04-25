package de.seerhein_lab.jic;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Class {
	private final static Map<String, Class> classes = new HashMap<String, Class>();

	private final String name;
	private final Map<String, ClassMethod> methods = new HashMap<String, ClassMethod>();
	private final Set<ClassMethod> instantiations = new HashSet<ClassMethod>();

	public Class(String name) {
		this.name = name;
		classes.put(name, this);
	}

	public static Collection<Class> getClasses() {
		return classes.values();
	}

	public static Class getClass(String name) {
		if (!classes.containsKey(name))
			classes.put(name, new Class(name));
		return classes.get(name);
	}

	public static void addInstantiation(String clazz, ClassMethod method) {
		Class c = classes.get(clazz);
		if (c == null) {
			c = new Class(clazz);
			classes.put(clazz, c);
		}
		c.addInstantiation(method);
	}

	public Map<String, ClassMethod> getMethods() {
		return methods;
	}

	public ClassMethod getMethod(String method) {
		if (!methods.containsKey(method))
			methods.put(method, new ClassMethod(method));
		return methods.get(method);
	}

	public void addMethod(ClassMethod method) {
		this.methods.put(method.getName(), method);
	}

	public Set<ClassMethod> getInstantiations() {
		return instantiations;
	}

	public void addInstantiation(ClassMethod method) {
		instantiations.add(method);
	}

	public String getName() {
		return name;
	}
}

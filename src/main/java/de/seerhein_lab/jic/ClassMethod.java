package de.seerhein_lab.jic;

import java.util.HashSet;
import java.util.Set;

public class ClassMethod {
	private final String name;
	private final Set<ClassMethod> callingMethods = new HashSet<ClassMethod>();
	private final Set<Class> classInstantiations = new HashSet<Class>();

	public ClassMethod(String name) {
		this.name = name;
	}

	public Set<ClassMethod> getCallingMethods() {
		return callingMethods;
	}

	public void addCallingMethod(ClassMethod method) {
		this.callingMethods.add(method);
	}

	public Set<Class> getClassInstantiations() {
		return classInstantiations;
	}

	public void addClassInstantiation(Class classInstantiation) {
		this.classInstantiations.add(classInstantiation);
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name + "()";
	}
}

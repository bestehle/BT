package de.seerhein_lab.jic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;

import de.seerhein_lab.jic.analyzer.QualifiedMethod;

public class DetailedClass {

	private final JavaClass clazz;
	private final Map<String, QualifiedMethod> methods = new HashMap<String, QualifiedMethod>();
	private final Set<QualifiedMethod> instantiations = new HashSet<QualifiedMethod>();
	private final ClassRepository repository;

	DetailedClass(JavaClass clazz, ClassRepository repository) {
		this.clazz = clazz;
		this.repository = repository;
		for (Method method : clazz.getMethods()) {
			methods.put(method.getName(), new QualifiedMethod(clazz, method));
		}
	}

	public Map<String, QualifiedMethod> getMethods() {
		return methods;
	}

	public QualifiedMethod getMethod(String method) {
		// if (clazz.isInterface()) {
		// JavaClass[] interfaces = clazz.getAllInterfaces();
		// for (JavaClass interFace : interfaces) {
		// targetMethod = repository.getClass(interFace).getMethod(method);
		// } TODO
		// }

		QualifiedMethod targetMethod = methods.get(method);

		while (targetMethod == null) {
			try {
				targetMethod = repository.getClass(clazz.getSuperClass()).getMethod(method);
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

	public JavaClass getJavaClass() {
		return clazz;
	}

	@Override
	public String toString() {
		return "DetailedClass [clazz=" + clazz.getClassName() + "]";
	}
}

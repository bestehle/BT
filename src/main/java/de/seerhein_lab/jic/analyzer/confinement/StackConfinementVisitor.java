package de.seerhein_lab.jic.analyzer.confinement;

import java.util.HashSet;
import java.util.Set;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.CodeExceptionGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;

import de.seerhein_lab.jic.DetailedClass;
import de.seerhein_lab.jic.Pair;
import de.seerhein_lab.jic.analyzer.BaseMethodAnalyzer;
import de.seerhein_lab.jic.analyzer.BaseVisitor;
import de.seerhein_lab.jic.analyzer.QualifiedMethod;
import de.seerhein_lab.jic.cache.AnalysisCache;
import de.seerhein_lab.jic.cache.AnalysisCache.Check;
import de.seerhein_lab.jic.slot.Slot;
import de.seerhein_lab.jic.vm.Frame;
import de.seerhein_lab.jic.vm.Heap;
import de.seerhein_lab.jic.vm.HeapObject;
import de.seerhein_lab.jic.vm.PC;
import de.seerhein_lab.jic.vm.ReferenceSlot;
import de.seerhein_lab.jic.vm.UnknownObject;
import edu.umd.cs.findbugs.annotations.Confidence;
import edu.umd.cs.findbugs.ba.ClassContext;

public class StackConfinementVisitor extends BaseVisitor {
	private DetailedClass classToCheck;

	protected StackConfinementVisitor(ClassContext classContext, MethodGen methodGen, Frame frame,
			Heap heap, ConstantPoolGen constantPoolGen, PC pc,
			CodeExceptionGen[] exceptionHandlers, Set<QualifiedMethod> alreadyVisitedMethods,
			int depth, Set<Pair<InstructionHandle, Boolean>> alreadyVisitedIfBranch,
			AnalysisCache cache, int methodInvocationDepth, DetailedClass classToAnalyze) {
		super(classContext, methodGen, frame, heap, constantPoolGen, alreadyVisitedIfBranch,
				alreadyVisitedMethods, pc, exceptionHandlers, depth, cache, methodInvocationDepth);
		this.classToCheck = classToAnalyze;
	}

	@Override
	protected Check getCheck() {
		return null;
	}

	@Override
	protected BaseMethodAnalyzer getMethodAnalyzer(MethodGen targetMethodGen,
			Set<QualifiedMethod> alreadyVisitedMethods, int methodInvocationDepth) {
		return new StackConfinementAnalyzer(classContext, targetMethodGen, alreadyVisitedMethods,
				depth, cache, methodInvocationDepth, classToCheck);
	}

	// ******************************************************************//
	// Bug detection section //
	// ******************************************************************//

	@Override
	protected void detectVirtualMethodBug(ReferenceSlot argument) {
		if (!(argument instanceof ReferenceSlot))
			return;

		HeapObject object = ((ReferenceSlot) argument).getObject(heap);

		if (object == null || object instanceof UnknownObject)
			return;

		if (classToCheck != null && !object.getType().equals(classToCheck.getName()))
			return;

		logger.warning("StackConfinementBUG in " + this.classContext.getJavaClass().getClassName()
				+ object);
		addBug("STACK_CONFINEMENT_BUG", Confidence.HIGH,
				"instance is passed to a virtual method -> may be not stack confined",
				pc.getCurrentInstruction());
	}

	@Override
	protected void detectXAStoreBug(ReferenceSlot arrayReference, Slot valueToStore) {
		detect(valueToStore);
	}

	@Override
	protected void detectPutFieldBug(ReferenceSlot targetReference, Slot valueToPut) {
		detect(valueToPut);
	}

	@Override
	protected void detectPutStaticBug(ReferenceSlot referenceToPut) {
		detect(referenceToPut);
	}

	@Override
	protected void detectAReturnBug(ReferenceSlot returnSlot) {
	}

	private void detect(Slot value) {
		if (!(value instanceof ReferenceSlot))
			return;

		HeapObject object = ((ReferenceSlot) value).getObject(heap);

		if (object == null || object instanceof UnknownObject)
			return;

		if (classToCheck != null && !object.getType().equals(classToCheck.getName()))
			return;

		logger.warning("StackConfinementBUG in " + this.classContext.getJavaClass().getClassName()
				+ object);
		addBug("STACK_CONFINEMENT_BUG", Confidence.HIGH,
				"instance is assigned to an object -> not stack confined",
				pc.getCurrentInstruction());
	}

	@Override
	protected boolean hasToBeAnalyzed(InvokeInstruction instruction) {
		if (instruction.getLoadClassType(constantPoolGen).getClassName()
				.equals(classToCheck.getName()))
			return true; // TODO Superklassen ???
			// if (instruction.getMethodName(constantPoolGen).equals("<init>"))
			// return true;

		JavaClass[] interfaces = null;
		JavaClass[] superClasses = null;
		try {
			interfaces = classToCheck.getJavaClass().getAllInterfaces();
			superClasses = classToCheck.getJavaClass().getSuperClasses();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		Set<String> classes = new HashSet<String>();
		classes.add(classToCheck.getName());

		for (JavaClass javaClass : superClasses) {
			classes.add(javaClass.getClassName());
		}
		for (JavaClass javaClass : interfaces) {
			classes.add(javaClass.getClassName());
		}

		for (Type argument : instruction.getArgumentTypes(constantPoolGen)) {
			if (classes.contains(argument.toString()))
				return true;
		}
		if (classes.contains(instruction.getReturnType(constantPoolGen).toString()))
			return true;

		return false;
	}
}

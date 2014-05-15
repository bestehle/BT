package de.seerhein_lab.jic.analyzer.confinement;

import java.util.Set;

import org.apache.bcel.generic.CodeExceptionGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.MethodGen;

import de.seerhein_lab.jic.Class;
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
import edu.umd.cs.findbugs.ba.ClassContext;

public class StackConfinementVisitor extends BaseVisitor {
	private Class classToAnalyze;

	protected StackConfinementVisitor(ClassContext classContext, MethodGen methodGen, Frame frame,
			Heap heap, ConstantPoolGen constantPoolGen, PC pc,
			CodeExceptionGen[] exceptionHandlers, Set<QualifiedMethod> alreadyVisitedMethods,
			int depth, Set<Pair<InstructionHandle, Boolean>> alreadyVisitedIfBranch,
			AnalysisCache cache, int methodInvocationDepth, Class classToAnalyze) {
		super(classContext, methodGen, frame, heap, constantPoolGen, alreadyVisitedIfBranch,
				alreadyVisitedMethods, pc, exceptionHandlers, depth, cache, methodInvocationDepth);
		this.classToAnalyze = classToAnalyze;
	}

	@Override
	protected Check getCheck() {
		return null;
	}

	@Override
	protected BaseMethodAnalyzer getMethodAnalyzer(MethodGen targetMethodGen,
			Set<QualifiedMethod> alreadyVisitedMethods, int methodInvocationDepth) {
		return new ConfinementAnalyzer(classContext, targetMethodGen, alreadyVisitedMethods, depth,
				cache, methodInvocationDepth, classToAnalyze);
	}

	// ******************************************************************//
	// Bug detection section //
	// ******************************************************************//

	@Override
	protected void detectVirtualMethodBug(ReferenceSlot argument) {
	}

	@Override
	protected void detectXAStoreBug(ReferenceSlot arrayReference, Slot valueToStore) {
	}

	@Override
	protected void detectPutFieldBug(ReferenceSlot targetReference, Slot valueToPut) {
		if (!(valueToPut instanceof ReferenceSlot))
			return;

		HeapObject value = ((ReferenceSlot) valueToPut).getObject(heap);

		if (!value.getType().equals(classToAnalyze.getName()))
			return;

		HeapObject targetObject = targetReference.getObject(heap);

		if (!targetObject.isStackConfined())
			System.out.println("StackConfinementBUG: " + value);

		// if (targetObject.equals(heap.getThisInstance()))
		//
		// value.setStackConfinement(targetObject);
		//
		// for (HeapObject referred : value.getReferredObjects()) {
		// referred.setStackConfinement(targetObject);
		// }
	}

	@Override
	protected void detectPutStaticBug(ReferenceSlot referenceToPut) {
		if (!(referenceToPut instanceof ReferenceSlot))
			return;

		HeapObject value = ((ReferenceSlot) referenceToPut).getObject(heap);

		if (!value.getType().equals(classToAnalyze.getName()))
			return;

		System.out.println("StackConfinementBUG: " + value);
	}

	@Override
	protected void detectAReturnBug(ReferenceSlot returnSlot) {
	}

}

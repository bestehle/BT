package de.seerhein_lab.jic.analyzer.confinement;

import java.util.Set;

import org.apache.bcel.generic.CodeExceptionGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.MethodGen;

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
	private DetailedClass classToAnalyze;

	protected StackConfinementVisitor(ClassContext classContext, MethodGen methodGen, Frame frame,
			Heap heap, ConstantPoolGen constantPoolGen, PC pc,
			CodeExceptionGen[] exceptionHandlers, Set<QualifiedMethod> alreadyVisitedMethods,
			int depth, Set<Pair<InstructionHandle, Boolean>> alreadyVisitedIfBranch,
			AnalysisCache cache, int methodInvocationDepth, DetailedClass classToAnalyze) {
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
		return new StackConfinementAnalyzer(classContext, targetMethodGen, alreadyVisitedMethods,
				depth, cache, methodInvocationDepth, classToAnalyze);
	}

	// ******************************************************************//
	// Bug detection section //
	// ******************************************************************//

	@Override
	protected void detectVirtualMethodBug(ReferenceSlot argument) {
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
		// TODO
	}

	private void detect(Slot value) {
		if (!(value instanceof ReferenceSlot))
			return;

		HeapObject object = ((ReferenceSlot) value).getObject(heap);

		if (object == null)
			return;

		if (classToAnalyze != null && !object.getType().equals(classToAnalyze.getName()))
			return;

		logger.warning("StackConfinementBUG: " + object);
		addBug("STACK_CONFINEMENT_BUG", Confidence.HIGH,
				"instance is assigned to an object -> not stack confined",
				pc.getCurrentInstruction());
	}

}

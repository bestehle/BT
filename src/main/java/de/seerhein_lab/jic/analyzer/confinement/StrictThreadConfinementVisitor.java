package de.seerhein_lab.jic.analyzer.confinement;

import java.util.Set;

import org.apache.bcel.generic.CodeExceptionGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.MethodGen;

import de.seerhein_lab.jic.AnalysisResult;
import de.seerhein_lab.jic.DetailedClass;
import de.seerhein_lab.jic.EvaluationResult;
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

public class StrictThreadConfinementVisitor extends BaseVisitor {
	private DetailedClass classToAnalyze;

	protected StrictThreadConfinementVisitor(ClassContext classContext, MethodGen methodGen, Frame frame,
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
		return new StrictThreadConfinementAnalyzer(classContext, targetMethodGen, alreadyVisitedMethods, depth,
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

		Set<QualifiedMethod> methods = DetailedClass.getClass(methodGen.getClassName())
				.getMethod(methodGen.getMethod().getName())
				.getCallingMethodsWithInstantiation(DetailedClass.getClass(targetObject.getType()));

		for (QualifiedMethod method : methods) {

			MethodGen targetMethodGen = new MethodGen(method.getMethod(), method.getJavaClass()
					.getClassName(), new ConstantPoolGen(method.getJavaClass().getConstantPool()));

			BaseMethodAnalyzer methodAnalyzer = new StrictThreadConfinementAnalyzer(classContext,
					targetMethodGen, cache, 0, DetailedClass.getClass(targetObject.getType()));

			AnalysisResult results = methodAnalyzer.analyze();

			for (EvaluationResult result : results.getResults()) {
				for (HeapObject object : result.getHeap().getObjects()) {
					// object.getType().equals(anObject);
				}
			}

		}
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
	}

	@Override
	protected void detectAReturnBug(ReferenceSlot returnSlot) {
	}

}

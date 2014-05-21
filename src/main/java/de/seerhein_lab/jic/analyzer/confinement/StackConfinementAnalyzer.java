package de.seerhein_lab.jic.analyzer.confinement;

import java.util.HashSet;
import java.util.Set;

import net.jcip.annotations.ThreadSafe;

import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.MethodGen;

import de.seerhein_lab.jic.DetailedClass;
import de.seerhein_lab.jic.Pair;
import de.seerhein_lab.jic.analyzer.BaseMethodAnalyzer;
import de.seerhein_lab.jic.analyzer.BaseVisitor;
import de.seerhein_lab.jic.analyzer.QualifiedMethod;
import de.seerhein_lab.jic.cache.AnalysisCache;
import de.seerhein_lab.jic.vm.Frame;
import de.seerhein_lab.jic.vm.Heap;
import de.seerhein_lab.jic.vm.PC;
import edu.umd.cs.findbugs.ba.ClassContext;

@ThreadSafe
// Superclass is thread-safe, this sub-class doesn't add any public methods
public final class StackConfinementAnalyzer extends BaseMethodAnalyzer {
	private DetailedClass classToAnalyze;

	public StackConfinementAnalyzer(ClassContext classContext, MethodGen methodGen, AnalysisCache cache,
			int methodInvocationDepth, DetailedClass classToAnalyze) {
		this(classContext, methodGen, new HashSet<QualifiedMethod>(), -1, cache,
				methodInvocationDepth, classToAnalyze);
		alreadyVisitedMethods.add(new QualifiedMethod(classContext.getJavaClass(), methodGen
				.getMethod()));
	}

	public StackConfinementAnalyzer(ClassContext classContext, MethodGen methodGen,
			Set<QualifiedMethod> alreadyVisitedMethods, int depth, AnalysisCache cache,
			int methodInvocationDepth, DetailedClass classToAnalyze) {
		super(classContext, methodGen, alreadyVisitedMethods, depth, cache, methodInvocationDepth);
		this.classToAnalyze = classToAnalyze;
	}

	protected BaseVisitor getInstructionVisitor(Frame frame, Heap heap, PC pc,
			Set<Pair<InstructionHandle, Boolean>> alreadyVisitedIfBranch) {
		return new StackConfinementVisitor(classContext, methodGen, frame, heap,
				methodGen.getConstantPool(), pc, exceptionHandlers, alreadyVisitedMethods, depth,
				alreadyVisitedIfBranch, cache, methodInvocationDepth, classToAnalyze);
	}

	@Override
	protected Heap getHeap() {
		return new Heap(this.classContext.getJavaClass().getClassName());
	}

}

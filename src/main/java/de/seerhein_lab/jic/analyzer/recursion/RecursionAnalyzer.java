package de.seerhein_lab.jic.analyzer.recursion;

import java.util.HashSet;
import java.util.Set;

import net.jcip.annotations.ThreadSafe;

import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.MethodGen;

import de.seerhein_lab.jic.ClassRepository;
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
public final class RecursionAnalyzer extends BaseMethodAnalyzer {

	public RecursionAnalyzer(ClassContext classContext, MethodGen methodGen, AnalysisCache cache,
			int methodInvocationDepth, ClassRepository repository) {
		this(classContext, methodGen, new HashSet<QualifiedMethod>(), -1, cache,
				methodInvocationDepth, repository);
		alreadyVisitedMethods.add(new QualifiedMethod(classContext.getJavaClass(), methodGen
				.getMethod()));
	}

	public RecursionAnalyzer(ClassContext classContext, MethodGen methodGen,
			Set<QualifiedMethod> alreadyVisitedMethods, int depth, AnalysisCache cache,
			int methodInvocationDepth, ClassRepository repository) {
		super(classContext, methodGen, alreadyVisitedMethods, depth, cache, methodInvocationDepth,
				repository);
	}

	protected BaseVisitor getInstructionVisitor(Frame frame, Heap heap, PC pc,
			Set<Pair<InstructionHandle, Boolean>> alreadyVisitedIfBranch) {
		return new RecursionVisitor(classContext, methodGen, frame, heap,
				methodGen.getConstantPool(), pc, exceptionHandlers, alreadyVisitedMethods, depth,
				alreadyVisitedIfBranch, cache, methodInvocationDepth, repository);
	}

	@Override
	protected Heap getHeap() {
		return new Heap(this.classContext.getJavaClass().getClassName());
	}

}

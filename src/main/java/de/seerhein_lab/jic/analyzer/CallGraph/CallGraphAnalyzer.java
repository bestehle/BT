package de.seerhein_lab.jic.analyzer.CallGraph;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import net.jcip.annotations.ThreadSafe;

import org.apache.bcel.generic.CodeExceptionGen;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;

import de.seerhein_lab.jic.AnalysisResult;
import de.seerhein_lab.jic.Pair;
import de.seerhein_lab.jic.Utils;
import de.seerhein_lab.jic.analyzer.BaseVisitor;
import de.seerhein_lab.jic.analyzer.ClassHelper;
import de.seerhein_lab.jic.slot.Slot;
import de.seerhein_lab.jic.vm.Frame;
import de.seerhein_lab.jic.vm.Heap;
import de.seerhein_lab.jic.vm.OpStack;
import de.seerhein_lab.jic.vm.PC;
import de.seerhein_lab.jic.vm.ReferenceSlot;
import edu.umd.cs.findbugs.ba.ClassContext;

@ThreadSafe
// Superclass is thread-safe, this sub-class doesn't add any public methods
public final class CallGraphAnalyzer {
	private static final Logger logger = Logger.getLogger("BaseMethodAnalyzer");
	protected final ClassContext classContext;
	protected final MethodGen methodGen;
	protected final CodeExceptionGen[] exceptionHandlers;

	protected CallGraphAnalyzer(ClassContext classContext, MethodGen methodGen) {
		if (classContext == null || methodGen == null)
			throw new AssertionError("Params must not be null.");

		this.classContext = classContext;
		this.methodGen = methodGen;
		exceptionHandlers = methodGen.getExceptionHandlers();
	}

	protected BaseVisitor getInstructionVisitor(Frame frame, Heap heap, PC pc,
			Set<Pair<InstructionHandle, Boolean>> alreadyVisitedIfBranch) {
		return null;
	}

	protected abstract Heap getHeap();

	private Frame createCalleeFrame(OpStack callerOpStack) {
		int numSlots = methodGen.isStatic() ? 0 : 1;

		for (Type type : methodGen.getArgumentTypes()) {
			numSlots += Slot.getDefaultSlotInstance(type).getNumSlots();
		}

		Frame calleeFrame = new Frame(methodGen.getMethod().getCode().getMaxLocals(),
				callerOpStack, numSlots);
		return calleeFrame;
	}

	public final synchronized AnalysisResult analyze() {
		logger.info(methodGen.getClassName() + "." + methodGen.getMethod().getName()
				+ methodGen.getMethod().getSignature());
		OpStack callerStack = new OpStack();
		Heap callerHeap = getHeap();
		callerStack.push(ReferenceSlot.getThisReference(callerHeap));

		return analyze(callerStack, 0, callerHeap);
	}

	private final AnalysisResult analyze(OpStack callerStack, int index, Heap heap) {
		Type[] arguments = methodGen.getArgumentTypes();

		if (index == arguments.length)
			return analyze(callerStack, heap);

		Slot argument = Slot.getDefaultSlotInstance(arguments[index]);
		if (!(argument instanceof ReferenceSlot)) {
			for (int i = 0; i < argument.getNumSlots(); i++)
				callerStack.push(argument);

			return analyze(callerStack, index + 1, heap);
		}

		OpStack clonedStack = new OpStack(callerStack);

		return analyze(
				callerStack.push(ReferenceSlot.getExternalReference(heap,
						ClassHelper.isImmutableAndFinal(arguments[index]))), index + 1, heap)
				.merge(analyze(clonedStack.push(ReferenceSlot.getNullReference()), index + 1, heap));

	}

	public synchronized AnalysisResult analyze(OpStack callerStack, Heap heap) {
		Frame calleeFrame = createCalleeFrame(callerStack);

		InstructionHandle[] instructionHandles = new InstructionList(methodGen.getMethod()
				.getCode().getCode()).getInstructionHandles();

		return analyze(instructionHandles[0], calleeFrame, heap,
				new HashSet<Pair<InstructionHandle, Boolean>>());
	}

	public final synchronized AnalysisResult analyze(InstructionHandle ih, Frame frame, Heap heap,
			Set<Pair<InstructionHandle, Boolean>> alreadyVisitedIfBranch) {

		PC pc = new PC(ih);

		BaseVisitor visitor = getInstructionVisitor(frame, heap, pc, alreadyVisitedIfBranch);

		logger.fine(Utils.formatLoggingOutput(this.methodInvocationDepth)
				+ "vvvvvvvvvvvvvvvvvvvvvvvvvv");
		while (pc.isValid()) {
			// visitor is expected to
			// (1) either execute the current opcode and then update the pc, or
			// (2) deliver a (possibly multi-value) result and invalidate the
			// pc.
			// The result can be computed by execution of the last opcode in
			// the list, or by recursively instantiating other analyzers.
			pc.getCurrentInstruction().accept(visitor);
		}

		logger.fine(Utils.formatLoggingOutput(this.methodInvocationDepth)
				+ "^^^^^^^^^^^^^^^^^^^^^^^^^^");

		return new AnalysisResult(visitor.getResult(), visitor.getBugs().getCollection());
	}

}

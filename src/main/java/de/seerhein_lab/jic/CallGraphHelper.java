package de.seerhein_lab.jic;

import java.util.logging.Logger;

import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.NEW;

import de.seerhein_lab.jic.analyzer.QualifiedMethod;

public class CallGraphHelper {
	private static final Logger logger = Logger.getLogger("CallGraphHelper");

	public static void generateCallGraph(JavaClass jClazz) {
		Class clazz = Class.getClass(jClazz);

		for (Method method : jClazz.getMethods()) {

			logger.severe("<---  " + method.getName() + method.getSignature() + " --->");

			MethodGen methodGen = new MethodGen(method, jClazz.getClassName(), new ConstantPoolGen(
					jClazz.getConstantPool()));

			ConstantPoolGen constantPool = methodGen.getConstantPool();

			Code code = methodGen.getMethod().getCode();
			if (code == null)
				continue;

			InstructionHandle[] instructions = new InstructionList(code.getCode())
					.getInstructionHandles();

			for (InstructionHandle ih : instructions) {
				if (ih.getInstruction() instanceof NEW) {
					NEW newInstruction = (NEW) ih.getInstruction();
					Class.getClass(newInstruction.getLoadClassType(constantPool).getClassName())
							.addInstantiation(clazz.getMethod(method));

					logger.severe("new: " + newInstruction.getLoadClassType(constantPool));
				} else if (ih.getInstruction() instanceof InvokeInstruction) {
					InvokeInstruction invokeInstruction = (InvokeInstruction) ih.getInstruction();
					Class.getClass(invokeInstruction.getLoadClassType(constantPool).getClassName())
							.getMethod(invokeInstruction.getMethodName(constantPool))
							.addCallingMethod(clazz.getMethod(method));

					logger.severe(ih.getInstruction().getName() + "   --    "
							+ invokeInstruction.getLoadClassType(constantPool) + "."
							+ invokeInstruction.getMethodName(constantPool));
				}
			}
			System.out.println();
			;
		}
	}

	public static void printCallGraph() {
		logger.severe("Ergebnis:\n");

		for (Class clazz : Class.getClasses()) {
			logger.severe(clazz.getName());
			logger.severe("\tInstanzierungen: " + clazz.getInstantiations());
			for (QualifiedMethod method : clazz.getMethods().values()) {
				logger.severe("\t" + method + ": " + method.getCallingMethods());
			}
		}
	}
}

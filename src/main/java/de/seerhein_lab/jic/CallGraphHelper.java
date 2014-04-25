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

public class CallGraphHelper {
	private static final Logger logger = Logger.getLogger("CallGraphHelper");

	public static void generateCallGraph(JavaClass jClazz) {
		Class clazz = new Class(jClazz.getClassName());

		for (Method method : jClazz.getMethods()) {
			ClassMethod m = new ClassMethod(method.getName());
			clazz.addMethod(m);

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
					Class.addInstantiation(newInstruction.getLoadClassType(constantPool)
							.getClassName(), m);

					System.out.println("new: " + newInstruction.getLoadClassType(constantPool));
				} else if (ih.getInstruction() instanceof InvokeInstruction) {
					InvokeInstruction invokeInstruction = (InvokeInstruction) ih.getInstruction();
					Class.getClass(invokeInstruction.getLoadClassType(constantPool).getClassName())
							.getMethod(invokeInstruction.getMethodName(constantPool))
							.addCallingMethod(m);

					System.out.println(ih.getInstruction().getName() + "   --    "
							+ invokeInstruction.getLoadClassType(constantPool) + "."
							+ invokeInstruction.getMethodName(constantPool));
				}
			}
			System.out.println();
			;
		}
	}

	public static void printCallGraph() {
		System.out.println("Ergebnis:\n");

		for (Class clazz : Class.getClasses()) {
			System.out.println(clazz.getName());
			System.out.println("\tInstanzierungen: " + clazz.getInstantiations());
			for (ClassMethod method : clazz.getMethods().values()) {
				System.out.println("\t" + method.getName());
				System.out.println("\t\tAufegrufen in:" + method.getCallingMethods());
			}
		}
	}
}

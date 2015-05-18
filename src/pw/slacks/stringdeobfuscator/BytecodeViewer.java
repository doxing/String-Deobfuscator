package pw.slacks.stringdeobfuscator;


import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.ASMifier;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

/* Created on 5/18/2015 @ 6:32 AM */
public class BytecodeViewer {

    private TraceMethodVisitor mp;
    private Printer printer;

    public BytecodeViewer(){
        this.printer = new Textifier();
        this.mp = new TraceMethodVisitor(printer);
    }

    public String scanClass(InputStream in) throws Exception {
        ClassReader cr = new ClassReader(in);
        ClassNode classNode = new ClassNode();

        cr.accept(classNode, 0);

        String byteCode = "";

        for(Object o : classNode.methods){
            MethodNode methodNode = (MethodNode) o;
            if(methodNode.name.equals("<clinit>"))
                byteCode += scanMethod(methodNode);
        }

        return byteCode;
    }

    private String scanMethod(MethodNode methodNode){
        ASMifier asMifier = new ASMifier();
        asMifier.getText();

        StringBuilder sb = new StringBuilder();
        for(AbstractInsnNode aIN : methodNode.instructions.toArray()){
            sb.append(insnToString(aIN));
        }
        return sb.toString();
    }

    private String insnToString(AbstractInsnNode insn){
        insn.accept(mp);
        StringWriter sw = new StringWriter();
        printer.print(new PrintWriter(sw));
        printer.getText().clear();
        return sw.toString();
    }

}

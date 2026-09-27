package com.gx.velveteclipse.compat;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

/**
 * OptiFine I6 pre6 changes warn(String,Object[]) to warn(String,Object,Object).
 * LDLib 1.0.52.a redirects the former with require=1. Restore the equivalent
 * varargs call before mixin injection, preserving both argument objects in order.
 * No library JAR is patched, and no model-loading or shader instruction is removed.
 */
public final class ModelBakeryLogBridge {
    public static final String TARGET = "net/minecraft/client/resources/model/ModelBakery";
    public static final String VARARGS = "(Ljava/lang/String;[Ljava/lang/Object;)V";
    public static final String PAIR = "(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V";
    private static final String GET_MODEL = "(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/resources/model/UnbakedModel;";

    private ModelBakeryLogBridge() {}

    public static int normalize(ClassNode type) {
        if (!TARGET.equals(type.name)) return 0;
        int changed = 0;
        for (MethodNode method : type.methods) {
            if ((!method.name.equals("m_119341_") && !method.name.equals("getModel")) || !GET_MODEL.equals(method.desc)) continue;
            // Vanilla / an already adapted class already supplies LDLib's required target.
            boolean present = false;
            for (var instruction : method.instructions)
                if (instruction instanceof MethodInsnNode call && warning(call, VARARGS)) present = true;
            if (present) continue;
            for (var instruction : method.instructions.toArray()) {
                if (!(instruction instanceof MethodInsnNode call) || !warning(call, PAIR)) continue;
                int first = method.maxLocals, second = first + 1;
                var pack = new InsnList();
                // Stack before: logger, message, first argument, second argument.
                pack.add(new VarInsnNode(Opcodes.ASTORE, second));
                pack.add(new VarInsnNode(Opcodes.ASTORE, first));
                pack.add(new InsnNode(Opcodes.ICONST_2));
                pack.add(new TypeInsnNode(Opcodes.ANEWARRAY, "java/lang/Object"));
                pack.add(new InsnNode(Opcodes.DUP));
                pack.add(new InsnNode(Opcodes.ICONST_0));
                pack.add(new VarInsnNode(Opcodes.ALOAD, first));
                pack.add(new InsnNode(Opcodes.AASTORE));
                pack.add(new InsnNode(Opcodes.DUP));
                pack.add(new InsnNode(Opcodes.ICONST_1));
                pack.add(new VarInsnNode(Opcodes.ALOAD, second));
                pack.add(new InsnNode(Opcodes.AASTORE));
                method.instructions.insertBefore(call, pack);
                call.desc = VARARGS;
                method.maxLocals += 2;
                method.maxStack += 2;
                changed++;
            }
        }
        return changed;
    }

    private static boolean warning(MethodInsnNode call, String descriptor) {
        return call.getOpcode() == Opcodes.INVOKEINTERFACE && call.itf
                && call.owner.equals("org/slf4j/Logger") && call.name.equals("warn") && call.desc.equals(descriptor);
    }
}

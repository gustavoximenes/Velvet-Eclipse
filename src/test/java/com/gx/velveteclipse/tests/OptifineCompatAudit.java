package com.gx.velveteclipse.tests;

import com.gx.velveteclipse.compat.*;
import com.google.gson.JsonParser;
import java.io.*;
import java.lang.reflect.Proxy;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;
import org.objectweb.asm.util.*;
import org.slf4j.Logger;
import static org.objectweb.asm.Opcodes.*;

/** Checks the exact boot-failure target without starting the game or a graphics context. */
public final class OptifineCompatAudit {
    private static int checks;
    private static final String MODEL_DESC="(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/resources/model/UnbakedModel;";
    private static final String GENERATED="com/gx/velveteclipse/tests/GeneratedLogBridgeFixture";
    private static void check(boolean b,String what){checks++;if(!b)throw new AssertionError(what);}
    private static ClassNode read(byte[] data){var node=new ClassNode();new ClassReader(data).accept(node,0);return node;}
    private static byte[] resource(String path)throws Exception{try(var in=OptifineCompatAudit.class.getResourceAsStream(path)){if(in==null)throw new AssertionError(path);return in.readAllBytes();}}
    private static MethodNode target(ClassNode c){return c.methods.stream().filter(m->m.desc.equals(MODEL_DESC)&&(m.name.equals("m_119341_")||m.name.equals("getModel"))).findFirst().orElseThrow();}
    private static long warnings(MethodNode method,String desc){return Arrays.stream(method.instructions.toArray()).filter(n->n instanceof MethodInsnNode m&&m.owner.equals("org/slf4j/Logger")&&m.name.equals("warn")&&m.desc.equals(desc)).count();}
    private static String code(MethodNode m){var text=new Textifier();m.accept(new TraceMethodVisitor(text));var out=new StringWriter();text.print(new PrintWriter(out));return out.toString();}
    private static void verify(ClassNode c)throws Exception{for(var m:c.methods)if((m.access&(ACC_ABSTRACT|ACC_NATIVE))==0){new Analyzer<BasicValue>(new BasicVerifier()).analyze(c.name,m);checks++;}}

    public static void main(String[] args)throws Exception{
        var vanilla=read(resource("/net/minecraft/client/resources/model/ModelBakery.class"));
        String before=code(target(vanilla));check(warnings(target(vanilla),ModelBakeryLogBridge.VARARGS)>0,"vanilla contains LDLib redirect target");
        check(ModelBakeryLogBridge.normalize(vanilla)==0&&before.equals(code(target(vanilla))),"vanilla/Forge unchanged");
        for(String name:List.of("getModel","m_119341_")){
            var fixture=fixture(name);check(warnings(target(fixture),ModelBakeryLogBridge.VARARGS)==0,"reproduces missing target");
            check(ModelBakeryLogBridge.normalize(fixture)==1,"one overload adapted");
            check(warnings(target(fixture),ModelBakeryLogBridge.VARARGS)==1&&warnings(target(fixture),ModelBakeryLogBridge.PAIR)==0,"redirect target restored");
            check(ModelBakeryLogBridge.normalize(fixture)==0,"idempotent adaptation");verify(fixture);execute(fixture,name);
        }
        var unrelated=fixture("getModel");unrelated.name="example/OtherClass";
        check(ModelBakeryLogBridge.normalize(unrelated)==0,"no unrelated class changes");
        var otherMethod=fixture("lookupOtherModel");check(ModelBakeryLogBridge.normalize(otherMethod)==0,"no unrelated method changes");

        var config=JsonParser.parseString(Files.readString(Path.of("src/main/resources/velvet_eclipse.compat.mixins.json"))).getAsJsonObject();
        check(config.get("priority").getAsInt()>1000,"bridge runs before LDLib default priority");
        check(config.getAsJsonArray("client").size()==2&&!config.has("mixins"),"client-only registration");
        var ldlib=read(resource("/com/lowdragmc/lowdraglib/core/mixins/ModelBakeryMixin.class"));
        var redirect=ldlib.methods.stream().filter(m->m.name.equals("injectStateToModelLocation")).findFirst().orElseThrow();
        String annotation=code(redirect);
        check(annotation.contains("Lorg/slf4j/Logger;warn"+ModelBakeryLogBridge.VARARGS),"matches original LDLib injection descriptor");
        var marker=read(resource("/com/gx/velveteclipse/compat/mixin/ModelBakeryCompatibilityMixin.class"));
        check(marker.invisibleAnnotations.stream().anyMatch(a->a.desc.equals("Lorg/spongepowered/asm/mixin/Mixin;")&&a.values.contains(2000)),"compiled mixin keeps priority 2000");
        String classPath=System.getProperty("velvet.compat.optifineClass");
        if(classPath!=null){
            byte[] raw=Files.readAllBytes(Path.of(classPath));String md5=HexFormat.of().formatHex(MessageDigest.getInstance("MD5").digest(raw));
            check(md5.equals("9fbe39ca185282a480b7997d6b0a86bb"),"matches supplied I6 pre6 patch checksum");
            var actual=read(raw);check(warnings(target(actual),ModelBakeryLogBridge.VARARGS)==0,"real OptiFine has missing LDLib target");
            var unchanged=new LinkedHashMap<String,String>();for(var m:actual.methods)if(m!=target(actual))unchanged.put(m.name+m.desc,code(m));
            new OptifineLDLibMixinPlugin().preApply(ModelBakeryLogBridge.TARGET.replace('/','.'),actual,"com.gx.velveteclipse.compat.mixin.ModelBakeryCompatibilityMixin",null);
            check(warnings(target(actual),ModelBakeryLogBridge.VARARGS)==1,"actual OptiFine now has the required redirect site");
            for(var m:actual.methods)if(m!=target(actual))check(unchanged.get(m.name+m.desc).equals(code(m)),"all other OptiFine methods unchanged: "+m.name);
            verify(actual);var writer=new ClassWriter(0);actual.accept(writer);verify(read(writer.toByteArray()));
            check(ModelBakeryLogBridge.normalize(actual)==0,"actual patch is idempotent");
            System.out.println("PASS actual OptiFine I6 pre6: original MD5 "+md5+", required LDLib redirect site 0 -> 1; other methods preserved and ASM dataflow verified.");
        }else System.out.println("Actual OptiFine class not supplied; synthetic regression and vanilla checks only.");
        System.out.println("PASS OptiFine/LDLib compatibility: "+checks+" checks. Offline; no Minecraft boot or shader rendering test.");
    }

    private static ClassNode fixture(String methodName){
        var c=new ClassNode();c.version=V17;c.access=ACC_PUBLIC;c.name=ModelBakeryLogBridge.TARGET;c.superName="java/lang/Object";
        c.fields.add(new FieldNode(ACC_PUBLIC|ACC_STATIC,"logger","Lorg/slf4j/Logger;",null,null));
        c.fields.add(new FieldNode(ACC_PUBLIC|ACC_STATIC,"second","Ljava/lang/Object;",null,null));
        var init=new MethodNode(ACC_PUBLIC,"<init>","()V",null,null);init.instructions.add(new VarInsnNode(ALOAD,0));init.instructions.add(new MethodInsnNode(INVOKESPECIAL,"java/lang/Object","<init>","()V",false));init.instructions.add(new InsnNode(RETURN));init.maxStack=1;init.maxLocals=1;c.methods.add(init);
        var m=new MethodNode(ACC_PUBLIC,methodName,MODEL_DESC,null,null);
        m.instructions.add(new FieldInsnNode(GETSTATIC,GENERATED,"logger","Lorg/slf4j/Logger;"));
        m.instructions.add(new LdcInsnNode("Unable to load model: '{}' referenced from: {}"));
        m.instructions.add(new VarInsnNode(ALOAD,1));m.instructions.add(new FieldInsnNode(GETSTATIC,GENERATED,"second","Ljava/lang/Object;"));
        m.instructions.add(new MethodInsnNode(INVOKEINTERFACE,"org/slf4j/Logger","warn",ModelBakeryLogBridge.PAIR,true));
        m.instructions.add(new InsnNode(ACONST_NULL));m.instructions.add(new InsnNode(ARETURN));m.maxStack=4;m.maxLocals=2;c.methods.add(m);return c;
    }
    private static void execute(ClassNode node,String methodName)throws Exception{
        node.name=GENERATED;var writer=new ClassWriter(0);node.accept(writer);byte[] bytes=writer.toByteArray();
        class Loader extends ClassLoader{Loader(){super(OptifineCompatAudit.class.getClassLoader());}Class<?> define(){return defineClass(GENERATED.replace('/','.'),bytes,0,bytes.length);}}
        var type=new Loader().define();var calls=new ArrayList<Object[]>();
        Object logger=Proxy.newProxyInstance(Logger.class.getClassLoader(),new Class<?>[]{Logger.class},(p,m,a)->{if(m.getName().equals("warn"))calls.add(a);return null;});
        var first=new net.minecraft.resources.ResourceLocation("velvet_eclipse","model_evoker");Object second=new Object();
        type.getField("logger").set(null,logger);type.getField("second").set(null,second);
        type.getMethod(methodName,net.minecraft.resources.ResourceLocation.class).invoke(type.getConstructor().newInstance(),first);
        check(calls.size()==1,"warning invoked exactly once");Object[] call=calls.get(0);
        check(call.length==2&&call[0].equals("Unable to load model: '{}' referenced from: {}"),"message preserved");
        var values=(Object[])call[1];check(values.length==2&&values[0]==first&&values[1]==second,"argument identities/order preserved for LDLib filter");
    }
}

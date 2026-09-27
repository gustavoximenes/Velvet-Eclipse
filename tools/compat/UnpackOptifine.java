import java.nio.file.*;
import java.util.zip.*;
import java.io.*;
public class UnpackOptifine {
 public static void main(String[] args)throws Exception {
  String name="net/minecraft/client/resources/model/ModelBakery.class";
  try(var original=new ZipFile(args[0]);var optifine=new ZipFile(args[1])){
   byte[] base=original.getInputStream(original.getEntry("fww.class")).readAllBytes();
   var out=new ByteArrayOutputStream();
   new optifine.xdelta.GDiffPatcher(base,optifine.getInputStream(optifine.getEntry("patch/srg/"+name+".xdelta")),out);
   byte[] patched=out.toByteArray();
   String md5=java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("MD5").digest(patched));
   String expected=new String(optifine.getInputStream(optifine.getEntry("patch/srg/"+name+".md5")).readAllBytes()).trim();
   if(!md5.equalsIgnoreCase(expected))throw new AssertionError("OptiFine patched class MD5 mismatch: "+md5+" vs "+expected);
   Path root=Path.of(args[2]);Files.createDirectories(root.resolve("patched/"+name).getParent());Files.createDirectories(root.resolve("original/"+name).getParent());
   Files.write(root.resolve("patched/"+name),patched);Files.write(root.resolve("original/"+name),base);
   System.out.println("Reconstructed OptiFine ModelBakery verified against patch MD5: "+md5);
  }
 }
}

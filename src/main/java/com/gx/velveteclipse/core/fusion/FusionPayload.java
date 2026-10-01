package com.gx.velveteclipse.core.fusion;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.*;

/** Repeated recipe/owner data compresses well; keep both wire and expanded sizes bounded. */
public final class FusionPayload {
 public static final int WIRE_LIMIT=1_000_000, TEXT_LIMIT=8_388_608;
 public static byte[] encode(String json){
  byte[] plain=json.getBytes(StandardCharsets.UTF_8);if(plain.length>TEXT_LIMIT)throw new IllegalArgumentException("Fusion archive too large");
  try{var bytes=new ByteArrayOutputStream();try(var zip=new GZIPOutputStream(bytes)){zip.write(plain);}byte[] result=bytes.toByteArray();if(result.length>WIRE_LIMIT)throw new IllegalArgumentException("Fusion archive exceeds packet limit");return result;}
  catch(IOException e){throw new IllegalArgumentException("Cannot encode fusion archive",e);}
 }
 public static String decode(byte[] bytes){
  if(bytes.length>WIRE_LIMIT)throw new IllegalArgumentException("Fusion archive exceeds packet limit");
  try(var zip=new GZIPInputStream(new ByteArrayInputStream(bytes))){byte[] plain=zip.readNBytes(TEXT_LIMIT+1);if(plain.length>TEXT_LIMIT)throw new IllegalArgumentException("Fusion archive too large");return new String(plain,StandardCharsets.UTF_8);}
  catch(IOException e){throw new IllegalArgumentException("Invalid fusion archive",e);}
 }
 private FusionPayload(){}
}

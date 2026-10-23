"""Two-pass alpha preview for the same glass material used by the chunk model."""
def apply(code):
 code=code.replace('static float time;', 'static float time; static int opaqueCount; static float[][] glass;')
 old='var buf=BufferUtils.createByteBuffer(bytes.length);buf.put(bytes).flip();glBindBuffer(GL_ARRAY_BUFFER,vbo);glBufferData(GL_ARRAY_BUFFER,buf,GL_STATIC_DRAW);'
 new='''var source=ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer();
  var opaque=new ArrayList<float[]>();var transparent=new ArrayList<float[]>();
  while(source.hasRemaining()){float[] tri=new float[39];source.get(tri);(java.lang.Math.round(tri[9])==18?transparent:opaque).add(tri);}
  opaqueCount=opaque.size()*3;glass=transparent.toArray(float[][]::new);
  var buf=BufferUtils.createFloatBuffer(bytes.length/4);for(var tri:opaque)buf.put(tri);for(var tri:glass)buf.put(tri);buf.flip();
  glBindBuffer(GL_ARRAY_BUFFER,vbo);glBufferData(GL_ARRAY_BUFFER,buf,GL_DYNAMIC_DRAW);'''
 assert old in code;code=code.replace(old,new)
 code=code.replace('glDrawArrays(GL_TRIANGLES,0,count);glBindFramebuffer', 'glDrawArrays(GL_TRIANGLES,0,opaqueCount);glBindFramebuffer')
 code=code.replace('frag=vec4(pow(lit,vec3(1./2.2)),1.);','frag=vec4(pow(lit,vec3(1./2.2)),mat==18?114./255.:1.);')
 old='glDrawArrays(GL_TRIANGLES,0,count);\n  glFinish();'
 new='''glDrawArrays(GL_TRIANGLES,0,opaqueCount);
  java.util.Arrays.sort(glass,java.util.Comparator.comparingDouble((float[] tri)->{
   double x=(tri[0]+tri[13]+tri[26])/3.-eye[0],y=(tri[1]+tri[14]+tri[27])/3.-eye[1],z=(tri[2]+tri[15]+tri[28])/3.-eye[2];return -(x*x+y*y+z*z);
  }));
  var alpha=BufferUtils.createFloatBuffer(glass.length*39);for(var tri:glass)alpha.put(tri);alpha.flip();
  glBufferSubData(GL_ARRAY_BUFFER,(long)opaqueCount*52,alpha);glEnable(GL_BLEND);glBlendFunc(GL_SRC_ALPHA,GL_ONE_MINUS_SRC_ALPHA);glDepthMask(false);
  glDrawArrays(GL_TRIANGLES,opaqueCount,count-opaqueCount);glDepthMask(true);glDisable(GL_BLEND);
  glFinish();'''
 assert old in code;return code.replace(old,new)

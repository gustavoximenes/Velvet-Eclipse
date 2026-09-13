import java.nio.*;
import java.nio.file.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33.*;

/** Optional GPU smoke test. Uses installed LWJGL 3.3.1; creates only a hidden window. */
public final class ShaderProbe {
 private static int compile(int kind,Path path)throws Exception {
  int shader=glCreateShader(kind);glShaderSource(shader,Files.readString(path));glCompileShader(shader);
  if(glGetShaderi(shader,GL_COMPILE_STATUS)==0)throw new AssertionError(path+": "+glGetShaderInfoLog(shader));return shader;
 }
 public static void main(String[] args)throws Exception {
  if(!glfwInit())throw new AssertionError("GLFW unavailable");long window=0;
  try {
   glfwWindowHint(GLFW_VISIBLE,GLFW_FALSE);glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR,3);glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR,2);glfwWindowHint(GLFW_OPENGL_PROFILE,GLFW_OPENGL_CORE_PROFILE);
   window=glfwCreateWindow(256,256,"Velvet shader validation",0,0);if(window==0)throw new AssertionError("Hidden GL context unavailable");glfwMakeContextCurrent(window);GL.createCapabilities();
   Path source=Path.of(args[0]);int vertex=compile(GL_VERTEX_SHADER,source.resolve("spell_glow.vsh")),fragment=compile(GL_FRAGMENT_SHADER,source.resolve("spell_glow.fsh"));int program=glCreateProgram();glAttachShader(program,vertex);glAttachShader(program,fragment);
   glBindAttribLocation(program,0,"Position");glBindAttribLocation(program,1,"Color");glBindAttribLocation(program,2,"UV0");glLinkProgram(program);if(glGetProgrami(program,GL_LINK_STATUS)==0)throw new AssertionError(glGetProgramInfoLog(program));glUseProgram(program);
   float[] identity={1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1};for(String name:new String[]{"ModelViewMat","ProjMat"})glUniformMatrix4fv(glGetUniformLocation(program,name),false,identity);glUniform4f(glGetUniformLocation(program,"ColorModulator"),1,1,1,1);
   float[] quad={-.8f,-.8f,0,.68f,.45f,.97f,1,0,0, .8f,-.8f,0,.68f,.45f,.97f,1,1,0, .8f,.8f,0,.68f,.45f,.97f,1,1,1, -.8f,.8f,0,.68f,.45f,.97f,1,0,1};
   int vao=glGenVertexArrays(),vbo=glGenBuffers(),ebo=glGenBuffers();glBindVertexArray(vao);glBindBuffer(GL_ARRAY_BUFFER,vbo);glBufferData(GL_ARRAY_BUFFER,quad,GL_STATIC_DRAW);glBindBuffer(GL_ELEMENT_ARRAY_BUFFER,ebo);glBufferData(GL_ELEMENT_ARRAY_BUFFER,new int[]{0,1,2,0,2,3},GL_STATIC_DRAW);
   glVertexAttribPointer(0,3,GL_FLOAT,false,36,0);glVertexAttribPointer(1,4,GL_FLOAT,false,36,12);glVertexAttribPointer(2,2,GL_FLOAT,false,36,28);for(int i=0;i<3;i++)glEnableVertexAttribArray(i);
   glViewport(0,0,256,256);glClearColor(.012f,.047f,.21f,1);glClear(GL_COLOR_BUFFER_BIT);glEnable(GL_BLEND);glBlendFuncSeparate(GL_SRC_ALPHA,GL_ONE,GL_ONE,GL_ZERO);glDrawElements(GL_TRIANGLES,6,GL_UNSIGNED_INT,0);glFinish();
   int error=glGetError();if(error!=GL_NO_ERROR)throw new AssertionError("GL error "+error);
   ByteBuffer bytes=BufferUtils.createByteBuffer(256*256*4);glReadPixels(0,0,256,256,GL_RGBA,GL_UNSIGNED_BYTE,bytes);var image=new BufferedImage(256,256,BufferedImage.TYPE_INT_RGB);
   for(int y=0;y<256;y++)for(int x=0;x<256;x++){int i=(y*256+x)*4;image.setRGB(x,255-y,((bytes.get(i)&255)<<16)|((bytes.get(i+1)&255)<<8)|(bytes.get(i+2)&255));}
   int center=image.getRGB(128,128)>>16&255,halo=image.getRGB(70,128)>>16&255,edge=image.getRGB(26,128)>>16&255;
   if(!(center>200&&center>halo&&halo>edge+10&&edge<12))throw new AssertionError("Feathered glow invalid: "+center+"/"+halo+"/"+edge);
   ImageIO.write(image,"png",Path.of(args[1]).toFile());System.out.println("PASS GPU shader: GLSL compile/link, attributes, uniforms, additive blend, bright core and transparent edge; "+glGetString(GL_RENDERER));
   glDeleteProgram(program);glDeleteShader(vertex);glDeleteShader(fragment);glDeleteBuffers(vbo);glDeleteBuffers(ebo);glDeleteVertexArrays(vao);
  }finally{if(window!=0)glfwDestroyWindow(window);glfwTerminate();}
 }
}

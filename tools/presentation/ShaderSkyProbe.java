import java.nio.*;
import java.nio.file.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33.*;

/** Invisible standalone OpenGL test. Does not start Minecraft or change shader packs. */
public final class ShaderSkyProbe {
 static int compile(int type,Path source)throws Exception{int id=glCreateShader(type);glShaderSource(id,Files.readString(source));glCompileShader(id);if(glGetShaderi(id,GL_COMPILE_STATUS)==0)throw new AssertionError(glGetShaderInfoLog(id));return id;}
 public static void main(String[] args)throws Exception{
  if(!glfwInit())throw new AssertionError("GLFW unavailable");glfwWindowHint(GLFW_VISIBLE,GLFW_FALSE);glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR,3);glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR,3);glfwWindowHint(GLFW_OPENGL_PROFILE,GLFW_OPENGL_CORE_PROFILE);
  long window=glfwCreateWindow(800,450,"Velvet offline shader probe",0,0);if(window==0)throw new AssertionError("Hidden GL context unavailable");glfwMakeContextCurrent(window);GL.createCapabilities();
  try{
   var source=Path.of(args[0]);var out=Path.of(args[1]);Files.createDirectories(out);int v=compile(GL_VERTEX_SHADER,source.resolve("dark_hour_sky.vsh")),f=compile(GL_FRAGMENT_SHADER,source.resolve("dark_hour_sky.fsh")),program=glCreateProgram();glAttachShader(program,v);glAttachShader(program,f);glLinkProgram(program);if(glGetProgrami(program,GL_LINK_STATUS)==0)throw new AssertionError(glGetProgramInfoLog(program));glUseProgram(program);
   int vao=glGenVertexArrays();glBindVertexArray(vao);int buffer=glGenBuffers();glBindBuffer(GL_ARRAY_BUFFER,buffer);glBufferData(GL_ARRAY_BUFFER,new float[]{-1,-1,0,0,0, 1,-1,0,1,0, 1,1,0,1,1, -1,1,0,0,1},GL_STATIC_DRAW);int position=glGetAttribLocation(program,"Position"),uv=glGetAttribLocation(program,"UV0");glEnableVertexAttribArray(position);glVertexAttribPointer(position,3,GL_FLOAT,false,20,0);glEnableVertexAttribArray(uv);glVertexAttribPointer(uv,2,GL_FLOAT,false,20,12);
   int depth=glGenTextures();glActiveTexture(GL_TEXTURE0);glBindTexture(GL_TEXTURE_2D,depth);glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,GL_NEAREST);glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER,GL_NEAREST);
   float[] values=new float[800*450];for(int y=0;y<450;y++)for(int x=0;x<800;x++)values[y*800+x]=(x<180||y<90)?0.7f:1f;glTexImage2D(GL_TEXTURE_2D,0,GL_R32F,800,450,0,GL_RED,GL_FLOAT,values);glUniform1i(glGetUniformLocation(program,"SkyDepth"),0);
   var mat=BufferUtils.createFloatBuffer(16);new Matrix4f().perspective((float)Math.toRadians(70),800f/450,.05f,512).invert().get(mat);glUniformMatrix4fv(glGetUniformLocation(program,"InverseProjection"),false,mat);
   // View south and up toward the moon; the block mask should remain the clear color.
   mat.clear();new Matrix4f().rotationX((float)Math.toRadians(35)).get(mat);glUniformMatrix4fv(glGetUniformLocation(program,"InverseView"),false,mat);glUniform1f(glGetUniformLocation(program,"SkyTime"),16);
   glViewport(0,0,800,450);glDisable(GL_DEPTH_TEST);glDisable(GL_CULL_FACE);glDisable(GL_BLEND);glClearColor(.45f,.15f,.2f,1);glClear(GL_COLOR_BUFFER_BIT);glDrawArrays(GL_TRIANGLE_FAN,0,4);glFinish();var pixels=BufferUtils.createByteBuffer(800*450*4);glReadPixels(0,0,800,450,GL_RGBA,GL_UNSIGNED_BYTE,pixels);var image=new BufferedImage(800,450,BufferedImage.TYPE_INT_RGB);int moon=0,green=0,masked=0;
   for(int y=0;y<450;y++)for(int x=0;x<800;x++){int i=(y*800+x)*4,r=pixels.get(i)&255,g=pixels.get(i+1)&255,b=pixels.get(i+2)&255;image.setRGB(x,449-y,(r<<16)|(g<<8)|b);if(x<180||y<90){if(Math.abs(r-115)>2||Math.abs(g-38)>2||Math.abs(b-51)>2)throw new AssertionError("Sky overwrote terrain depth");masked++;}else {if(g>r&&g>b)green++;if(r>150&&g>150&&b>50)moon++;}}
   if(moon<500||green<10000)throw new AssertionError("Moon/green sky missing: "+moon+" / "+green);if(glGetError()!=GL_NO_ERROR)throw new AssertionError("GL error");ImageIO.write(image,"png",out.resolve("shader-sky-probe.png").toFile());System.out.println("PASS actual GLSL compile/link and depth composition: "+masked+" foreground pixels preserved; "+green+" green sky pixels; "+moon+" moon pixels. GPU: "+glGetString(GL_RENDERER)+". Not a Minecraft test.");
  }finally{glfwDestroyWindow(window);glfwTerminate();}
 }
}

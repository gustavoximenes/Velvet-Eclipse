import java.nio.*;
import java.nio.file.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33.*;

/** Hidden-context GPU check; does not launch or operate Minecraft or desktop UI. */
public final class LightingProbe {
 private static int compile(int kind,Path path)throws Exception{
  int shader=glCreateShader(kind);glShaderSource(shader,Files.readString(path));glCompileShader(shader);
  if(glGetShaderi(shader,GL_COMPILE_STATUS)==0)throw new AssertionError(glGetShaderInfoLog(shader));return shader;
 }
 public static void main(String[] args)throws Exception{
  if(!glfwInit())throw new AssertionError("GLFW unavailable");long window=0;
  try{
   glfwWindowHint(GLFW_VISIBLE,GLFW_FALSE);glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR,3);glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR,2);glfwWindowHint(GLFW_OPENGL_PROFILE,GLFW_OPENGL_CORE_PROFILE);
   window=glfwCreateWindow(256,128,"Velvet lighting validation",0,0);if(window==0)throw new AssertionError("Hidden GPU context unavailable");
   glfwMakeContextCurrent(window);GL.createCapabilities();Path source=Path.of(args[0]);
   int vs=compile(GL_VERTEX_SHADER,source.resolve("velvet_room_light.vsh")),fs=compile(GL_FRAGMENT_SHADER,source.resolve("velvet_room_light.fsh"));
   int program=glCreateProgram();glAttachShader(program,vs);glAttachShader(program,fs);glBindAttribLocation(program,0,"Position");glBindAttribLocation(program,1,"UV0");glLinkProgram(program);
   if(glGetProgrami(program,GL_LINK_STATUS)==0)throw new AssertionError(glGetProgramInfoLog(program));glUseProgram(program);glUniform1i(glGetUniformLocation(program,"RoomColor"),0);
   ByteBuffer input=BufferUtils.createByteBuffer(256*128*4);
   for(int y=0;y<128;y++)for(int x=0;x<256;x++){int r=x,g=y<64?x:x/2,b=y<64?x:Math.min(255,x*2);input.put((byte)r).put((byte)g).put((byte)b).put((byte)255);}input.flip();
   int texture=glGenTextures();glActiveTexture(GL_TEXTURE0);glBindTexture(GL_TEXTURE_2D,texture);glTexImage2D(GL_TEXTURE_2D,0,GL_RGBA8,256,128,0,GL_RGBA,GL_UNSIGNED_BYTE,input);glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,GL_NEAREST);glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER,GL_NEAREST);
   int vao=glGenVertexArrays(),vbo=glGenBuffers(),ebo=glGenBuffers();glBindVertexArray(vao);glBindBuffer(GL_ARRAY_BUFFER,vbo);
   glBufferData(GL_ARRAY_BUFFER,new float[]{-1,-1,0,0,0,1,-1,0,1,0,1,1,0,1,1,-1,1,0,0,1},GL_STATIC_DRAW);
   glBindBuffer(GL_ELEMENT_ARRAY_BUFFER,ebo);glBufferData(GL_ELEMENT_ARRAY_BUFFER,new int[]{0,1,2,0,2,3},GL_STATIC_DRAW);
   glVertexAttribPointer(0,3,GL_FLOAT,false,20,0);glVertexAttribPointer(1,2,GL_FLOAT,false,20,12);glEnableVertexAttribArray(0);glEnableVertexAttribArray(1);
   glViewport(0,0,256,128);glDisable(GL_DEPTH_TEST);glDisable(GL_BLEND);glDisable(GL_FRAMEBUFFER_SRGB);glDrawElements(GL_TRIANGLES,6,GL_UNSIGNED_INT,0);glFinish();
   ByteBuffer pixels=BufferUtils.createByteBuffer(256*128*4);glReadPixels(0,0,256,128,GL_RGBA,GL_UNSIGNED_BYTE,pixels);
   if(glGetError()!=GL_NO_ERROR)throw new AssertionError("GL rendering error");var image=new BufferedImage(512,128,BufferedImage.TYPE_INT_RGB);int checks=0;
   for(int y=0;y<128;y++)for(int x=0;x<256;x++){
    int offset=(y*256+x)*4,r=input.get(offset)&255,g=input.get(offset+1)&255,b=input.get(offset+2)&255;
    double l=(r*.2126+g*.7152+b*.0722)/255,s=Math.max(0,Math.min(1,(l-.06)/.29)),shadow=1-s*s*(3-2*s),target=l+(Math.max(l,Math.sqrt(l)*.60)-l)*shadow,gain=Math.min(2.5,target/Math.max(l,.0001));
    int rr=pixels.get(offset)&255,gg=pixels.get(offset+1)&255,bb=pixels.get(offset+2)&255;
    for(int c=0;c<3;c++){int expected=(int)Math.round(Math.min(255,new int[]{r,g,b}[c]*gain)),actual=new int[]{rr,gg,bb}[c];if(Math.abs(expected-actual)>2)throw new AssertionError("Pixel mismatch at "+x+","+y+": "+expected+" / "+actual);checks++;}
    image.setRGB(x,127-y,(r<<16)|(g<<8)|b);image.setRGB(x+256,127-y,(rr<<16)|(gg<<8)|bb);
   }
   ImageIO.write(image,"png",Path.of(args[1]).toFile());
   System.out.println("PASS hidden GPU: GLSL compile/link and "+checks+" pixel channels; shadow lift matches tone curve, highlights/hue preserved. "+glGetString(GL_RENDERER)+". Not an in-game shaderpack test.");
   glDeleteProgram(program);glDeleteShader(vs);glDeleteShader(fs);glDeleteTextures(texture);glDeleteBuffers(vbo);glDeleteBuffers(ebo);glDeleteVertexArrays(vao);
  }finally{if(window!=0)glfwDestroyWindow(window);glfwTerminate();}
 }
}

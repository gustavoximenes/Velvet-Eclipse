import java.nio.*;import java.nio.file.*;import java.util.*;
import java.awt.image.BufferedImage;import javax.imageio.ImageIO;
import com.google.gson.*;import org.joml.*;import org.lwjgl.BufferUtils;import org.lwjgl.opengl.GL;
import static org.lwjgl.glfw.GLFW.*;import static org.lwjgl.opengl.GL33.*;

/** Hidden offline context. Uses exported plan models and textures, with illustrative lighting. */
public final class CityBlockPreview {
 static final int W=1280,H=720;
 static int shader(int type,String source){int s=glCreateShader(type);glShaderSource(s,source);glCompileShader(s);if(glGetShaderi(s,GL_COMPILE_STATUS)==0)throw new IllegalStateException(glGetShaderInfoLog(s));return s;}
 static Vector3f vec(JsonArray a){return new Vector3f(a.get(0).getAsFloat(),a.get(1).getAsFloat(),a.get(2).getAsFloat());}
 public static void main(String[] args)throws Exception{
  Path dir=Path.of(args[0]);var meta=JsonParser.parseString(Files.readString(dir.resolve("scene.json"))).getAsJsonObject();
  if(!glfwInit())throw new IllegalStateException("GLFW init");long window=0;
  try{
   glfwWindowHint(GLFW_VISIBLE,GLFW_FALSE);glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR,3);glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR,3);glfwWindowHint(GLFW_OPENGL_PROFILE,GLFW_OPENGL_CORE_PROFILE);
   window=glfwCreateWindow(W,H,"Offline block city",0,0);glfwMakeContextCurrent(window);GL.createCapabilities();
   int program=glCreateProgram();glAttachShader(program,shader(GL_VERTEX_SHADER,"""
    #version 330 core
    layout(location=0) in vec3 Position;layout(location=1) in vec3 Normal;layout(location=2) in vec3 UV;
    layout(location=3) in vec3 Color;layout(location=4) in float Emission;
    uniform mat4 VP;out vec3 p;out vec3 n;out vec3 uv;out vec3 tint;out float em;
    void main(){p=Position;n=Normal;uv=UV;tint=Color;em=Emission;gl_Position=VP*vec4(Position,1);}
    """));
   glAttachShader(program,shader(GL_FRAGMENT_SHADER,"""
    #version 330 core
    in vec3 p;in vec3 n;in vec3 uv;in vec3 tint;in float em;
    uniform sampler2D Atlas;uniform float Tiles;uniform vec3 Eye;uniform float Dark;
    uniform int Count;uniform vec3 Lights[160];out vec4 color;
    void main(){
     vec4 tex=texture(Atlas,vec2((uv.z+clamp(uv.x,.007,.993))/Tiles,clamp(uv.y,.007,.993)));
     if(tex.a<.08)discard;
     vec3 normal=normalize(n);float sun=max(dot(normal,normalize(vec3(-.4,.8,.2))),0.);
     vec3 light=mix(vec3(.56)+vec3(.42)*sun,vec3(.17,.24,.22),Dark);
     for(int i=0;i<Count;i++){float d=distance(p,Lights[i]);if(d<15.)light=max(light,vec3(1.,.91,.70)*max(0.,1.-d/15.));}
     light=max(light,vec3(em));vec3 c=tex.rgb*tint*light;
     float fog=clamp(distance(Eye,p)/650.,0.,.75);c=mix(c,mix(vec3(.53,.70,.78),vec3(.025,.11,.065),Dark),fog);
     color=vec4(clamp(c,0.,1.),tex.a<.95?.30:1.);
    }
    """));glLinkProgram(program);if(glGetProgrami(program,GL_LINK_STATUS)==0)throw new IllegalStateException(glGetProgramInfoLog(program));glUseProgram(program);
   int vao=glGenVertexArrays(),vbo=glGenBuffers();glBindVertexArray(vao);glBindBuffer(GL_ARRAY_BUFFER,vbo);
   byte[] bytes=Files.readAllBytes(dir.resolve("city.bin"));var data=BufferUtils.createByteBuffer(bytes.length);data.put(bytes).flip();glBufferData(GL_ARRAY_BUFFER,data,GL_STATIC_DRAW);
   for(int i=0;i<5;i++){glEnableVertexAttribArray(i);glVertexAttribPointer(i,i==4?1:3,GL_FLOAT,false,52,i*12);}
   var im=ImageIO.read(dir.resolve("atlas.png").toFile());var pixels=BufferUtils.createByteBuffer(im.getWidth()*im.getHeight()*4);
   for(int y=0;y<im.getHeight();y++)for(int x=0;x<im.getWidth();x++){int c=im.getRGB(x,y);pixels.put((byte)(c>>16)).put((byte)(c>>8)).put((byte)c).put((byte)(c>>24));}pixels.flip();
   int texture=glGenTextures();glBindTexture(GL_TEXTURE_2D,texture);glTexImage2D(GL_TEXTURE_2D,0,GL_RGBA8,im.getWidth(),im.getHeight(),0,GL_RGBA,GL_UNSIGNED_BYTE,pixels);glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,GL_NEAREST);glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER,GL_NEAREST);
   glUniform1i(glGetUniformLocation(program,"Atlas"),0);glUniform1f(glGetUniformLocation(program,"Tiles"),meta.get("tiles").getAsFloat());
   var lights=meta.getAsJsonArray("lights");glUniform1i(glGetUniformLocation(program,"Count"),lights.size());
   for(int i=0;i<lights.size();i++){var l=vec(lights.get(i).getAsJsonArray());glUniform3f(glGetUniformLocation(program,"Lights["+i+"]"),l.x,l.y,l.z);}
   glEnable(GL_DEPTH_TEST);glDepthFunc(GL_LESS);glDisable(GL_CULL_FACE);glEnable(GL_BLEND);glBlendFunc(GL_SRC_ALPHA,GL_ONE_MINUS_SRC_ALPHA);glViewport(0,0,W,H);
   for(var item:meta.getAsJsonArray("views")){
    var v=item.getAsJsonObject();boolean dark=v.has("dark")&&v.get("dark").getAsBoolean();var eye=vec(v.getAsJsonArray("eye"));var target=vec(v.getAsJsonArray("target"));
    var vp=new Matrix4f().perspective((float)java.lang.Math.toRadians(v.get("fov").getAsFloat()),(float)W/H,.10f,800).lookAt(eye,target,new Vector3f(0,1,0));
    glUniformMatrix4fv(glGetUniformLocation(program,"VP"),false,vp.get(new float[16]));glUniform3f(glGetUniformLocation(program,"Eye"),eye.x,eye.y,eye.z);glUniform1f(glGetUniformLocation(program,"Dark"),dark?1:0);
    glClearColor(dark?.025f:.53f,dark?.11f:.7f,dark?.065f:.78f,1);glClear(GL_COLOR_BUFFER_BIT|GL_DEPTH_BUFFER_BIT);glDrawArrays(GL_TRIANGLES,0,meta.get("vertices").getAsInt());glFinish();
    var read=BufferUtils.createByteBuffer(W*H*4);glReadPixels(0,0,W,H,GL_RGBA,GL_UNSIGNED_BYTE,read);if(glGetError()!=GL_NO_ERROR)throw new IllegalStateException("Preview GL error");
    var image=new BufferedImage(W,H,BufferedImage.TYPE_INT_RGB);int[] rgb=new int[W*H];for(int y=0;y<H;y++)for(int x=0;x<W;x++){int i=(y*W+x)*4;rgb[(H-y-1)*W+x]=((read.get(i)&255)<<16)|((read.get(i+1)&255)<<8)|(read.get(i+2)&255);}image.setRGB(0,0,W,H,rgb,0,W);ImageIO.write(image,"png",dir.resolve(v.get("id").getAsString()+".png").toFile());
   }
   System.out.println("PASS hidden offline block preview: "+meta.getAsJsonArray("views").size()+" views; "+glGetString(GL_RENDERER));
  }finally{if(window!=0)glfwDestroyWindow(window);glfwTerminate();}
 }
}

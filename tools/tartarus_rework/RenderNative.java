import java.nio.*;
import java.nio.file.*;
import java.util.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import com.google.gson.*;
import org.joml.*;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33.*;

/** Production chunk geometry and materials, hidden offline OpenGL lighting probe; never launches Minecraft. */
public class RenderNative {
 static int W=1280,H=720,program,vao,vbo,count,shadowProgram,shadowFbo,shadowTex;static Matrix4f lightMatrix;static float time;
 static final String VS="""
 #version 330 core
 layout(location=0) in vec3 Position;layout(location=1) in vec3 Normal;layout(location=2) in vec3 Color;layout(location=3) in vec2 Surface;layout(location=4) in vec2 TexCoord;
 uniform mat4 VP;uniform mat4 LightMatrix;
 out vec3 p;out vec3 n;out vec3 color;out vec2 surface;out vec4 shadowPos;out vec2 uv;
 void main(){p=Position;n=Normal;color=Color;surface=Surface;uv=TexCoord;shadowPos=LightMatrix*vec4(Position,1);gl_Position=VP*vec4(Position,1);}
 """;
 static final String FS="""
 #version 330 core
 in vec3 p;in vec3 n;in vec3 color;in vec2 surface;in vec4 shadowPos;in vec2 uv;uniform sampler2DArray Materials;
 uniform vec3 Eye;uniform vec3 Key;uniform vec3 Fog;uniform int LightCount;uniform vec4 Lights[32];uniform vec3 LightColors[32];uniform sampler2D Shadow;uniform float Enhanced;uniform float Time;
 out vec4 frag;
 float hash(vec3 v){return fract(sin(dot(v,vec3(12.989,78.233,47.112)))*43758.5453);}
 float shadow(){vec3 q=shadowPos.xyz/shadowPos.w*.5+.5;if(q.z>1||q.z<0||q.x<0||q.x>1||q.y<0||q.y>1)return 1.;float s=0.;for(int x=-1;x<=1;x++)for(int y=-1;y<=1;y++){float d=texture(Shadow,q.xy+vec2(x,y)/2048.).r;s+=q.z-.0015<=d?1.:.33;}return s/9.;}
 vec3 aces(vec3 x){return clamp((x*(2.51*x+.03))/(x*(2.43*x+.59)+.14),0.,1.);}
 void main(){
  vec3 normal=normalize(n),base=pow(color*texture(Materials,vec3(uv,surface.x)).rgb,vec3(2.2));int mat=int(surface.x+.1);float emission=surface.y;vec3 v=normalize(Eye-p),key=normalize(Key-p);
  vec3 grid=floor(p*16.);float noise=hash(grid)-.5;base*=1.+noise*.065;
  if(mat==5){base*=.84+.16*sin(p.y*5.4)*sin((p.x+p.z)*5.1);emission+=.30;}
  if(mat==6){emission=1.5;}if(mat>=7){emission=.6;}
  float sh=mix(1.,shadow(),Enhanced);vec3 illum=vec3(.34,.38,.46)+vec3(.42,.49,.60)*max(dot(normal,key),0.)*sh;
  vec3 spec=vec3(0);
  for(int i=0;i<LightCount;i++){
   vec3 delta=Lights[i].xyz-p;float dist=length(delta);vec3 l=delta/max(dist,.01);float att=Lights[i].w/(1.+dist*dist*.42)*pow(max(0.,1.-dist/(Lights[i].w*1.8)),2.);float nl=max(dot(normal,l),0.);
   illum+=LightColors[i]*att*(.11+.55*nl);
   float shine=mat==3?48.:mat==1?90.:15.;float ks=mat==3?.16:mat==1?.035:.006;
   spec+=LightColors[i]*pow(max(dot(normal,normalize(l+v)),0.),shine)*ks*att;
  }
  vec3 lit=base*illum+spec*Enhanced+base*emission*1.8;
  float fog=(1.-exp(-length(Eye-p)*.006))*Enhanced;lit=mix(lit,Fog,fog*.45);
  lit=mix(clamp(lit,0.,1.),aces(lit*1.12),Enhanced);
  frag=vec4(pow(lit,vec3(1./2.2)),1.);
 }
 """;
 static int compile(int type,String text){int s=glCreateShader(type);glShaderSource(s,text);glCompileShader(s);if(glGetShaderi(s,GL_COMPILE_STATUS)==0)throw new AssertionError(glGetShaderInfoLog(s));return s;}
 static int program(String vs,String fs){int p=glCreateProgram();glAttachShader(p,compile(GL_VERTEX_SHADER,vs));glAttachShader(p,compile(GL_FRAGMENT_SHADER,fs));glLinkProgram(p);if(glGetProgrami(p,GL_LINK_STATUS)==0)throw new AssertionError(glGetProgramInfoLog(p));return p;}
 static float[] vector(JsonArray a){return new float[]{a.get(0).getAsFloat(),a.get(1).getAsFloat(),a.get(2).getAsFloat()};}
 static void matrix(int p,String name,Matrix4f m){glUniformMatrix4fv(glGetUniformLocation(p,name),false,m.get(new float[16]));}
 static void vec(String name,float[] v){glUniform3f(glGetUniformLocation(program,name),v[0],v[1],v[2]);}
 static Vector3f v(float[] p){return new Vector3f(p[0],p[1],p[2]);}
 static float[] lerp(float[] a,float[] b,float t){return new float[]{a[0]+(b[0]-a[0])*t,a[1]+(b[1]-a[1])*t,a[2]+(b[2]-a[2])*t};}
 static void load(Path base,String name)throws Exception{
  var meta=JsonParser.parseString(Files.readString(base.resolve(name+".json"))).getAsJsonObject();count=meta.get("vertexCount").getAsInt();
  byte[] bytes=Files.readAllBytes(base.resolve(name+"-render.bin"));var buf=BufferUtils.createByteBuffer(bytes.length);buf.put(bytes).flip();glBindBuffer(GL_ARRAY_BUFFER,vbo);glBufferData(GL_ARRAY_BUFFER,buf,GL_STATIC_DRAW);
  float[] key=vector(meta.getAsJsonArray("key")),center=vector(meta.getAsJsonArray("center"));
  lightMatrix=new Matrix4f().ortho(-45,45,-45,45,.2f,120).mul(new Matrix4f().lookAt(v(key),v(center),new Vector3f(0,1,0)));
  glUseProgram(shadowProgram);matrix(shadowProgram,"VP",lightMatrix);glBindFramebuffer(GL_FRAMEBUFFER,shadowFbo);glViewport(0,0,2048,2048);glClear(GL_DEPTH_BUFFER_BIT);glDrawArrays(GL_TRIANGLES,0,count);glBindFramebuffer(GL_FRAMEBUFFER,0);
  glUseProgram(program);matrix(program,"LightMatrix",lightMatrix);vec("Key",key);vec("Fog",name.equals("hall")?new float[]{.04f,.11f,.16f}:new float[]{.016f,.055f,.10f});
  var lights=meta.getAsJsonArray("lights");glUniform1i(glGetUniformLocation(program,"LightCount"),lights.size());
  for(int i=0;i<lights.size();i++){var a=lights.get(i).getAsJsonArray();glUniform4f(glGetUniformLocation(program,"Lights["+i+"]"),a.get(0).getAsFloat(),a.get(1).getAsFloat(),a.get(2).getAsFloat(),a.get(6).getAsFloat());glUniform3f(glGetUniformLocation(program,"LightColors["+i+"]"),a.get(3).getAsFloat(),a.get(4).getAsFloat(),a.get(5).getAsFloat());}
  glActiveTexture(GL_TEXTURE0);glBindTexture(GL_TEXTURE_2D,shadowTex);glUniform1i(glGetUniformLocation(program,"Shadow"),0);
 }
 static void render(float[] eye,float[] target,float fov,boolean enhanced,Path path)throws Exception{
  glViewport(0,0,W,H);glClearColor(.022f,.033f,.056f,1);glClear(GL_COLOR_BUFFER_BIT|GL_DEPTH_BUFFER_BIT);glUseProgram(program);
  var vp=new Matrix4f().perspective((float)java.lang.Math.toRadians(fov),(float)W/H,.12f,150).mul(new Matrix4f().lookAt(v(eye),v(target),new Vector3f(0,1,0)));matrix(program,"VP",vp);vec("Eye",eye);glUniform1f(glGetUniformLocation(program,"Enhanced"),enhanced?1:0);glUniform1f(glGetUniformLocation(program,"Time"),time);glDrawArrays(GL_TRIANGLES,0,count);
  glFinish();var pixels=BufferUtils.createByteBuffer(W*H*4);glReadPixels(0,0,W,H,GL_RGBA,GL_UNSIGNED_BYTE,pixels);if(glGetError()!=GL_NO_ERROR)throw new AssertionError("OpenGL error");
  var image=new BufferedImage(W,H,BufferedImage.TYPE_INT_RGB);int[] rgb=new int[W*H];for(int y=0;y<H;y++)for(int x=0;x<W;x++){int i=(y*W+x)*4;rgb[(H-y-1)*W+x]=((pixels.get(i)&255)<<16)|((pixels.get(i+1)&255)<<8)|(pixels.get(i+2)&255);}image.setRGB(0,0,W,H,rgb,0,W);
  var label=image.createGraphics();label.setColor(new java.awt.Color(5,14,28));label.fillRect(0,H-28,W,28);label.setColor(new java.awt.Color(166,225,235));label.setFont(new java.awt.Font("SansSerif",java.awt.Font.PLAIN,14));label.drawString("MALHAS DO MOD / AUDITORIA OFFLINE — iluminação aproximada; não é captura do Minecraft",12,H-9);label.dispose();ImageIO.write(image,"png",path.toFile());
 }
 public static void main(String[] args)throws Exception{
  Path base=Path.of(args[0]),out=base.resolve("frames");Files.createDirectories(out);boolean movie=args.length>1&&args[1].equals("movie");if(movie){W=960;H=540;}
  if(!glfwInit())throw new AssertionError("GLFW failed");long window=0;
  try{glfwWindowHint(GLFW_VISIBLE,GLFW_FALSE);glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR,3);glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR,3);glfwWindowHint(GLFW_OPENGL_PROFILE,GLFW_OPENGL_CORE_PROFILE);glfwWindowHint(GLFW_SAMPLES,4);window=glfwCreateWindow(W,H,"Offline Tartarus proposal",0,0);if(window==0)throw new AssertionError("Hidden context failed");glfwMakeContextCurrent(window);GL.createCapabilities();
   program=program(VS,FS);shadowProgram=program("#version 330 core\nlayout(location=0) in vec3 Position;uniform mat4 VP;void main(){gl_Position=VP*vec4(Position,1);}","#version 330 core\nvoid main(){}");
   vao=glGenVertexArrays();vbo=glGenBuffers();glBindVertexArray(vao);glBindBuffer(GL_ARRAY_BUFFER,vbo);for(int i=0;i<5;i++){glEnableVertexAttribArray(i);glVertexAttribPointer(i,i>=3?2:3,GL_FLOAT,false,52,i==4?44:i*12);}
   shadowTex=glGenTextures();glBindTexture(GL_TEXTURE_2D,shadowTex);glTexImage2D(GL_TEXTURE_2D,0,GL_DEPTH_COMPONENT24,2048,2048,0,GL_DEPTH_COMPONENT,GL_FLOAT,(ByteBuffer)null);glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,GL_NEAREST);glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER,GL_NEAREST);glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_S,GL_CLAMP_TO_EDGE);glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_T,GL_CLAMP_TO_EDGE);shadowFbo=glGenFramebuffers();glBindFramebuffer(GL_FRAMEBUFFER,shadowFbo);glFramebufferTexture2D(GL_FRAMEBUFFER,GL_DEPTH_ATTACHMENT,GL_TEXTURE_2D,shadowTex,0);glDrawBuffer(GL_NONE);glReadBuffer(GL_NONE);if(glCheckFramebufferStatus(GL_FRAMEBUFFER)!=GL_FRAMEBUFFER_COMPLETE)throw new AssertionError("Shadow FBO failed");glBindFramebuffer(GL_FRAMEBUFFER,0);
   glEnable(GL_DEPTH_TEST);glDepthFunc(GL_LEQUAL);glEnable(GL_CULL_FACE);glDisable(GL_FRAMEBUFFER_SRGB);glEnable(GL_MULTISAMPLE);

   glActiveTexture(GL_TEXTURE1);int materials=glGenTextures();glBindTexture(GL_TEXTURE_2D_ARRAY,materials);glTexImage3D(GL_TEXTURE_2D_ARRAY,0,GL_RGBA8,128,128,10,0,GL_RGBA,GL_UNSIGNED_BYTE,(ByteBuffer)null);
   for(int i=0;i<10;i++){var im=ImageIO.read(Path.of(args[1],"textures/block/tartarus_native_"+i+".png").toFile());var pix=BufferUtils.createByteBuffer(128*128*4);for(int y=0;y<128;y++)for(int x=0;x<128;x++){int argb=im.getRGB(x,y);pix.put((byte)(argb>>16)).put((byte)(argb>>8)).put((byte)argb).put((byte)255);}pix.flip();glTexSubImage3D(GL_TEXTURE_2D_ARRAY,0,0,0,i,128,128,1,GL_RGBA,GL_UNSIGNED_BYTE,pix);}
   glTexParameteri(GL_TEXTURE_2D_ARRAY,GL_TEXTURE_MIN_FILTER,GL_LINEAR);glTexParameteri(GL_TEXTURE_2D_ARRAY,GL_TEXTURE_MAG_FILTER,GL_LINEAR);glUseProgram(program);glUniform1i(glGetUniformLocation(program,"Materials"),1);

   for(String scene:List.of("floor-0","floor-7","floor-8","floor-10","floor-20","floor-25")){
    load(base,scene);var meta=JsonParser.parseString(Files.readString(base.resolve(scene+".json"))).getAsJsonObject();
    if(!movie){for(var el:meta.getAsJsonArray("views")){var a=el.getAsJsonObject();String id=a.get("id").getAsString();render(vector(a.getAsJsonArray("eye")),vector(a.getAsJsonArray("target")),a.get("fov").getAsFloat(),true,out.resolve(id+".png"));System.out.println(id);}continue;}
    for(var el:meta.getAsJsonArray("shots")){
     var shot=el.getAsJsonObject();String id=shot.get("id").getAsString();int frames=shot.get("frames").getAsInt();
     for(int i=0;i<frames;i++){
      float t=i/(float)(frames-1),e=t*t*(3-2*t);time=i/12f;
      float[] eye=lerp(vector(shot.getAsJsonArray("eye")),vector(shot.getAsJsonArray("eyeEnd")),e),target=lerp(vector(shot.getAsJsonArray("target")),vector(shot.getAsJsonArray("targetEnd")),e);
      if(shot.has("stairCamera")&&shot.get("stairCamera").getAsBoolean())eye[1]=2.8f+java.lang.Math.max(0f,java.lang.Math.min(8f,5f-eye[2]));
      render(eye,target,shot.get("fov").getAsFloat(),true,out.resolve(String.format("%s-%s-%03d.png",scene,id,i)));if(i%24==0)System.out.println(scene+" shot "+id+" frame "+i);
     }
    }
   }
   System.out.println("PASS offline architectural preview, "+glGetString(GL_RENDERER));
  }finally{if(window!=0)glfwDestroyWindow(window);glfwTerminate();}
 }
}

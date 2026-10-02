import java.nio.*;
import java.nio.file.*;
import java.io.*;
import java.util.*;
import com.google.gson.*;
import java.util.zip.GZIPInputStream;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33.*;

/** Hidden OpenGL audit of production meshes and shader. No Minecraft or desktop interaction. */
public final class MenuGpuProbe {
    static final int W=1280,H=720;
    static int compile(int kind,Path path)throws Exception{int s=glCreateShader(kind);glShaderSource(s,Files.readString(path));glCompileShader(s);if(glGetShaderi(s,GL_COMPILE_STATUS)==0)throw new AssertionError(glGetShaderInfoLog(s));return s;}
    static int texture(Path file)throws Exception{var im=ImageIO.read(file.toFile());var b=BufferUtils.createByteBuffer(im.getWidth()*im.getHeight()*4);for(int y=0;y<im.getHeight();y++)for(int x=0;x<im.getWidth();x++){int c=im.getRGB(x,y);b.put((byte)(c>>16)).put((byte)(c>>8)).put((byte)c).put((byte)(c>>24));}b.flip();int t=glGenTextures();glBindTexture(GL_TEXTURE_2D,t);glTexImage2D(GL_TEXTURE_2D,0,GL_RGBA8,im.getWidth(),im.getHeight(),0,GL_RGBA,GL_UNSIGNED_BYTE,b);glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,GL_NEAREST);glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER,GL_NEAREST);return t;}
    public static void main(String[] args)throws Exception{
        if(!glfwInit())throw new AssertionError("No GLFW");long window=0;
        try{
            glfwWindowHint(GLFW_VISIBLE,GLFW_FALSE);glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR,3);glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR,2);glfwWindowHint(GLFW_OPENGL_PROFILE,GLFW_OPENGL_CORE_PROFILE);
            window=glfwCreateWindow(W,H,"Offline menu audit",0,0);if(window==0)throw new AssertionError("No hidden context");glfwMakeContextCurrent(window);GL.createCapabilities();
            Path assets=Path.of(args[0]),out=Path.of(args[2]);Files.createDirectories(out);
            int program=glCreateProgram();glAttachShader(program,compile(GL_VERTEX_SHADER,assets.resolve("shaders/core/menu_blue.vsh")));glAttachShader(program,compile(GL_FRAGMENT_SHADER,assets.resolve("shaders/core/menu_blue.fsh")));glBindAttribLocation(program,0,"Position");glBindAttribLocation(program,1,"UV0");glBindAttribLocation(program,2,"Color");glLinkProgram(program);if(glGetProgrami(program,GL_LINK_STATUS)==0)throw new AssertionError(glGetProgramInfoLog(program));glUseProgram(program);
            float[] identity={1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1};glUniformMatrix4fv(glGetUniformLocation(program,"ModelViewMat"),false,identity);glUniformMatrix4fv(glGetUniformLocation(program,"ProjMat"),false,identity);glUniform1i(glGetUniformLocation(program,"Sampler0"),0);
            int[] textures={texture(Path.of(args[1])),texture(assets.resolve("textures/entity/evoker_fx.png"))};int vao=glGenVertexArrays(),vbo=glGenBuffers();glBindVertexArray(vao);glBindBuffer(GL_ARRAY_BUFFER,vbo);glVertexAttribPointer(0,3,GL_FLOAT,false,36,0);glVertexAttribPointer(1,2,GL_FLOAT,false,36,12);glVertexAttribPointer(2,4,GL_FLOAT,false,36,20);for(int i=0;i<3;i++)glEnableVertexAttribArray(i);
            glViewport(0,0,W,H);glDisable(GL_CULL_FACE);glDisable(GL_FRAMEBUFFER_SRGB);glEnable(GL_DEPTH_TEST);glDepthFunc(GL_LEQUAL);glEnable(GL_BLEND);glBlendFuncSeparate(GL_SRC_ALPHA,GL_ONE_MINUS_SRC_ALPHA,GL_ONE,GL_ONE_MINUS_SRC_ALPHA);
            var openings=new LinkedHashMap<String,JsonObject>();
            if(args.length>3&&args[3].equals("opening"))for(var element:JsonParser.parseString(Files.readString(out.resolve("opening-poses.json"))).getAsJsonArray()){var p=element.getAsJsonObject();openings.put(p.get("name").getAsString(),p);}
            for(String name:openings.isEmpty()?List.of("main","skill","item","equip","stats","system"):openings.keySet()){
                boolean opening=openings.containsKey(name);
                String mesh=opening?"main":name.equals("stats")?"status":name;byte[] binding;float[] frame;int faces;
                try(var in=new DataInputStream(new GZIPInputStream(Files.newInputStream(assets.resolve("menu/"+mesh+"_slim.bin.gz"))))){in.readInt();in.readInt();faces=in.readInt();in.readInt();binding=in.readNBytes(faces);in.skipNBytes(faces*28*4);frame=new float[faces*28];for(int i=0;i<frame.length;i++)frame[i]=in.readFloat();}
                double angle,scale,cx,cy,opacity=1,sy;
                if(name.equals("main")||opening){angle=211;scale=740./440;sy=1076./640;cx=305;cy=252;if(opening){var p=openings.get(name);angle=p.get("angle").getAsDouble();cx=p.get("x").getAsDouble();cy=p.get("y").getAsDouble();}}
                else{
                    double hx,hy,ax,ay;
                    if(name.equals("skill")){hx=701;hy=542;ax=223.49111015584333;ay=166.01672493254813;scale=1.42;angle=184;}
                    else if(name.equals("item")){hx=656;hy=278;ax=196.6336412117185;ay=242.13582982261292;scale=1.75;angle=34;opacity=.62;}
                    else if(name.equals("equip")){hx=842;hy=570;ax=211.8240629945809;ay=167.8552019571583;scale=1.58;angle=254;}
                    else{hx=1030;hy=255;ax=220;ay=220;scale=1.5;angle=name.equals("stats")?-5:185;}
                    double r=Math.toRadians(angle),dx=(ax-220)*scale,dy=(ay-320)*scale;cx=hx-dx*Math.cos(r)+dy*Math.sin(r);cy=hy-dx*Math.sin(r)-dy*Math.cos(r);sy=scale;
                }
                double a=Math.toRadians(angle);glClearColor(0,0,0,0);glClear(GL_COLOR_BUFFER_BIT|GL_DEPTH_BUFFER_BIT);glUniform4f(glGetUniformLocation(program,"ColorModulator"),1,1,1,(float)opacity);
                glDepthMask(true);
                for(int pass=0;pass<(opacity<1?2:1);pass++){
                boolean depthOnly=opacity<1&&pass==0;glColorMask(!depthOnly,!depthOnly,!depthOnly,!depthOnly);glDepthFunc(opacity<1&&pass==1?GL_EQUAL:GL_LEQUAL);glDepthMask(depthOnly||opacity==1);
                for(int tex=0;tex<2;tex++){
                    var vertices=new ArrayList<Float>();for(int face=0;face<faces;face++)if(binding[face]==tex)for(int corner:new int[]{0,1,2,0,2,3}){
                        int ix=(face*4+corner)*7;double x=(frame[ix]-220)*scale,y=(frame[ix+1]-320)*sy,px=cx+x*Math.cos(a)-y*Math.sin(a),py=cy+x*Math.sin(a)+y*Math.cos(a);if(name.equals("main")||opening)px=480-px;
                        float[] vertex={(float)(px/W*2-1),(float)(1-py/H*2),frame[ix+2]/20,frame[ix+3],frame[ix+4],Math.round(frame[ix+5]*127.5f)/255f,Math.round(frame[ix+6]*2040)/255f,0,1};for(float v:vertex)vertices.add(v);
                    }
                    if(vertices.isEmpty())continue;float[] buffer=new float[vertices.size()];for(int i=0;i<buffer.length;i++)buffer[i]=vertices.get(i);glBufferData(GL_ARRAY_BUFFER,buffer,GL_STREAM_DRAW);glBindTexture(GL_TEXTURE_2D,textures[tex]);glDrawArrays(GL_TRIANGLES,0,buffer.length/9);
                }
                }
                glDepthMask(true);glColorMask(true,true,true,true);glDepthFunc(GL_LEQUAL);
                glFinish();var pixels=BufferUtils.createByteBuffer(W*H*4);glReadPixels(0,0,W,H,GL_RGBA,GL_UNSIGNED_BYTE,pixels);if(glGetError()!=GL_NO_ERROR)throw new AssertionError("GL error");
                var pose=new BufferedImage(W,H,BufferedImage.TYPE_INT_ARGB);int count=0;for(int y=0;y<H;y++)for(int x=0;x<W;x++){int i=(y*W+x)*4,r=pixels.get(i)&255,g=pixels.get(i+1)&255,b=pixels.get(i+2)&255,alpha=pixels.get(i+3)&255;if(alpha>20)count++;if(alpha>0&&alpha<255){r=Math.min(255,r*255/alpha);g=Math.min(255,g*255/alpha);b=Math.min(255,b*255/alpha);}pose.setRGB(x,H-1-y,(alpha<<24)|(r<<16)|(g<<8)|b);}
                if(!opening&&count<10000)throw new AssertionError("Portrait absent: "+name);ImageIO.write(pose,"png",out.resolve(name+"-pose.png").toFile());
            }
            try(var files=Files.list(out)){for(Path bg:files.filter(p->p.getFileName().toString().endsWith("-background.png")).toList()){String name=bg.getFileName().toString().replace("-background.png","");var im=ImageIO.read(bg.toFile());var g=im.createGraphics();Path pose=out.resolve(name+"-pose.png");if(Files.exists(pose))g.drawImage(ImageIO.read(pose.toFile()),0,0,null);g.drawImage(ImageIO.read(out.resolve(name+"-foreground.png").toFile()),0,0,null);g.dispose();ImageIO.write(im,"png",out.resolve(name+"-native.png").toFile());}}
            System.out.println("PASS hidden OpenGL: production blue shader compiled/linked; production meshes with live texture binding rendered and composited with native layout layers. "+glGetString(GL_RENDERER)+". Offline audit, not in-game validation.");
        }finally{if(window!=0)glfwDestroyWindow(window);glfwTerminate();}
    }
}

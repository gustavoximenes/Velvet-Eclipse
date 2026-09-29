package com.gx.velveteclipse.tests;
import com.gx.velveteclipse.client.*;
import com.gx.velveteclipse.client.presentation.*;
import com.gx.velveteclipse.client.vfx.PhotonEffectPlan;
import com.gx.velveteclipse.core.persona.Element;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.List;
import javax.imageio.ImageIO;

/** Paints the production geometry. Background and portrait slots are explicitly illustrative. */
public final class ReloadPresentationPreview {
 private static Font body,display;
 public static void main(String[] args)throws Exception{
  Path out=Path.of("build/reports/reload");Files.createDirectories(out);
  body=Font.createFont(Font.TRUETYPE_FONT,Path.of("src/main/resources/assets/velvet_eclipse/font/body.ttf").toFile());display=Font.createFont(Font.TRUETYPE_FONT,Path.of("src/main/resources/assets/velvet_eclipse/font/display.ttf").toFile());
  hud(out,1280,720);hud(out,960,720);hud(out,1720,720);effects(out);
 }
 private static void hud(Path out,int width,int height)throws Exception{
  var image=new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);var g=image.createGraphics();g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);g.scale(2,2);int w=width/2,h=height/2;
  g.setPaint(new GradientPaint(0,0,new Color(0x051420),w,h,new Color(0x173D50)));g.fillRect(0,0,w,h);
  g.setColor(new Color(0x224F57));for(int x=-500;x<w+500;x+=65)g.drawLine(w/2,80,x,h);for(int y=h/2;y<h;y+=20)g.drawLine(0,y,w,y);
  HudDesign.Canvas canvas=new Painter(g);
  var v=new HudDesign.View(true,true,false,"Orpheus",103,120,42,45,"Cowardly Maya",64,85,.85,.93,.75,.8,"","MAIN",List.of(),List.of("1","2","3","4","5","6"),"ESC",0,false,0,"");
  ReloadBattleDesign.commandsBackdrop(canvas,v,w,h);var rects=BattleMenuLayout.main(w,h);for(int i=0;i<rects.length;i++)ReloadBattleDesign.command(canvas,v,i,rects[i],i==2);
  for(int i=0;i<5;i++){var r=ReloadBattleDesign.party(w,h,5,i);double u=r.w()/78.;ReloadBattleDesign.partyCard(canvas,r,new String[]{"Gustavo","Alice","Bruno","Carla","Diego"}[i],new int[]{231,182,317,95,210}[i],new int[]{42,71,28,60,91}[i],i==0,false);
   int size=(int)(30*u),x=r.x()+(int)(7*u),y=r.y()-(int)(7*u);canvas.fill(x,y,size,size,0xFF163775);canvas.styled("SKIN",x+3,y+size/2-4,(float)(.6*u),0xFF72E7FF);
   for(int n=0;n<2;n++){var transform=g.getTransform();int bx=r.x()+(int)(7*u),by=r.y()+(int)((n==0?37:46)*u),bw=(int)(61*u);g.rotate(Math.toRadians(-9),bx,by);canvas.fill(bx,by,bw,Math.max(2,(int)(3*u)),0xFF10253F);canvas.fill(bx,by,(int)(bw*(n==0?.9-i*.1:.45+i*.08)),Math.max(2,(int)(3*u)),n==0?0xFFB9FF65:0xFF4CE8FF);g.setTransform(transform);}
  }
  canvas.styled("PRÉVIA OFFLINE • LAYOUT REAL • CENÁRIO E RETRATOS ILUSTRATIVOS",10,13,.75f,0xFF9FC1CC);
  g.dispose();ImageIO.write(image,"PNG",out.resolve("hud-"+width+"x"+height+".png").toFile());
 }
 private static final class Painter implements HudDesign.Canvas{
  final Graphics2D g;Painter(Graphics2D g){this.g=g;}
  public void fill(int x,int y,int w,int h,int color){g.setColor(new Color(color,true));g.fillRect(x,y,w,h);}
  public int width(String text){g.setFont(body.deriveFont(9f));return g.getFontMetrics().stringWidth(text);}
  public void text(String text,int x,int y,int color){g.setFont(body.deriveFont(9f));g.setColor(new Color(color,true));g.drawString(text,x,y+8);}
  public void styled(String text,int x,int y,float size,int color){g.setFont(display.deriveFont(11*size));g.setColor(new Color(color,true));g.drawString(text,x,y+9*size);}
  public void angled(String text,int x,int y,float size,int color,float degrees){AffineTransform old=g.getTransform();g.rotate(Math.toRadians(degrees),x,y);styled(text,x,y,size,color);g.setTransform(old);}
 }
 private static void effects(Path out)throws Exception{
  var image=new BufferedImage(1200,900,BufferedImage.TYPE_INT_RGB);var g=image.createGraphics();g.setColor(new Color(0x061323));g.fillRect(0,0,1200,900);g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
  var elements=List.of(Element.FIRE,Element.ICE,Element.ELECTRIC,Element.DARK,Element.WIND,Element.LIGHT,Element.NUCLEAR,Element.PSY,Element.ALMIGHTY,Element.HEALING,Element.SUPPORT,Element.SLASH);
  for(int i=0;i<elements.size();i++){int ox=(i%4)*300+150,oy=(i/4)*290+260;var p=PhotonEffectPlan.create(elements.get(i),1,41);
   g.setColor(new Color(0x1A364B));g.drawOval(ox-83,oy-26,166,50);
   for(var b:p.beams()){int ax=ox+(int)((b.from().x()+b.from().z()*.35)*47),ay=oy-(int)((b.from().y()+b.from().z()*.22)*39),bx=ox+(int)((b.to().x()+b.to().z()*.35)*47),by=oy-(int)((b.to().y()+b.to().z()*.22)*39);for(int glow=3;glow>=1;glow--){g.setStroke(new BasicStroke(Math.max(1,b.width()*45*glow),BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));g.setColor(new Color((b.color()&0xFFFFFF)|((glow==1?240:35)<<24),true));g.drawLine(ax,ay,bx,by);}}
   g.setFont(display.deriveFont(20f));g.setColor(Color.WHITE);g.drawString(elements.get(i).name(),ox-120,oy-224);
  }
  g.setFont(body.deriveFont(16f));g.setColor(new Color(0x8BBDD2));g.drawString("PRÉVIA OFFLINE • GEOMETRIA DOS EMISSORES • GLOW ILUSTRATIVO, SEM CAPTURA DO PHOTON",22,885);g.dispose();ImageIO.write(image,"PNG",out.resolve("vfx-geometry.png").toFile());
 }
}

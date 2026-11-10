package com.gx.velveteclipse.exploration;

import com.gx.velveteclipse.client.exploration.EntryBannerGeometry;
import com.google.gson.Gson;
import java.nio.file.*;
import java.util.*;

/** Project the very same quad/UV used by the world renderer from all camera sides. */
public final class EntryBannerAudit {
    static int checks;
    static void check(boolean value, String why) { checks++; if (!value) throw new AssertionError(why); }
    static double[] project(EntryBannerGeometry.Vertex p, double cx, double cy, double cz,
                            double ex, double ey, double ez) {
        double fx=cx-ex, fy=cy-ey, fz=cz-ez, h=Math.hypot(fx,fz), len=Math.sqrt(h*h+fy*fy);
        double rx=-fz/h, rz=fx/h;
        fx/=len; fy/=len; fz/=len;
        double ux=-rz*fy, uy=rz*fx-rx*fz, uz=rx*fy;
        double x=p.x()-ex,y=p.y()-ey,z=p.z()-ez, depth=x*fx+y*fy+z*fz;
        check(depth>0,"in front of camera");
        return new double[]{(x*rx+z*rz)/depth,-(x*ux+y*uy+z*uz)/depth};
    }
    public static void main(String[] args) throws Exception {
        var examples=new ArrayList<Object>();
        for(double coordinate:new double[]{0,20000,-20000}) for(int angle=0;angle<360;angle+=5)
        for(double height:new double[]{1.6,3.8,8}) for(double playerYaw:new double[]{0,90,180,270}) {
            double x=coordinate+3,y=64.045,z=coordinate-7,a=Math.toRadians(angle);
            double ex=x+9*Math.sin(a),ez=z-9*Math.cos(a),ey=y+height;
            var quad=EntryBannerGeometry.quad(x,y,z,ex,ez,playerYaw);
            double[][] screen=Arrays.stream(quad).map(v->project(v,x,y,z,ex,ey,ez)).toArray(double[][]::new);
            check(screen[0][0]<screen[3][0]&&screen[1][0]<screen[2][0],"U increases left to right (no mirror)");
            check(screen[0][1]<screen[1][1]&&screen[3][1]<screen[2][1],"V increases top to bottom (upright)");
            double ax=quad[1].x()-quad[0].x(),az=quad[1].z()-quad[0].z();
            double bx=quad[2].x()-quad[0].x(),bz=quad[2].z()-quad[0].z();
            check(az*bx-ax*bz>0,"upward winding, not culled from above");
            for(var v:quad)check(v.y()==y,"decal stays on floor");
            if(coordinate==0&&height==3.8&&playerYaw==0&&angle%90==0)
                examples.add(Map.of("camera_angle",angle,"quad",quad,"screen",screen,"camera",new double[]{ex,ey,ez},"center",new double[]{x,y,z}));
        }
        for(int yaw=0;yaw<360;yaw+=5) for(var v:EntryBannerGeometry.quad(0,0,0,0,0,yaw))
            check(Double.isFinite(v.x())&&Double.isFinite(v.z()),"directly above fallback");
        Path out=Path.of("build/reports/entry-banner");Files.createDirectories(out);
        Files.writeString(out.resolve("geometry.json"),new Gson().toJson(examples));
        Files.writeString(out.resolve("validation.json"),new Gson().toJson(Map.of("status","passed","checks",checks,"camera_angles",72,"heights",3,"player_headings",4,"offline",true)));
        System.out.println("PASS entry floor banner: "+checks+" projection/winding checks; all camera sides, four independent player headings, far-world coordinates. Offline only.");
    }
}

package com.gx.velveteclipse.city;

import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.*;
import net.minecraft.world.phys.*;

/** Load the destination before testing its real floors, including fractional architecture. */
public final class CityArrival {
    public static Vec3 find(double x,double y,double z,int radius,Predicate<AABB> clear){
        for(int step=0;step<=32;step++)for(int sign:new int[]{1,-1}){
            if(step==0&&sign<0||sign<0&&step>8)continue;
            for(int r=0;r<=radius;r++)for(int dx=-r;dx<=r;dx++)for(int dz=-r;dz<=r;dz++){
                if(Math.max(Math.abs(dx),Math.abs(dz))!=r)continue;
                double h=y+sign*step/8.;var box=new AABB(x+dx-.3,h+.002,z+dz-.3,x+dx+.3,h+1.802,z+dz+.3);
                if(clear.test(box)&&!clear.test(box.m_82386_(0,-.126,0)))return new Vec3(x+dx,h+.003,z+dz);
            }
        }
        return null;
    }
    public static Vec3 find(ServerLevel level,ServerPlayer p,double x,double y,double z,int radius){
        int loX=(int)Math.floor(x-radius-1)>>4,hiX=(int)Math.floor(x+radius+1)>>4;
        int loZ=(int)Math.floor(z-radius-1)>>4,hiZ=(int)Math.floor(z+radius+1)>>4;
        for(int cx=loX;cx<=hiX;cx++)for(int cz=loZ;cz<=hiZ;cz++)level.m_6325_(cx,cz);
        return find(x,y,z,radius,b->level.m_6857_().m_61937_(BlockPos.m_274561_(b.f_82288_,b.f_82289_,b.f_82290_))&&level.m_45756_(p,b));
    }
    private CityArrival(){}
}

package com.gx.velveteclipse.client.menu;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/** GUI coordinates point down; front faces therefore have negative signed area. */
public final class MenuGeometry {
    private MenuGeometry(){}
    public static void draw(VertexConsumer out,Matrix4f pose,double[] points,int color){
        if((color>>>24)==0||points.length<6||points.length%2!=0)return;
        int count=points.length/2;float[] vertices=new float[count*3];var v=new Vector3f();
        for(int i=0;i<count;i++){
            pose.transformPosition((float)points[i*2],(float)points[i*2+1],0,v);
            if(!Float.isFinite(v.x+v.y+v.z))return;
            vertices[i*3]=v.x;vertices[i*3+1]=v.y;vertices[i*3+2]=v.z;
        }
        for(int i=1;i+1<count;i++){
            int b=i,c=i+1;
            double area=(vertices[b*3]-vertices[0])*(double)(vertices[c*3+1]-vertices[1])
                -(vertices[b*3+1]-vertices[1])*(double)(vertices[c*3]-vertices[0]);
            if(Math.abs(area)<1e-7)continue;
            if(area>0){b=i+1;c=i;}
            // QUADS indexes (0,1,2) and (2,3,0): repeating vertex 2 leaves one visible triangle.
            vertex(out,vertices,0,color);vertex(out,vertices,b,color);vertex(out,vertices,c,color);vertex(out,vertices,c,color);
        }
    }
    private static void vertex(VertexConsumer out,float[] p,int index,int color){
        int at=index*3;out.m_5483_(p[at],p[at+1],p[at+2]).m_6122_((color>>16)&255,(color>>8)&255,color&255,color>>>24).m_5752_();
    }
}

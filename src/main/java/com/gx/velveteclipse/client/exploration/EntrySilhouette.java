package com.gx.velveteclipse.client.exploration;

import com.mojang.blaze3d.vertex.VertexConsumer;

/** Passes the player's real geometry to an unlit POSITION_COLOR buffer.
 * Skin UVs and lighting cannot darken the solid blue cinematic treatment. */
public final class EntrySilhouette implements VertexConsumer {
    public static final int BLUE=0x1616FF;
    private final VertexConsumer output;
    private final int alpha;
    public EntrySilhouette(VertexConsumer output,float alpha){this.output=output;this.alpha=Math.round(Math.max(0,Math.min(1,alpha))*255);}
    @Override public VertexConsumer m_5483_(double x,double y,double z){output.m_5483_(x,y,z);return this;}
    @Override public VertexConsumer m_6122_(int r,int g,int b,int a){return this;}
    @Override public VertexConsumer m_7421_(float u,float v){return this;}
    @Override public VertexConsumer m_7122_(int u,int v){return this;}
    @Override public VertexConsumer m_7120_(int u,int v){return this;}
    @Override public VertexConsumer m_5601_(float x,float y,float z){return this;}
    @Override public void m_5752_(){output.m_6122_((BLUE>>16)&255,(BLUE>>8)&255,BLUE&255,alpha);output.m_5752_();}
    @Override public void m_7404_(int r,int g,int b,int a){}
    @Override public void m_141991_(){}
}

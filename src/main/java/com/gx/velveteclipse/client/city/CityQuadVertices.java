package com.gx.velveteclipse.client.city;

/** Encodes a triangle as a four-vertex face without a collapsed edge.
 * Minecraft splits quads along 0-2; the midpoint divides the original triangle
 * into two equal, nondegenerate triangles and preserves interpolated attributes.
 */
public final class CityQuadVertices {
    public static float component(float[] raw,int at,int vertex,int component) {
        if(vertex==2)return (raw[at+13+component]+raw[at+26+component])*.5f;
        return raw[at+(vertex==3?26:vertex*13)+component];
    }
    private CityQuadVertices(){}
}

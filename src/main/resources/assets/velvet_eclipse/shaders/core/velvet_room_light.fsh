#version 150
uniform sampler2D RoomColor;
in vec2 roomUv;
out vec4 fragColor;
void main(){
    vec4 source=texture(RoomColor,roomUv);
    float luminance=dot(source.rgb,vec3(0.2126,0.7152,0.0722));
    float shadow=1.0-smoothstep(0.06,0.35,luminance);
    float target=mix(luminance,max(luminance,sqrt(luminance)*0.60),shadow);
    float gain=min(2.5,target/max(luminance,0.0001));
    // Preserve hue and black details; highlights and menus receive no added blue wash.
    fragColor=vec4(clamp(source.rgb*gain,0.0,1.0),source.a);
}

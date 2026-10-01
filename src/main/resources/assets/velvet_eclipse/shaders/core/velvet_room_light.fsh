#version 150
uniform sampler2D RoomColor;
in vec2 roomUv;
out vec4 fragColor;
void main(){
    vec4 source=texture(RoomColor,roomUv);
    // Some packs warm emissive indigo into pink. Correct only blue-dominant room
    // materials after their tone mapping; neutral highlights and warm skin stay intact.
    vec3 color=source.rgb;
    float blue=smoothstep(0.04,0.18,color.b-max(color.r,color.g));
    color.r=mix(color.r,min(color.r,color.g*0.85),blue);
    float luminance=dot(color,vec3(0.2126,0.7152,0.0722));
    float shadow=1.0-smoothstep(0.06,0.35,luminance);
    float target=mix(luminance,max(luminance,sqrt(luminance)*0.60),shadow);
    float gain=min(min(2.5,target/max(luminance,0.0001)),1.0/max(max(color.r,color.g),max(color.b,0.0001)));
    // Never clip only the blue channel: that desaturates indigo into pale violet.
    fragColor=vec4(color*gain,source.a);
}

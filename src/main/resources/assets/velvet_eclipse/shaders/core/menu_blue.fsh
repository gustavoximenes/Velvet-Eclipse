#version 150
uniform sampler2D Sampler0;
uniform vec4 ColorModulator;
in vec2 skinUv;
in vec2 lighting;
out vec4 fragColor;
vec3 blue(float x){
    vec3 a=vec3(3,9,47),b=vec3(5,22,92),c=vec3(18,85,219),d=vec3(115,206,246),e=vec3(194,237,248),f=vec3(244,254,255);
    if(x<.065)return mix(a,b,x/.065)/255.;
    if(x<.16)return mix(b,c,(x-.065)/.095)/255.;
    if(x<.30)return mix(c,d,(x-.16)/.14)/255.;
    if(x<.50)return mix(d,e,(x-.30)/.20)/255.;
    return mix(e,f,clamp((x-.50)/.50,0.,1.))/255.;
}
void main(){
    vec4 skin=texture(Sampler0,skinUv);if(skin.a<.354)discard;
    vec3 lit=clamp(skin.rgb*lighting.x*2.+vec3(55,190,235)/255.*lighting.y/8.,0.,1.);
    fragColor=vec4(blue(dot(lit,vec3(.25,.65,.10))),skin.a)*ColorModulator;
}

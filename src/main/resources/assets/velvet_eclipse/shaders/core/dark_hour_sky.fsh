#version 150
uniform sampler2D SkyDepth;
uniform mat4 InverseProjection;
uniform mat4 InverseView;
uniform float SkyTime;
in vec2 skyUV;
out vec4 fragColor;
void main() {
    // Exact far-plane depth: do not paint through terrain, leaves, glass, entities or held items.
    if (texture(SkyDepth, skyUV).r < 0.9999999) discard;
    vec4 view = InverseProjection * vec4(skyUV * 2.0 - 1.0, 1.0, 1.0);
    vec3 ray = normalize(mat3(InverseView) * (view.xyz / view.w));
    float time = SkyTime * 0.06;
    float clouds = pow(max(0.0, sin(ray.x*8.0 + ray.z*5.0 + time) + sin(ray.z*11.0 - ray.y*9.0 - time*0.5))*0.5, 2.0);
    vec3 color = vec3(0.02,0.095,0.048) + clouds*vec3(0.055,0.19,0.08) + max(ray.y,0.0)*vec3(0.0,0.035,0.01);
    vec3 moon = normalize(vec3(0.0,0.75,-0.66));
    float angular = acos(clamp(dot(ray,moon),-1.0,1.0));
    float halo = exp(-pow(angular/0.23,2.0))*0.18;
    color += halo * vec3(0.45,0.7,0.13);
    float disk = 1.0 - smoothstep(0.152,0.158,angular);
    vec3 right = vec3(1.0,0.0,0.0), up = normalize(cross(moon,right));
    vec2 uv = vec2(dot(ray,right),dot(ray,up))/0.155;
    float craters = 0.0;
    for (int i=0; i<18; i++) {
        float a=float(i)*2.399, r=sqrt((float(i)+0.5)/18.0)*0.79;
        float size=0.06+float(i%3)*0.027;
        craters += (1.0-smoothstep(size*0.7,size,length(uv-vec2(cos(a),sin(a))*r)))*0.06;
    }
    color = mix(color,vec3(0.89,0.95,0.48)-craters*vec3(0.9,0.8,0.35),disk);
    fragColor = vec4(color,1.0);
}

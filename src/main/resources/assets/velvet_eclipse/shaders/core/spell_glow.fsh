#version 150
in vec4 vertexColor;
in vec2 beamUv;
uniform vec4 ColorModulator;
out vec4 fragColor;
void main() {
    float edge = abs(beamUv.x * 2.0 - 1.0);
    float halo = pow(max(0.0, 1.0 - edge), 1.65);
    float core = 1.0 - smoothstep(0.04, 0.32, edge);
    vec3 glow = mix(vertexColor.rgb, vec3(1.0), core * 0.72);
    fragColor = vec4(glow, vertexColor.a * halo) * ColorModulator;
}

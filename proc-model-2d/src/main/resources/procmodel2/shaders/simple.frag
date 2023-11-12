#version 450

layout(location = 0) in vec3 inColor;

layout(location = 0) out vec4 outColor;

void main() {
    outColor = vec4(pow(inColor.x, 2.2), pow(inColor.y, 2.2), pow(inColor.z, 2.2), 1.0);
}

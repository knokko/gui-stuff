#version 450

layout(location = 0) in vec3 inColor;

layout(location = 0) out vec4 outColor;

float srgbToLinear(float srgbScalar) {
    // This formula came from https://en.wikipedia.org/wiki/SRGB#From_sRGB_to_CIE_XYZ
    if (srgbScalar > 0.0405) return pow((srgbScalar + 0.055) / (1.055), 2.4);
    else return srgbScalar / 12.92;
}

void main() {
    outColor = vec4(srgbToLinear(inColor.r), srgbToLinear(inColor.g), srgbToLinear(inColor.b), 1.0);
}

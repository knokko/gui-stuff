#version 450

layout(constant_id = 0) const int NUM_TEXTURES = 100;

layout(location = 0) in vec2 position;
layout(location = 1) in flat int operationIndex;
layout(location = 2) in flat int scissorIndex;
layout(location = 3) in vec2 quadCoordinates;
layout(location = 4) in vec4 textColor;
layout(location = 5) in vec4 strokeColor;
layout(location = 6) in vec2 strokeDelta;

layout(location = 0) out vec4 outColor;

layout(set = 0, binding = 0) readonly buffer ShaderStorage {
    int operations[];
} shaderStorage;
layout(set = 0, binding = 1) uniform sampler textureSampler[2];
layout(set = 0, binding = 2) uniform texture2D textures[NUM_TEXTURES];
layout(set = 0, binding = 3) uniform texture2D textAtlasTexture;

float decodeColorComponent(int rawValue) {
    return float(rawValue & 255) / 255.0;
}

vec4 decodeColor(int rawColor) {
    return vec4(
        decodeColorComponent(rawColor),
        decodeColorComponent(rawColor >> 8),
        decodeColorComponent(rawColor >> 16),
        decodeColorComponent(rawColor >> 24)
    );
}

float decodeFloat(int rawValue) {
    return float(rawValue) * 0.000001;
}

const int OP_CODE_FILL = 1;
const int OP_CODE_DRAW_IMAGE = 2;
const int OP_CODE_DRAW_TEXT = 6;
const int OP_CODE_DRAW_ROUNDED_RECT = 7;
const int OP_CODE_DRAW_RECT = 8;

void main() {
    float scissorMinX = decodeFloat(shaderStorage.operations[scissorIndex]);
    float scissorMinY = decodeFloat(shaderStorage.operations[scissorIndex + 1]);
    float scissorMaxX = decodeFloat(shaderStorage.operations[scissorIndex + 2]);
    float scissorMaxY = decodeFloat(shaderStorage.operations[scissorIndex + 3]);

    if (position.x < scissorMinX || position.x > scissorMaxX || position.y < scissorMinY || position.y > scissorMaxY) {
        discard;
    }

    int operationCode = shaderStorage.operations[operationIndex];

    if (operationCode == OP_CODE_FILL) {
        vec4 fillColor = decodeColor(shaderStorage.operations[operationIndex + 1]);
        outColor = fillColor;
    } else if (operationCode == OP_CODE_DRAW_IMAGE) {
        int imageIndex = shaderStorage.operations[operationIndex + 1];
        vec2 textureCoordinates = vec2(quadCoordinates.x, 1.0 - quadCoordinates.y);
        outColor = texture(sampler2D(textures[imageIndex], textureSampler[0]), textureCoordinates);
    } else if (operationCode == OP_CODE_DRAW_TEXT) {
        vec2 textureCoordinates = vec2(quadCoordinates.x, 1.0 - quadCoordinates.y);
        float rawIntensity = texture(sampler2D(textAtlasTexture, textureSampler[1]), textureCoordinates).r;

        rawIntensity += texture(sampler2D(textAtlasTexture, textureSampler[1]), textureCoordinates + vec2(strokeDelta.x, strokeDelta.y)).r;
        rawIntensity += texture(sampler2D(textAtlasTexture, textureSampler[1]), textureCoordinates + vec2(-strokeDelta.x, strokeDelta.y)).r;
        rawIntensity += texture(sampler2D(textAtlasTexture, textureSampler[1]), textureCoordinates + vec2(strokeDelta.x, -strokeDelta.y)).r;
        rawIntensity += texture(sampler2D(textAtlasTexture, textureSampler[1]), textureCoordinates + vec2(-strokeDelta.x, -strokeDelta.y)).r;
        rawIntensity /= 5.0;

        float strokeIntensity;
        float fillIntensity;

        float strokeThreshold = 0.7;
        if (rawIntensity <= strokeThreshold) {
            strokeIntensity = rawIntensity / strokeThreshold;
            fillIntensity = 0.0;
        } else {
            strokeIntensity = (1.0 - rawIntensity) / (1.0 - strokeThreshold);
            fillIntensity = 1.0 - strokeIntensity;
        }
        outColor = strokeIntensity * strokeColor + fillIntensity * textColor;
    } else if (operationCode == OP_CODE_DRAW_ROUNDED_RECT) {
        vec4 fillColor = decodeColor(shaderStorage.operations[operationIndex + 1]);
        float minX = decodeFloat(shaderStorage.operations[operationIndex + 2]);
        float minY = decodeFloat(shaderStorage.operations[operationIndex + 3]);
        float maxX = decodeFloat(shaderStorage.operations[operationIndex + 4]);
        float maxY = decodeFloat(shaderStorage.operations[operationIndex + 5]);
        float radiusX = decodeFloat(shaderStorage.operations[operationIndex + 6]);
        float radiusY = 0.5f * (maxY - minY);
        float lineWidth = decodeFloat(shaderStorage.operations[operationIndex + 7]);

        float x = quadCoordinates.x;
        float y = quadCoordinates.y;

        float dx = 0.0;
        if (x < minX + radiusX) {
            dx = x - (minX + radiusX);
        } else if (x > maxX - radiusX) {
            dx = (maxX - radiusX) - x;
        }

        float dy = 0.5 * (minY + maxY) - y;

        dx /= radiusX;
        dy /= radiusY;

        float referenceDistance;
        if (dx == 0.0) {
            referenceDistance = sqrt(dy * dy);
        } else {
            referenceDistance = sqrt(dx * dx + dy * dy);
        }

        float intensity = 1.0;
        if (lineWidth != 0.0) {
            if (referenceDistance >= 1.0 - lineWidth * 0.5) {
                intensity = (1.0 - referenceDistance) / (lineWidth * 0.4);
            } else {
                intensity = max(0.0, (referenceDistance - (1.0 - lineWidth)) / (lineWidth * 0.4));
            }
        } else {
            if (referenceDistance >= 0.95) {
                intensity = max(0.0, (1.0 - referenceDistance) / 0.04);
            }
        }
        outColor = fillColor * min(1.0, intensity);
    } else if (operationCode == OP_CODE_DRAW_RECT) {
        float lineWidthX = decodeFloat(shaderStorage.operations[operationIndex + 1]);
        float lineWidthY = decodeFloat(shaderStorage.operations[operationIndex + 2]);
        vec4 color = decodeColor(shaderStorage.operations[operationIndex + 3]);

        float edgeDistanceLeft = quadCoordinates.x - lineWidthX * 0.5;
        float edgeDistanceRight = 1.0 - lineWidthX * 0.5 - quadCoordinates.x;
        float edgeDistanceX = min(abs(edgeDistanceLeft), abs(edgeDistanceRight)) / lineWidthX;

        float edgeDistanceBottom = quadCoordinates.y - lineWidthY * 0.5;
        float edgeDistanceTop = 1.0 - lineWidthY * 0.5 - quadCoordinates.y;
        float edgeDistanceY = min(abs(edgeDistanceBottom), abs(edgeDistanceTop)) / lineWidthY;

        float edgeDistance = min(edgeDistanceX, edgeDistanceY);
        float signedEdgeDistance = min(
            min(edgeDistanceLeft, edgeDistanceRight) / lineWidthX,
            min(edgeDistanceBottom, edgeDistanceTop) / lineWidthY
        );
        if (signedEdgeDistance < 0.0) edgeDistance = max(edgeDistance, -signedEdgeDistance);

        float intensity = (0.5 - edgeDistance) / 0.3;

        outColor = color * min(1.0, intensity);
    } else {
        // This is the 'unknown operation code' color, for the sake of debugging
        outColor = vec4(1.0, 0.2, 0.6, 1.0);
    }
}

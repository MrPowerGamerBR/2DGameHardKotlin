#version 300 es

precision mediump float;

out vec4 FragColor;
in vec2 TexCoords;

uniform sampler2D image;

// Define the weight array as a constant
const float weight[5] = float[](0.027027, 0.1945946, 0.1216216, 0.054054, 0.016216);

void main() {
    vec2 tex_offset = 4.0 / vec2(textureSize(image, 0)); // Gets size of a single texel
    vec3 result = texture(image, TexCoords).rgb * weight[0]; // Current fragment's contribution

    for (int i = 1; i < 5; ++i) {
        result += texture(image, TexCoords + vec2(tex_offset.x * float(i), 0.0)).rgb * weight[i];
        result += texture(image, TexCoords - vec2(tex_offset.x * float(i), 0.0)).rgb * weight[i];
    }

    FragColor = vec4(result, 0.7);
}
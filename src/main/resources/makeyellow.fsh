#version 300 es
precision highp float;

in vec2 TexCoords;
out vec4 color;

uniform sampler2D image;

void main() {
    vec4 newColor = texture(image, TexCoords);

    if (newColor.a == 1.0f) {
        color = vec4(1.0f, 0.9f, 0.0f, 1.0f);
    } else {
        color = vec4(1.0f, 0.0f, 0.0f, 0.0f);
    }
}
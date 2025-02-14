#version 430 core

out vec4 FragColor;

in vec2 TexCoords; // Passed from vertex shader (range 0.0 to 1.0)

uniform vec3 colorTop;    // Color at the top (e.g., vec3(1.0, 0.0, 0.0) for red)
uniform vec3 colorBottom; // Color at the bottom (e.g., vec3(0.0, 0.0, 1.0) for blue)

void main() {
    float t = TexCoords.y; // Use the y-coordinate (0 at bottom, 1 at top)
    vec3 gradientColor = mix(colorBottom, colorTop, t);
    FragColor = vec4(gradientColor, 1.0);
}
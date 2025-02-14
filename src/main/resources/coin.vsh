#version 430 core

layout (location = 0) in vec4 vertex; // <vec2 position, vec2 texCoords>

out vec2 TexCoords;

uniform float fTime;
uniform mat4 model;
uniform mat4 projection;

void main()
{
    // Calculate the pulsation factor using a sine wave
    float pulsation = sin(fTime * 4.0) * 0.5 + 0.5; // Oscillates between 0 and 1

    // Using xy also works but that's due to our vertex matching the UV coordinates perfectly lol
    TexCoords = vertex.zw;
    gl_Position = projection * model * vec4(vertex.x, vertex.y - (0.005 - (pulsation * 0.01)), 0.0, 1.0);
}
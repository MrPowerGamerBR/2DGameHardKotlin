#version 430 core

in vec2 TexCoords;
out vec4 color;

uniform sampler2D image;
uniform vec4 subImageCoordinates; // (u_min, v_min, u_max, v_max)

void main() {
    // vec4 test = vec4(0.0f, 0.0f, 1.0f, 1.0f);
    // vec2 atlasCoord = mix(test.xy, test.zw, TexCoords);
    // Remember that this is the PIXEL that corresponds to the image
    // color = texture(image, vec2(TexCoords.x + 0.1f, TexCoords.y + 0.1f));
    // color = vec4(1f, 1f, 1f, 1f);
    

    vec2 atlasCoord = mix(subImageCoordinates.xy, subImageCoordinates.zw, TexCoords);
    
    // Remember that this is the PIXEL that corresponds to the image
    color = texture(image, vec2(atlasCoord.x, atlasCoord.y));
}
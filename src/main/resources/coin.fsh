#version 430 core

in vec2 TexCoords;
out vec4 color;

uniform float fTime;
uniform sampler2D image;

const float Epsilon = 1e-10;

vec3 RGBtoHSV(in vec3 RGB) {
    vec4  P   = (RGB.g < RGB.b) ? vec4(RGB.bg, -1.0, 2.0/3.0) : vec4(RGB.gb, 0.0, -1.0/3.0);
    vec4  Q   = (RGB.r < P.x) ? vec4(P.xyw, RGB.r) : vec4(RGB.r, P.yzx);
    float C   = Q.x - min(Q.w, Q.y);
    float H   = abs((Q.w - Q.y) / (6.0 * C + Epsilon) + Q.z);
    vec3  HCV = vec3(H, C, Q.x);
    float S   = HCV.y / (HCV.z + Epsilon);
    return vec3(HCV.x, S, HCV.z);
}

vec3 HSVtoRGB(in vec3 HSV) {
    float H   = HSV.x;
    float R   = abs(H * 6.0 - 3.0) - 1.0;
    float G   = 2.0 - abs(H * 6.0 - 2.0);
    float B   = 2.0 - abs(H * 6.0 - 4.0);
    vec3  RGB = clamp( vec3(R,G,B), 0.0, 1.0 );
    return ((RGB - 1.0) * HSV.y + 1.0) * HSV.z;
}

void main() {
    // Adds a small pulsating brightness to the coins
    vec4 newColor = texture(image, TexCoords);

    if (newColor.a == 1.0f) {
        vec3 hsv = RGBtoHSV(vec3(newColor.r, newColor.g, newColor.b));

        // Calculate the pulsation factor using a sine wave
        float pulsation = sin(fTime * 4.0) * 0.5 + 0.5; // Oscillates between 0 and 1

        if (newColor.r == 0 && newColor.g == 0 && newColor.g == 0) {
            color = newColor;
        } else {
            vec3 rgb = HSVtoRGB(vec3(hsv.r, hsv.g, (hsv.b + (pulsation * 0.7f))));
            color = vec4(rgb.rgb, newColor.a);
        }
    } else {
        color = vec4(1.0f, 0.0f, 0.0f, 0.0f);
    }
}
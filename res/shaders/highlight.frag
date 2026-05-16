#version 330 core

in vec2 TexCoord;
in vec4 Tint;

out vec4 FragColor;

uniform sampler2D u_Texture;
uniform float u_Highlight;      

void main() {
    vec4 texColor = texture(u_Texture, TexCoord) * Tint;

    if(texColor.a < 0.1) discard;

    vec3 white = vec3(1.0, 1.0, 1.0);

    vec3 resultRGB = mix(texColor.rgb, white, u_Highlight);

    FragColor = vec4(resultRGB, texColor.a);
}
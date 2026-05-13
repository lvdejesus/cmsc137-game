#version 330 core
out vec4 FragColor;

in vec2 TexCoord;
in vec4 Tint;

uniform sampler2D u_Texture;

void main() {
    vec4 texColor = texture(u_Texture, TexCoord) * Tint;
    if(texColor.a < 0.1) discard;
    FragColor = texColor;
}
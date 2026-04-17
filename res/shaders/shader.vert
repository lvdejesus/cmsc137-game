#version 330 core

layout (location = 0) in vec3 aPos;
layout (location = 1) in vec2 aTexCoord;
layout (location = 2) in vec4 aColor;

out vec2 TexCoord;
out vec4 Tint;

uniform mat4 u_ProjectionView;

void main() {
    gl_Position = u_ProjectionView * vec4(aPos, 1.0);
    TexCoord = aTexCoord;
    Tint = aColor;
}
#version 330 core

uniform mat4 sprojectionMatrix;
uniform mat4 sviewMatrix;
uniform mat4 modelMatrix;

layout(location = 0) in vec3 position;

void main(void) {
	gl_Position = sprojectionMatrix * sviewMatrix * modelMatrix * vec4(position, 1);
}

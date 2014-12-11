#version 330 core

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 modelMatrix;

uniform mat4 sprojectionMatrix;
uniform mat4 sviewMatrix;

layout(location = 0) in vec3 position;
layout(location = 1) in vec2 texCoord;
layout(location = 2) in vec3 normal;
layout(location = 3) in vec3 tangent;

out vec3 pass_position;
out vec2 pass_texCoord;
out vec3 pass_normal;
out vec4 pass_shadowCoord;

void main(void) {
	pass_position = position;
	pass_texCoord = texCoord;
	pass_normal = normal;
	pass_shadowCoord = vec4((sprojectionMatrix * sviewMatrix * modelMatrix * vec4(position, 1)) / 2 + 0.5);
	
	gl_Position = projectionMatrix * viewMatrix * modelMatrix * vec4(position, 1);
}

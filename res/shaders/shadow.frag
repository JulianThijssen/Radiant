#version 330 core

layout(location = 0) out float fragmentDepth;

uniform vec3 lightPos;

in vec3 pass_position;

void main(void) {
	vec3 lightDir = lightPos - pass_position;
	
	fragmentDepth = length(lightDir);
}

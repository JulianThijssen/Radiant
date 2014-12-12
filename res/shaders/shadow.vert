#version 330 core

// Shadow
struct ShadowInfo {
	sampler2DShadow shadowMap;
	mat4 projectionMatrix;
	mat4 viewMatrix;
};

uniform ShadowInfo shadowInfo;

uniform mat4 modelMatrix;

layout(location = 0) in vec3 position;

void main(void) {
	gl_Position = shadowInfo.projectionMatrix * shadowInfo.viewMatrix * modelMatrix * vec4(position, 1);
}

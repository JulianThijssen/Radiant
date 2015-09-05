uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 modelMatrix;

// Shadow
struct ShadowInfo {
	sampler2D shadowMap;
	mat4 projectionMatrix;
	mat4 viewMatrix;
};

uniform bool isDirLight;

uniform ShadowInfo shadowInfo;

layout(location = 0) in vec3 position;
layout(location = 1) in vec2 texCoord;
layout(location = 2) in vec3 normal;
layout(location = 3) in vec3 tangent;

out vec3 pass_position;
out vec2 pass_texCoord;
out vec3 pass_normal;
out vec3 pass_tangent;
out vec4 pass_shadowCoord;

void main(void) {
	pass_position = position;
	pass_texCoord = texCoord;
	pass_normal = normal;
	pass_tangent = (modelMatrix * vec4(tangent, 0)).xyz;
	if (isDirLight) {
		pass_shadowCoord = biasMatrix * shadowInfo.projectionMatrix * shadowInfo.viewMatrix * modelMatrix * vec4(position, 1);
	}

	gl_Position = projectionMatrix * viewMatrix * modelMatrix * vec4(position, 1);
}

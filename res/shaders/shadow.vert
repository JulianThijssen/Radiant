// Shadow
struct ShadowInfo {
	sampler2D shadowMap;
	mat4 projectionMatrix;
	mat4 viewMatrix;
};

uniform ShadowInfo shadowInfo;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 modelMatrix;

layout(location = 0) in vec3 position;

out vec3 pass_position;

void main(void) {
	gl_Position = projectionMatrix * viewMatrix * modelMatrix * vec4(position, 1);
	pass_position = (modelMatrix * vec4(position, 1)).xyz;
}

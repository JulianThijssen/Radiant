uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 modelMatrix;

layout(location = 0) in vec3 position;
layout(location = 1) in vec2 texCoord;
layout(location = 2) in vec3 normal;
layout(location = 3) in vec3 tangent;

out vec3 pass_position;
out vec2 pass_texCoord;
out vec3 pass_normal;
out vec3 pass_tangent;

void main(void) {
	pass_position = position;
	pass_texCoord = texCoord;
	pass_normal = (modelMatrix * vec4(normal, 0)).xyz;
	pass_tangent = (modelMatrix * vec4(tangent, 0)).xyz;
	
	gl_Position = projectionMatrix * viewMatrix * modelMatrix * vec4(position, 1);
}

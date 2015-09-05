uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 modelMatrix;

layout(location = 0) in vec3 position;
layout(location = 1) in vec2 texCoord;

out vec3 pass_position;
out vec2 pass_texCoord;

void main(void) {
	pass_position = position;
	pass_texCoord = texCoord;
	
	gl_Position = projectionMatrix * viewMatrix * modelMatrix * vec4(position, 1);
}

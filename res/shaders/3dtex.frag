// Pass
uniform mat4 modelMatrix;

uniform float ambientLight;

out vec4 out_Color;

void main(void) {
	out_Color = vec4(texture(reflCubeMap, pass_position).rgb, 1);
}

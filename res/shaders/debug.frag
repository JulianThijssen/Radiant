// Pass
uniform mat4 modelMatrix;

out vec4 out_Color;

void main(void) {
	float f = texture(shadowCubeMap, pass_position).z;
	out_Color = vec4(f, f, f, 1);
}

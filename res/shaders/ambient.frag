uniform sampler2D colorTex;

out vec4 out_Color;

void main(void) {
	vec3 color = texture(colorTex, pass_texCoord).rgb;
	
	out_Color = vec4(color, 1) * 0.1f;
}

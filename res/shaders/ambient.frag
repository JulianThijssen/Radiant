uniform sampler2D colorTex;

uniform float ambientLight;

out vec4 out_Color;

void main(void) {
	vec3 color = texture(colorTex, pass_texCoord).rgb;
	
	out_Color = vec4(color, 1) * ambientLight;
}

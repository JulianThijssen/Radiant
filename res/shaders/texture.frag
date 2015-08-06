// Pass
uniform mat4 modelMatrix;

uniform float ambientLight;

out vec4 out_Color;

void main(void) {
	if (material.hasDiffuseMap) {
		vec3 color = texture(material.diffuseMap, pass_texCoord * material.tiling).rgb;
		out_Color = vec4(color * ambientLight, 1);
	} else {
		vec3 color = vec3(1, 1, 1);
		out_Color = vec4(color * ambientLight, 1);
	}
}

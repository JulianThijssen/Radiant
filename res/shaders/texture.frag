// Pass
uniform mat4 modelMatrix;

out vec4 out_Color;

void main(void) {
	if (material.hasDiffuseMap) {
		out_Color = texture(material.diffuseMap, pass_texCoord * material.tiling);
	} else {
		out_Color = vec4(1, 1, 1, 1);
	}
}

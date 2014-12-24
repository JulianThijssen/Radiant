// Material
uniform sampler2D diffuseMap;

uniform int hasDiffuseMap = 0;

uniform vec3 diffuseColor;

uniform vec2 tiling;

// Pass
uniform mat4 modelMatrix;

out vec4 out_Color;

void main(void) {
	vec3 light = vec3(0, 0, 0);
	
	out_Color = vec4(diffuseColor, 1);
	if(hasDiffuseMap == 1) {
		out_Color = texture(diffuseMap, pass_texCoord * tiling);
	}
}

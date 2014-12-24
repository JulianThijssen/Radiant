#version 330 core

// Material
struct Material {
	vec3 diffuseColor;
	vec3 specularColor;
	
	float specularIntensity;
	vec2 tiling;
	
	float hardness;
	
	sampler2D diffuseMap;
	sampler2D normalMap;
	sampler2D specularMap;
	
	bool hasDiffuseMap;
	bool hasNormalMap;
	bool hasSpecularMap;
	
	bool receiveShadows;
};

uniform Material material;

// Pass
uniform mat4 modelMatrix;
in vec3 pass_position;
in vec2 pass_texCoord;
in vec3 pass_normal;

out vec4 out_Color;

void main(void) {
	if (material.hasDiffuseMap) {
		out_Color = texture(material.diffuseMap, pass_texCoord * material.tiling);
	} else {
		out_Color = vec4(1, 1, 1, 1);
	}
}

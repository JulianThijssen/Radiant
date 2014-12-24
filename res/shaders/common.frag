#version 330 core

// Shadow
struct ShadowInfo {
	sampler2D shadowMap;
	mat4 projectionMatrix;
	mat4 viewMatrix;
};

// Light
struct PointLight {
	vec3 position;
	vec3 color;
	float energy;
	float distance;
	bool castShadows;
};

struct DirectionalLight {
	vec3 direction;
	vec3 color;
	float energy;
	bool castShadows;
};

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

uniform ShadowInfo shadowInfo;
uniform samplerCube shadowCubeMap;

uniform PointLight pointLight;
uniform DirectionalLight dirLight;

uniform bool isPointLight;
uniform bool isDirLight;

uniform Material material;

// Pass
in vec3 pass_position;
in vec2 pass_texCoord;
in vec3 pass_normal;
in vec3 pass_tangent;
in vec4 pass_shadowCoord;



/* Calculates the normal of the fragment using a normal map */
vec3 calcNormal(vec3 src_normal, vec3 src_tangent, vec2 texCoord) {
	vec3 normal = normalize(src_normal);
	vec3 tangent = normalize(src_tangent);
	tangent = normalize(tangent - dot(tangent, normal) * normal);
	vec3 bitangent = cross(tangent, normal);
	vec3 mapnorm = texture(material.normalMap, texCoord * material.tiling).rgb * 2.0 - 1.0;
	
	mat3 TBN = mat3(tangent, bitangent, normal);
	normal = normalize(TBN * mapnorm);
	return normal;
}

/* Calculates point light attribution */
float calcPointAtt(PointLight light, vec3 lightDir) {
	float lightLength = length(lightDir);
	float x = lightLength / light.distance;
    float fAtt = 1 - pow(x, 2);
    if (fAtt < 0) {
    	fAtt = 0;
    }
    return fAtt;
}

/* Calculates the diffuse contribution of the light */
float calcDiffuse(vec3 lightDir, vec3 normal) {
	return clamp(dot(normal, normalize(lightDir)), 0, 1);
}

/* Calculates the specular contribution of the light */
float calcSpec(vec3 lightDir, vec3 camDir, vec3 normal) {
	vec3 half = (normalize(lightDir) + normalize(camDir)) / 2;
    float fPhong = pow(max(dot(normal, normalize(half)), 0), material.hardness);
    if(material.hasSpecularMap) {
    	fPhong *= texture(material.specularMap, pass_texCoord * material.tiling).xyz;
    }
    return fPhong;
}

/* Calculates whether a given fragment is in shadow for directional lights */
float getDirVisibility(float bias) {
	float factor = 0;
	float xOffset = 1.0 / 1024;
	float yOffset = 1.0 / 1024;
	
	for (int y = -1; y <= 1; y++) {
		for (int x = -1; x <= 1; x++) {
			float sx = pass_shadowCoord.x + x * xOffset;
			float sy = pass_shadowCoord.y + y * yOffset;
			if (texture(shadowInfo.shadowMap, pass_shadowCoord.xy).z < pass_shadowCoord.z - bias) {
				factor += 0;
			} else {
				factor += 1;
			}
		}
	}
	
	return (factor / 18.0);
}

/* Calculates whether a given fragment is in shadow for point lights */
float getPointVisibility(float bias, vec3 lightDir) {
	float factor = 0;
	float xOffset = 1.0 / 1024;
	float yOffset = 1.0 / 1024;
	
	float sample = texture(shadowCubeMap, -lightDir).r;
	float dist = length(lightDir);
	
	for (int y = -1; y <= 1; y++) {
		for (int x = -1; x <= 1; x++) {
			float sx = pass_shadowCoord.x + x * xOffset;
			float sy = pass_shadowCoord.y + y * yOffset;
			if (sample < dist - bias) {
				factor += 0;
			} else {
				factor += 1;
			}
		}
	}
	
	return (factor / 18.0);
}

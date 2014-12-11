#version 330 core

// Light
struct PointLight {
	vec3 position;
	vec3 color;
	float energy;
	float distance;
};

struct DirectionalLight {
	vec3 direction;
	vec3 color;
	float energy;
};

uniform PointLight pointLights[80];
uniform DirectionalLight dirLights[10];

uniform int numPointLights;
uniform int numDirLights;

uniform sampler2D shadowMap;

// Material
struct Material {
	vec3 diffuseColor;
	float specularIntensity;
	vec2 tiling;
	
	float hardness;
	
	sampler2D diffuseMap;
	sampler2D normalMap;
	sampler2D specularMap;
	
	int hasDiffuseMap;
	int hasNormalMap;
	int hasSpecularMap;
};

uniform Material material;

// Pass
uniform mat4 modelMatrix;
in vec3 pass_position;
in vec2 pass_texCoord;
in vec3 pass_normal;
in vec4 pass_shadowCoord;

out vec4 out_Color;

void main(void) {
	vec3 refl = vec3(0, 0, 0);
	
	// Calculate the location of this fragment (pixel) in world coordinates
    vec3 position = (modelMatrix * vec4(pass_position, 1)).xyz;
    
	//Normals
    vec3 normal = normalize(transpose(inverse(mat3(modelMatrix))) * pass_normal);
    
    // Point lighting
    for(int i = 0; i < numPointLights; i++) {
    	PointLight light = pointLights[i];
    	
	    // Calculate the vector from this pixels surface to the light source
	    vec3 lightDir = light.position - position;
	    
	    float length = length(lightDir);
	    
	    float x = length / light.distance;
	    float fAtt = 1 - pow(x, 2);
	    if (fAtt < 0) {
	    	fAtt = 0;
	    }
	    
	    // Calculate the cosine of the angle of incidence (brightness)
	    float fDiffuse = clamp(dot(normal, normalize(lightDir)), 0, 1);
	    
	    refl += material.diffuseColor * light.color * fDiffuse * fAtt * light.energy;
	}
	
	float bias = 0.005;
	float visibility = 1.0;
	if (texture(shadowMap, pass_shadowCoord.xy).z < pass_shadowCoord.z - bias) {
		visibility = 0.5;
	}
	
	// Directional lighting
	for(int i = 0; i < numDirLights; i++) {
		DirectionalLight light = dirLights[i];
	
		// Calculate the vector from this pixels surface to the light source
		vec3 lightDir = -light.direction;
		vec3 lightColor = light.color;
		
		// Calculate the cosine of the angle of incidence (brightness)
		float fDiffuse = clamp(dot(normal, normalize(lightDir)), 0, 1);
		
		refl += material.diffuseColor * light.color * fDiffuse * light.energy;
	}
	
	out_Color = vec4(material.diffuseColor * refl, 1);
	if(material.hasDiffuseMap == 1) {
		out_Color *= texture(material.diffuseMap, pass_texCoord * material.tiling);
	}
	
	out_Color.rgb *= visibility;
}

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

// Shadow
struct ShadowInfo {
	sampler2D shadowMap;
	mat4 projectionMatrix;
	mat4 viewMatrix;
};

uniform ShadowInfo shadowInfo;

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
	
	// Shadows
	float visibility = 1.0;
	float bias = 0.005;
	float factor = 0;
	float xOffset = 1.0 / 1024;
	float yOffset = 1.0 / 800;
	
	if (material.receiveShadows) {
		//float cosTheta = dot(normal, normalize(-dirLights[0].direction));
		//float bias = 0.01 * tan(acos(cosTheta));
		
		for (int y = -2; y <= 2; y++) {
			for (int x = -2; x <= 2; x++) {
				float sx = pass_shadowCoord.x + x * xOffset;
				float sy = pass_shadowCoord.y + y * yOffset;
				//factor += texture(shadowInfo.shadowMap, vec3(sx, sy, pass_shadowCoord.z - bias));
			}
 		}
		
		visibility = 0.5 + (factor / 50.0);
	}
	
	out_Color = vec4(material.diffuseColor * refl, 1);
	if(material.hasDiffuseMap) {
		out_Color *= texture(material.diffuseMap, pass_texCoord * material.tiling);
	}
	
	out_Color.rgb *= visibility;
	
	out_Color = vec4(texture(shadowInfo.shadowMap, pass_texCoord).rgb, 1);
}

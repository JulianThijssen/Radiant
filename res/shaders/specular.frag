#version 330 core

// Shadow
struct ShadowInfo {
	sampler2DShadow shadowMap;
	mat4 projectionMatrix;
	mat4 viewMatrix;
};

uniform ShadowInfo plShadowInfo[20];
uniform ShadowInfo dlShadowInfo[10];

// Light
struct PointLight {
	vec3 position;
	vec3 color;
	float energy;
	float distance;
	
	//ShadowInfo shadowInfo[6];
};

struct DirectionalLight {
	vec3 direction;
	vec3 color;
	float energy;
	
	//ShadowInfo shadowInfo;
};

uniform PointLight pointLights[20];
uniform DirectionalLight dirLights[10];

uniform int numPointLights;
uniform int numDirLights;

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

uniform mat4 modelMatrix;
uniform vec3 cameraPosition;

// Pass
in vec3 pass_position;
in vec2 pass_texCoord;
in vec3 pass_normal;
in vec3 pass_tangent;

out vec4 out_Color;

vec3 calcNormal(vec3 src_normal) {
	vec3 normal = normalize(src_normal);
	vec3 tangent = normalize(pass_tangent);
	tangent = normalize(tangent - dot(tangent, normal) * normal);
	vec3 bitangent = cross(tangent, normal);
	vec3 mapnorm = texture(material.normalMap, pass_texCoord * material.tiling).rgb * 2.0 - 1.0;
	
	mat3 TBN = mat3(tangent, bitangent, normal);
	normal = normalize(TBN * mapnorm);
	return normal;
}

void main(void) {
	// Shadows
	float visibility = 0.0;
	float bias = 0.005;
	float xOffset = 1.0 / 1024;
	float yOffset = 1.0 / 800;
	
	vec3 refl = vec3(0, 0, 0);
	float shadow = 0.5f;
	
	//Calculate the location of this fragment (pixel) in world coordinates
    vec3 position = (modelMatrix * vec4(pass_position, 1)).xyz;
    
    //Normals
    vec3 normal = normalize(transpose(inverse(mat3(modelMatrix))) * pass_normal);
    
    if(material.hasNormalMap) {
    	normal = calcNormal(normal);
    }
    
    vec3 camDir = normalize(cameraPosition - position);
    
    // Point lighting
    for(int i = 0; i < numPointLights; i++) {
    	PointLight light = pointLights[i];
    	
	    // Calculate the vector from this pixels surface to the light source
	    vec3 lightDir = light.position - position;

	    float length = length(lightDir);
	    
	    float x = length / light.distance;
	    float fAtt = 1 - pow(x, 4);
	    if (fAtt < 0) {
	    	fAtt = 0;
	    }
	    
	    // Calculate diffuse lighting
	    float fDiffuse = clamp(dot(normal, normalize(lightDir)), 0, 1);
	    
		refl += material.diffuseColor * light.color * fDiffuse * fAtt * light.energy;
	    
	    // Calculate specular lighting
	    vec3 half = (normalize(lightDir) + normalize(camDir))/2;
	    float fPhong = pow(max(dot(normal, normalize(half)), 0), material.hardness);
	    if(material.hasSpecularMap) {
	    	fPhong *= texture(material.specularMap, pass_texCoord * material.tiling).xyz;
	    }
	    
	    for (int j = 0; j < 6; j++) {
	    	int index = i * 6 + j;
		    // Shadows
			vec4 shadowCoord = vec4((plShadowInfo[index].projectionMatrix * plShadowInfo[index].viewMatrix * vec4(position, 1)) / 2 + 0.5);
			
			if (material.receiveShadows) {
				//float cosTheta = dot(normal, normalize(-dirLights[0].direction));
				//float bias = 0.01 * tan(acos(cosTheta));
				float factor = 0;
				
				for (int y = -1; y <= 1; y++) {
					for (int x = -1; x <= 1; x++) {
						float sx = shadowCoord.x + x * xOffset;
						float sy = shadowCoord.y + y * yOffset;
						factor += texture(plShadowInfo[i].shadowMap, vec3(sx, sy, shadowCoord.z - bias));
					}
		 		}
				visibility += (factor / 18.0);
			}
		}
	    
		refl += material.specularColor * light.color * fPhong * material.specularIntensity;
	}
	
	// Directional lighting
	for(int i = 0; i < numDirLights; i++) {
		DirectionalLight light = dirLights[i];
	
		// Calculate the vector from this pixels surface to the light source
		vec3 lightDir = -light.direction;
		vec3 lightColor = light.color;
	
		
		// Calculate diffuse lighting
		float fDiffuse = clamp(dot(normal, normalize(lightDir)), 0, 1);
		
		refl += material.diffuseColor * light.color * fDiffuse * light.energy;

		// Calculate specular lighting
	    vec3 half = (normalize(lightDir) + normalize(camDir))/2;
	    float fPhong = pow(max(dot(normal, normalize(half)), 0), material.hardness);
	    if(material.hasSpecularMap) {
	    	fPhong *= texture(material.specularMap, pass_texCoord * material.tiling).xyz;
	    }
		
		// Shadows
		vec4 shadowCoord = vec4((dlShadowInfo[i].projectionMatrix * dlShadowInfo[i].viewMatrix * vec4(position, 1)) / 2 + 0.5);
		
		if (material.receiveShadows) {
			//float cosTheta = dot(normal, normalize(-dirLights[0].direction));
			//float bias = 0.01 * tan(acos(cosTheta));
			float factor = 0;
			
			for (int y = -1; y <= 1; y++) {
				for (int x = -1; x <= 1; x++) {
					float sx = shadowCoord.x + x * xOffset;
					float sy = shadowCoord.y + y * yOffset;
					factor += texture(dlShadowInfo[i].shadowMap, vec3(sx, sy, shadowCoord.z - bias));
				}
	 		}
			
			visibility = 0.5 + (factor / 18.0);
		}
		
		refl += material.specularColor * light.color * fPhong * material.specularIntensity;
	}
	
	out_Color = vec4(material.diffuseColor * refl, 1);
	if(material.hasDiffuseMap) {
		out_Color *= texture(material.diffuseMap, pass_texCoord * material.tiling);
	}
	
	out_Color.rgb *= visibility;
	
	//out_Color = texture(plShadowInfo[4].shadowMap, pass_texCoord);
}

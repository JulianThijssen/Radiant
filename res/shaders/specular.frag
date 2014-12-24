#version 330 core

// Shadow
struct ShadowInfo {
	sampler2D shadowMap;
	mat4 projectionMatrix;
	mat4 viewMatrix;
};

uniform ShadowInfo shadowInfo;
uniform samplerCube shadowCubeMap;

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

uniform PointLight pointLight;
uniform DirectionalLight dirLight;

uniform bool isPointLight;
uniform bool isDirLight;

// Material
struct Material {
	vec3 diffuseColor;
	vec3 specularColor;
	
	float specularIntensity;
	vec2 tiling;
	
	float hardness;
	
	sampler2D normalMap;
	sampler2D specularMap;
	
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
in vec4 pass_shadowCoord;

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
	float visibility = 1.0;
	float bias = 0.005;
	float xOffset = 1.0 / 1024;
	float yOffset = 1.0 / 1024;
	
	vec3 refl = vec3(0, 0, 0);
	
	//Calculate the location of this fragment (pixel) in world coordinates
    vec3 position = (modelMatrix * vec4(pass_position, 1)).xyz;
    
    //Normals
    vec3 normal = normalize(transpose(inverse(mat3(modelMatrix))) * pass_normal);
    
    if(material.hasNormalMap) {
    	normal = calcNormal(normal);
    }
    
    vec3 camDir = normalize(cameraPosition - position);
	
	// Point lighting
	if (isPointLight) {
		PointLight light = pointLight;
		
		// Calculate the vector from this pixels surface to the light source
	    vec3 lightDir = light.position - position;

	    float lightLength = length(lightDir);
	    
	    float x = lightLength / light.distance;
	    float fAtt = 1 - pow(x, 4);
	    if (fAtt < 0) {
	    	fAtt = 0;
	    }
	    
	    // Calculate diffuse lighting
	    float fDiffuse = clamp(dot(normal, normalize(lightDir)), 0, 1);
	    
		refl += material.diffuseColor * light.color * fDiffuse * fAtt * light.energy;
	    
	    // Calculate specular lighting
	    vec3 half = (normalize(lightDir) + normalize(camDir)) / 2;
	    float fPhong = pow(max(dot(normal, normalize(half)), 0), material.hardness);
	    if(material.hasSpecularMap) {
	    	fPhong *= texture(material.specularMap, pass_texCoord * material.tiling).xyz;
	    }
		
		// Shadows
		if (material.receiveShadows) {
			//float cosTheta = dot(normal, normalize(-dirLight.direction));
			//float bias = 0.01 * tan(acos(cosTheta));
			//float factor = 0;
			
			//for (int y = -1; y <= 1; y++) {
			//	for (int x = -1; x <= 1; x++) {
			//		//float sx = shadowCoord.x + x * xOffset;
			//		//float sy = shadowCoord.y + y * yOffset;
			//		//factor += texture(shadowInfo.shadowMap, vec3(sx, sy, shadowCoord.z - bias));
			//	}
	 		//}
			//visibility += (factor / 18.0);

			float sample = texture(shadowCubeMap, position - light.position).r;
			float dist = length(lightDir);
			
			if (sample < dist - bias * 20) {
				visibility = 0.5;
			}
		}
	    
		refl += material.specularColor * light.color * fPhong * fAtt * material.specularIntensity;
	}
	
	// Directional lighting
	if (isDirLight) {
		DirectionalLight light = dirLight;
	
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
		if (material.receiveShadows) {
			//float cosTheta = dot(normal, normalize(-dirLight.direction));
			//float bias = 0.01 * tan(acos(cosTheta));
			//float factor = 0;
			
			//for (int y = -1; y <= 1; y++) {
			//	for (int x = -1; x <= 1; x++) {
			//		float sx = shadowCoord.x + x * xOffset;
			//		float sy = shadowCoord.y + y * yOffset;
			//		factor += texture(shadowInfo.shadowMap, vec3(sx, sy, shadowCoord.z - bias));
			//	}
	 		//}
			//visibility += (factor / 18.0);
			
			if (texture(shadowInfo.shadowMap, pass_shadowCoord.xy).z < pass_shadowCoord.z - bias) {
				visibility = 0.5;
			}
		}
		
		refl += material.specularColor * light.color * fPhong * material.specularIntensity;
	}
	
	out_Color = vec4(refl * visibility, 1);
}

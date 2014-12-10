#version 330 core

// Light
struct PointLight {
	vec3 position;
	vec3 color;
	float distance;
};

struct DirectionalLight {
	vec3 direction;
	vec3 color;
};

uniform PointLight pointLights[80];
uniform DirectionalLight dirLights[10];

uniform int numPointLights;
uniform int numDirLights;

uniform sampler2DShadow shadowMap;

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

uniform vec3 cameraPosition;

// Pass
uniform mat4 modelMatrix;
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
	vec3 refl = vec3(0, 0, 0);
	
	//Calculate the location of this fragment (pixel) in world coordinates
    vec3 position = (modelMatrix * vec4(pass_position, 1)).xyz;
    
    //Normals
	//mat3 normalMatrix = transpose(inverse(mat3(modelMatrix)));
    //vec3 normal = normalize(normalMatrix * pass_normal);
    vec3 normal = pass_normal;
    
    if(material.hasNormalMap) {
    	normal = calcNormal(normal);
    	normal = normal;
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
	    float fDiffuse = dot(normal, normalize(lightDir));
	    
		refl += material.diffuseColor * light.color * fDiffuse * fAtt;
	    
	    // Calculate specular lighting
	    vec3 half = (normalize(lightDir) + normalize(camDir))/2;
	    float fPhong = pow(max(dot(normal, normalize(half)), 0), material.hardness);
	    if(material.hasSpecularMap) {
	    	fPhong *= texture(material.specularMap, pass_texCoord * material.tiling).xyz;
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
		float fDiffuse = dot(normal, normalize(lightDir));
		
		refl += material.diffuseColor * light.color * fDiffuse;

		// Calculate specular lighting
	    vec3 half = (normalize(lightDir) + normalize(camDir))/2;
	    float fPhong = pow(max(dot(normal, normalize(half)), 0), material.hardness);
	    if(material.hasSpecularMap) {
	    	fPhong *= texture(material.specularMap, pass_texCoord * material.tiling).xyz;
	    }
		
		refl += material.specularColor * light.color * fPhong * material.specularIntensity;
	}
	
	// Shadows
	float visibility = 1.0;
	if (material.receiveShadows) {
		float cosTheta = dot(normal, normalize(-dirLights[0].direction));
		float bias = 0.01 * tan(acos(cosTheta));
		bias = 0.005;
		
		float shadowAtt = texture(shadowMap, vec3(pass_shadowCoord.xy, pass_shadowCoord.z - bias)) + 1;
		visibility -= 0.5/shadowAtt;
	}
	
	out_Color = vec4(material.diffuseColor * refl, 1);
	if(material.hasDiffuseMap) {
		out_Color *= texture(material.diffuseMap, pass_texCoord * material.tiling);
	}
	
	out_Color.rgb *= visibility;
}

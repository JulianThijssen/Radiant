#version 330 core

struct Light {
	vec4 position;
	vec3 color;
	float constantAtt;
	float linearAtt;
	float quadraticAtt;
};

uniform mat4 modelMatrix;
uniform mat4 viewMatrix;

uniform sampler2D diffuseMap;
uniform sampler2D normalMap;
uniform sampler2D specularMap;

uniform vec2 tiling;

uniform Light lights[100];
uniform int numLights;

in vec4 pass_position;
in vec2 pass_texCoord;
in vec3 pass_normal;
in vec3 pass_tangent;

out vec4 out_Color;

vec3 calcNormal() {
	vec3 normal = normalize(pass_normal);
	vec3 tangent = normalize(pass_tangent);
	tangent = normalize(tangent - dot(tangent, normal) * normal);
	vec3 bitangent = cross(tangent, normal);
	vec3 mapnorm = texture(normalMap, pass_texCoord * tiling).rgb * 2.0 - 1.0;
	
	mat3 TBN = mat3(tangent, bitangent, normal);
	normal = normalize(TBN * mapnorm);
	return normal;
}

void main(void) {
	vec3 light = vec3(0, 0, 0);
	
	//Calculate the location of this fragment (pixel) in world coordinates
    vec4 position = modelMatrix * pass_position;
    
    //Normals
    vec3 normal = calcNormal();
    
    for(int i = 0; i < numLights; i++) {
	    //Calculate the vector from this pixels surface to the light source
	    vec3 lightDir = (lights[i].position - position).xyz;
	    
	    float length = length(lightDir);
	    vec3 lightColor = lights[i].color;
	    
	    //Calculate the cosine of the angle of incidence (brightness)
	    float fDiffuse = dot(normal, normalize(lightDir));
	    float fAttTotal = 1 / (lights[i].constantAtt + lights[i].linearAtt * length + lights[i].quadraticAtt * length * length);
	    light.r += lightColor.r * fAttTotal * fDiffuse;
	    light.g += lightColor.g * fAttTotal * fDiffuse;
	    light.b += lightColor.b * fAttTotal * fDiffuse;
	}
	
    out_Color = texture(diffuseMap, pass_texCoord * tiling) * vec4(light, 1);
}

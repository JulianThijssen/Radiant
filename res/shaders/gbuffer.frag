uniform mat4 modelMatrix;

layout(location = 0) out vec4 out_Color;
layout(location = 1) out vec4 out_Normal;
layout(location = 2) out vec4 out_Position;
layout(location = 3) out vec4 out_Specular;

void main(void) {
	out_Color = vec4(material.diffuseColor, 1);
	if (material.hasDiffuseMap) {
		out_Color *= texture(material.diffuseMap, pass_texCoord * material.tiling);
	}
	
	vec3 normal = pass_normal;
	if (material.hasNormalMap) {
    	normal = calcNormal(normal, pass_tangent, pass_texCoord);
    }
    
	out_Normal = toColor(normalize((modelMatrix * vec4(normal, 0))));
	out_Position = vec4(pass_position, 1);
	out_Specular = vec4(material.specularColor * material.specularIntensity, material.hardness);
	//out_Depth = vec4(depth, depth, depth, 1);
}

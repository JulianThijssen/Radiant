// Pass
uniform mat4 modelMatrix;

out vec4 out_Color;

void main(void) {
	vec3 refl = vec3(0, 0, 0);
	
	//Calculate the location of this fragment (pixel) in world coordinates
    vec3 position = (modelMatrix * vec4(pass_position, 1)).xyz;
    
    //Normals
	mat3 normalMatrix = transpose(inverse(mat3(modelMatrix)));
    vec3 normal = normalize(normalMatrix * pass_normal);
    
    if(material.hasNormalMap) {
    	normal = calcNormal(normal, pass_tangent, pass_texCoord);
    	normal = normal;
    }
    
	// Point lighting
	if (isPointLight) {
		PointLight light = pointLight;
    	
    	vec3 lightDir = light.position - position;
    	
	    // Calculate the diffuse contribution
	    float fAtt = calcPointAtt(light, lightDir);
	    float fDiffuse = calcDiffuse(lightDir, normal);
		refl += material.diffuseColor * light.color * light.energy * fDiffuse * fAtt;
	}
	
	// Directional lighting
	if (isDirLight) {
		DirectionalLight light = dirLight;
		
		// Calculate the vector from this pixels surface to the light source
		vec3 lightDir = -light.direction;	
		
		// Calculate the cosine of the angle of incidence (brightness)
		float fDiffuse = dot(normal, normalize(lightDir));
		
		refl += light.color * fDiffuse;
	}
	
	out_Color = vec4(material.diffuseColor, 1) * vec4(refl, 1);
	if(material.hasDiffuseMap) {
		out_Color *= texture(material.diffuseMap, pass_texCoord * material.tiling);
	}
}

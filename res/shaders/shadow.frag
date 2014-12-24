layout(location = 0) out float fragmentDepth;

uniform vec3 lightPos;

void main(void) {
	vec3 lightDir = lightPos - pass_position;
	
	fragmentDepth = length(lightDir);
}

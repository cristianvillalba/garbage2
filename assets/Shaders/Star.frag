uniform sampler2D m_Texture;
uniform vec3 m_posOffset;
 
uniform vec2 g_Resolution;
uniform float g_Time;
uniform float m_distance;
 
float snoise(vec3 uv, float res)	// by trisomie21
{
	const vec3 s = vec3(1e0, 1e2, 1e4);
	
	uv *= res;
	
	vec3 uv0 = floor(mod(uv, res))*s;
	vec3 uv1 = floor(mod(uv+vec3(1.), res))*s;
	
	vec3 f = fract(uv); f = f*f*(3.0-2.0*f);
	
	vec4 v = vec4(uv0.x+uv0.y+uv0.z, uv1.x+uv0.y+uv0.z,
		      	  uv0.x+uv1.y+uv0.z, uv1.x+uv1.y+uv0.z);
	
	vec4 r = fract(sin(v*1e-3)*1e5);
	float r0 = mix(mix(r.x, r.y, f.x), mix(r.z, r.w, f.x), f.y);
	
	r = fract(sin((v + uv1.z - uv0.z)*1e-3)*1e5);
	float r1 = mix(mix(r.x, r.y, f.x), mix(r.z, r.w, f.x), f.y);
	
	return mix(r0, r1, f.z)*2.-1.;
}


void main(void)
{
	//float brightness	= 0.25 * 0.25 + 0.1 * 0.25;
        float brightness	= m_distance * 0.25 + 0.1 * 0.25;
	//float radius		= 0.24 + brightness * 0.2;
        float radius		= m_distance + brightness * 0.2;
	float invRadius 	= 1.0/radius;
	
	vec3 orange             = vec3( 0.8, 0.65, 0.3 );
	vec3 orangeRed		= vec3( 0.8, 0.35, 0.1 );
	float time		= g_Time * 0.1;
	float aspect            = g_Resolution.x/g_Resolution.y;
	vec2 uv			= gl_FragCoord.xy/ g_Resolution.xy;
        vec2 uv2                = -m_posOffset.xy;
        vec2 p                  = -0.5 + uv + uv2;
	p.x *= aspect;

	float fade		= pow( length( 2.0 * p ), 0.5 );
	float fVal1		= 1.0 - fade;
	float fVal2		= 1.0 - fade;
	
	float angle		= atan( p.x, p.y )/6.2832;
	float dist		= length(p);
	vec3 coord		= vec3( angle, dist, time * 0.1 );
	
	float newTime1	= abs( snoise( coord + vec3( 0.0, -time * ( 0.35 + brightness * 0.001 ), time * 0.015 ), 15.0 ) );
	float newTime2	= abs( snoise( coord + vec3( 0.0, -time * ( 0.15 + brightness * 0.001 ), time * 0.015 ), 45.0 ) );	
	for( int i=1; i<=7; i++ ){
		float power = pow( 2.0, float(i + 1) );
		fVal1 += ( 0.5 / power ) * snoise( coord + vec3( 0.0, -time, time * 0.2 ), ( power * ( 10.0 ) * ( newTime1 + 1.0 ) ) );
		fVal2 += ( 0.5 / power ) * snoise( coord + vec3( 0.0, -time, time * 0.2 ), ( power * ( 25.0 ) * ( newTime2 + 1.0 ) ) );
	}
	
	float corona		= pow( fVal1 * max( 1.1 - fade, 0.0 ), 2.0 ) * 50.0;
	corona				+= pow( fVal2 * max( 1.1 - fade, 0.0 ), 2.0 ) * 50.0;
	corona				*= 1.2 - newTime1;
	vec3 sphereNormal 	= vec3( 0.0, 0.0, 1.0 );
	vec3 dir 			= vec3( 0.0 );
	vec3 center			= vec3( 0.5, 0.5, 1.0 );
	vec3 starSphere		= vec3( 0.0 );
	
	vec2 sp = -1.0 + 2.0 * (uv + uv2);
	sp.x *= aspect;
	sp *= ( 2.0 - brightness );
  	float r = dot(sp,sp);
	float f = (1.0-sqrt(abs(1.0-r)))/(r) + brightness * 0.5 ;
	if( dist < radius ){
		corona			*= pow( dist * invRadius, 24.0 );
  		vec2 newUv;
 		newUv.x = sp.x*f ;
  		newUv.y = sp.y*f +0.5;
		newUv += vec2(time, 0.0 );
                
		
		vec3 texSample 	= texture2D( m_Texture, newUv ).rgb;
		float uOff		= ( texSample.g * brightness * 4.5 + time );

		vec2 starUV		= newUv + vec2( uOff, 0.0 );
		starSphere		= texture2D( m_Texture, starUV ).rgb;
	}
        corona *= m_distance;
	
	float starGlow	= min( max( 1.0 - dist * ( 1.0 - brightness ), 0.0 ), 1.0 );
	gl_FragColor.rgb	= vec3( f * ( 0.75 + brightness * 0.3 ) * orange  ) + starSphere + corona * orange  + starGlow * orangeRed ;
        
        if (dist < radius){
            gl_FragColor.a		= 1.0f;
        }
        else
        {
            //---Trate de usar la funcion de intensidad :(
            //float dist2 = abs(dist-radius);
            //dist2 *= 100;
            //dist2 += 1;
            //gl_FragColor.a              = 1/(pow(dist2,2)) + corona * orange;
            float dist2 = clamp(dist, radius, 1.5*radius);
            dist2 = abs(1.5*radius - dist2)/((1.5*radius - radius));
            gl_FragColor.a = dist2 * corona * 24* m_distance * orange;
        }
        
}
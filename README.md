## 场景2
应用和IAM（WSO2）集成，用户或应用基于OAUTH协议规范访问具体API
### 技术
采用OAuth实现对API资源的保护
### 示例
```shell
git clone https://github.com/fly2wind/iam-sample-api.git
```
### 步骤
> WSO2中的配置

1. 登录WSO2, 新增一个代表该应用的Service Providers, 填入Service Provider的Name, 如: `EC2-API`
![](1.png)

2. 点击`Register`进入下一步，配置Service Providers的Inbound Authentication Configuration，新增一个OAuth/OpenID Connect Configuration的配置
![](2.png)

3. 点击`Configure`, 配置应用的OAuth的回调地址，如: `http://demo.thoughtworks.local:8082/login`
![](3.png)

4. 点击`Add`,完成Service Providers的添加，系统将会生成应用用于和IAM(WSO2)集成所需的OAuth的`Client Key`和`Client Secret`
![](4.png)

5. 另外，重复以上步骤，新增一个代表IAM应用的Service Providers，配置相应的OAuth信息，该OAuth的`Client Key`和`Client Secret`信息将会在下面步骤中用于模拟IAM应用通过WSO2(OAuth 认证服务器)获取用户的Access Token

> 应用中的配置

*应用根据自己的技术栈选择相应的OAuth框架，例如团队使用Spring Cloud进行微服务开发，可以使用Spring框架相应的OAuth框架*

1. 根据Spring Cloud框架应用配置具体OAuth的相关信息，修改application.yml文件，填入上面步骤注册Service Providers后生成的OAuth的`Client Key`和`Client Secret`

	```yaml
	spring:
	  application:
	    name: iam-sample-api
	
	server:
	    port: 8082
	
	security:
	  basic:
	    enabled: false
	  oauth2:
	    client:
	      clientId: gZiFpL3Du5le9vCH7SBCShMG2qEa
	      clientSecret: EIWlmkeidVgJOI6PNyd9vEUQs2Aa
	      accessTokenUri: https://localhost:9443/oauth2/token
	      userAuthorizationUri: https://localhost:9443/oauth2/authorize
	      clientAuthenticationScheme: header
	      scope: openid
	      autoApprove: true
	      use-current-uri: true
	    resource:
	      user-info-uri: https://localhost:9443/oauth2/userinfo?schema=openid
	      prefer-token-info: false     
	```

2. 应用内配置需要通过OAuth进行保护的API

	```java
	@Configuration
	@EnableAutoConfiguration
	@EnableResourceServer
	public class WebSecurityConfigurer extends WebSecurityConfigurerAdapter {
	
	    private static final Logger logger = LoggerFactory.getLogger(WebSecurityConfigurer.class);
	    
	    public void configure(HttpSecurity http) throws Exception {
	        http
	            .antMatcher("/**")
	            .authorizeRequests()
	            .antMatchers("/", "/login**", "/webjars/**")
	            .permitAll()
	            .anyRequest()
	            .authenticated();
	    }
	}
```

3. 启动应用后，命令行根据OAuth规范通过用户的用户名和密码获取Access Token

	   *其中, --user 中的键值对为上面步骤创建的代表IAM应用的Service Providers的OAuth的`Client Key`和`Client Secret`*
	
	```bash
	curl \
	  --user MaiZnSC9lQsKI7LJdyprXgEtZK4a:v_3jFRZhhrGXEF2qGh0SWq7TFS8a \
	  -k \
	  -d "grant_type=password&username=admin&password=admin&scope=openid" \
	  -H "Content-Type:application/x-www-form-urlencoded"  \
	  https://localhost:9443/oauth2/token
	```
	调用成功后，会返回包含Access Token的json信息

	```json
	{
	    "access_token": "8b3d2846-2d1c-3bd6-9f31-9f3ef3cbc519",
	    "refresh_token": "df01c8c0-06a5-3461-b66b-1989b8fd1084",
	    "scope": "openid",
	    "id_token": "eyJ4NXQiOiJObUptT0dVeE16WmxZak0yWkRSaE5UWmxZVEExWXpkaFpUUmlPV0UwTldJMk0ySm1PVGMxWkEiLCJraWQiOiJkMGVjNTE0YTMyYjZmODhjMGFiZDEyYTI4NDA2OTliZGQzZGViYTlkIiwiYWxnIjoiUlMyNTYifQ.eyJhdF9oYXNoIjoiMFljRmVwdDlON3V6ek9HUWQ2UXJzQSIsImFjciI6InVybjptYWNlOmluY29tbW9uOmlhcDpzaWx2ZXIiLCJzdWIiOiJmbHkyd2luZEBjYXJib24uc3VwZXIiLCJhdWQiOlsiTWFpWm5TQzlsUXNLSTdMSmR5cHJYZ0V0Wks0YSJdLCJhenAiOiJNYWlablNDOWxRc0tJN0xKZHlwclhnRXRaSzRhIiwiaXNzIjoiaHR0cHM6XC9cL2xvY2FsaG9zdDo5NDQzXC9vYXV0aDJcL3Rva2VuIiwiZXhwIjoxNDg4NDQzNjU5LCJpYXQiOjE0ODg0NDAwNTl9.Iskihyn4cqTdHmOaq29VWJv3l2XzDuLTrquyVDcsIlnLqbU8s30uJH0iDleUxeReW99GyeOpCC2alwTXyxAtFdX_IL85oinM4KVQ0ghggqJU_wfMqiYcXGjU_mxcfbpW_r7LscQ2d4krgYB6zcPD6MaNVANRXv9QetfR3OCZq1g",
	    "token_type": "Bearer",
	    "expires_in": 3600
	}
	```
4. 使用上步获取的Access Token，构造包含认证token的请求访问应用中被保护的API

	```bash
	curl \
	--header "Authorization: Bearer 8b3d2846-2d1c-3bd6-9f31-9f3ef3cbc519" \
	http://demo.thoughtworks.local:8082/api
	``` 
    调用成功后，示例程序的API会返回访问该API的用户相关信息，如用户名、Access Token等
    
    ```json
    {
    "details": {
        "remoteAddress": "127.0.0.1",
        "sessionId": null,
        "tokenValue": "8b3d2846-2d1c-3bd6-9f31-9f3ef3cbc519",
        "tokenType": "Bearer",
        "decodedDetails": null
    },
    "authorities": [
        {
            "authority": "ROLE_USER"
        }
    ],
    "authenticated": true,
    "userAuthentication": {
        "details": {
            "sub": "admin@carbon.super"
        },
        "authorities": [
            {
                "authority": "ROLE_USER"
            }
        ],
        "authenticated": true,
        "principal": "unknown",
        "credentials": "N/A",
        "name": "unknown"
    },
    "credentials": "",
    "principal": "unknown",
    "clientOnly": false,
    "oauth2Request": {
        "clientId": "UyfDuAcKSjT8A2iC41EZRpRiZxIa",
        "scope": [],
        "requestParameters": {},
        "resourceIds": [],
        "authorities": [],
        "approved": true,
        "refresh": false,
        "redirectUri": null,
        "responseTypes": [],
        "extensions": {},
        "grantType": null,
        "refreshTokenRequest": null
    },
    "name": "unknown"
}
    ```

# Spring OAuth2 Demo
This is a simple OAuth2 demo whose server uses [Spring Boot](http://projects.spring.io/spring-boot),
[Spring Security OAuth2](http://projects.spring.io/spring-security-oauth) as well as [Spring MVC](http://docs.spring.io/spring/docs/current/spring-framework-reference/html/mvc.html). You may invoke the server pretty much any way you like (browser location, curl, postman) but you can also drop this [AngularJS client](/client) application in a web server.

Server is packaged as a web archive (`war`) deployed and tested in [Tomcat](http://tomcat.apache.org) (either regular or embedded with [Webapp Runner](https://github.com/jsimone/webapp-runner)). Regular Tomcat was used locally while embedded Tomcat was used both locally and in [Heroku](https://www.heroku.com).

- [Server](#server)
  - [Overview](#overview)
  - [Endpoints](#endpoints)
  - [Resource Endpoints](#resource-endpoints)
  - [Deployment](#deployment)
    - [Heroku and SSL Termination](#heroku)
    - [Local Tomcat](#local-tomcat)
    - [Local Embedded Tomcat](#local-embedded-tomcat)
- [Client](#client)
- [Examples and screenshots](#examples)
  - [Authorization Code Grant - Heroku](#authorization-code-grant-flow-heroku)
  - [Resource Owner Grant](#resource-owner-grant)
  - [Administration Application](#administration-application)

## Server
### Overview
Server was developed following the Spring OAuth2 [guide](http://projects.spring.io/spring-security-oauth/docs/oauth2.html) and combines both **Authorization Server** and **Resource Server** [OAuth roles](https://tools.ietf.org/html/rfc6749#section-1.1). It has the following features :
* Customized user login and consent pages (JSP views)
* A management page (JSP view) that enables regular users (`ROLE_USER`) to view tokens and what they have consented to clients. They can also revoke approvals and remove tokens they own. Administrators cannot access this page.
* An [AngularJS](https://angularjs.org) based administration application that allows administrators (`ROLE_ADMIN`) to manage all approvals and tokens. When an administrator signs in, they are automatically [password-granted](https://tools.ietf.org/html/rfc6749#section-4.3) an admin token that allows them to access users, clients, consents (user approvals) and tokens as Resources. Admin token is kept in a cookie named `ADMIN_ACCESS_TOKEN`. Regular Administrator (`admin`) can only view while a super admin (named `root`) can delete any approval or token.
* 11 registered clients (`client0` to `client10`). `client0` is reserved and represents the administration application.
  * Client `client0` is registered with scopes `ADMIN_READ` and `ADMIN_WRITE`.
  * Other clients  are registered with scopes `A`, `B` and `C`.
* 12 users (`root`, `admin` and `user1` to `user10`).
  * User `admin` has authority `ROLE_ADMIN`, can access the admin application but cannot delete approvals and tokens (because their `ADMIN_ACCESS_TOKEN` granted upon login is requested with scope `ADMIN_READ` only).
  * User `root` has authority `ROLE_ADMIN`, can access the admin application and delete any approval or token (because their `ADMIN_ACCESS_TOKEN` granted upon login is requested with both scopes `ADMIN_READ` and `ADMIN_WRITE`).
  * Other users have authority `ROLE_USER`. They cannot access the administration application.
* Configuration [classes](server/src/main/java/com/vnet/oa2/config) decide which [endpoints](server/src/main/java/com/vnet/oa2/config/ResourceServerConfig.java) are OAuth2 token controlled (Resource Server) and which [ones](server/src/main/java/com/vnet/oa2/config/HttpSecurityConfig.java) are subjected to regular http security. See Spring OAuth2 [guide](http://projects.spring.io/spring-security-oauth/docs/oauth2.html) for further details.


### Endpoints
|Method|Endpoint|Authentication Required|Comment|
|---|---|---|---|
|Spring defaults|`/oauth/authorize`<br>`/oauth/token`<br>`/oauth/check_token`|Yes|Spring defaults|
||`/oauth/error`|No|Custom OAuth Error view|
|GET|`/oauth/confirm_access`|Yes|Custom user consent page|
|GET|`/get_login`|No|Custom login page|
|POST|`/login`|No|Spring defaults|
|POST|`/logout`|Yes|Spring defaults|
|GET|`/test`|No|Test Page|
|GET|`/home`|Yes|Home Page|
|GET|`/app/admin`|Yes with `ROLE_ADMIN`|Access to administration application|
|GET|`/app/manage`|Yes with `ROLE_USER`|View approvals and tokens|
|POST|`/app/manage`|Yes with `ROLE_USER`|Submit approval revocations and token removals|

### Resource Endpoints
|Method|Endpoint|Required Role|Required Scope|Comment|
|---|---|---|---|---|
|GET|`/user`|Any|Any|Returns User Information|
|GET|`/admin/clients`|`ROLE_ADMIN`|`ADMIN_READ`|Client list|
|GET|`/admin/users`|`ROLE_ADMIN`|`ADMIN_READ`|User list|
|GET|`/admin/approvals`|`ROLE_ADMIN`|`ADMIN_READ`|Approval list|
|GET|`/admin/tokens`|`ROLE_ADMIN`|`ADMIN_READ`|Token list|
|DELETE|`/admin/approvals`|`ROLE_ADMIN`|`ADMIN_WRITE`|Revoke approval. Takes 3 query parameters (`user`, `client` and `scope`) for username, client id and scope.|
|DELETE|`/admin/tokens/{token-value}`|`ROLE_ADMIN`|`ADMIN_WRITE`|Remove token|
|GET|`/things/A/{id}`|`ROLE_USER`|`A`|A Things|
|GET|`/things/B/{id}`|`ROLE_USER`|`B`|B Things|
|GET|`/things/C/{id}`|`ROLE_USER`|`C`|C Things|

## Deployment
### Heroku
#### Deploy Application
* Sign up for Heroku free tier in case you do not yet have an account. Create an application in Heroku : either on the Heroku portal or with the [Heroku Command Line](https://devcenter.heroku.com/categories/command-line) tools (extremely convenient). [Here](https://devcenter.heroku.com/articles/creating-apps) is how to create an application from the command line : you can name it yourself or let Heroku name it nicely for you. Either way, the application name must be added to the configuration of the Heroku mvn plugin section.
* Add the Heroku mvn [plugin](https://devcenter.heroku.com/articles/deploying-java-applications-with-the-heroku-maven-plugin) to your [pom.xml](server/pom.xml) and configure it with your application name. Make sure the pom.xml packaging is `war`. Deploy it with `mvn heroku:deploy-war`.
* On the Heroku portal, you can view how the application gets started with the embedded Tomcat container :
  * `java $JAVA_OPTS -jar target/dependency/webapp-runner.jar $WEBAPP_RUNNER_OPTS --port $PORT target/demoa2-1.0.war`
* Once the application is up and running, both http and https endpoints are available :
  * `http://YourAppName.herokuapp.com`
  * `https://YourAppName.herokuapp.com`
  * My application name was `demoa2`.

#### SSL Termination
* Heroku load balancing **terminates SSL** and **all** requests (even when initiated at Heroku HTTPS endpoints) will reach your web application over HTTP (with appropriate `x-forwarded-*` headers though). This is fine as long as your application does not include **redirected** conversations that you want to start and continue over **https** all the way through.
* If your application redirects to itself, [Webapp Runner 8](https://github.com/jsimone/webapp-runner) has the solution : its `--proxy-base-url` option tells your web application that incoming requests are being proxied and redirect URLs will consequently be properly constructed. Now, the second piece of luck is that Heroku commands will let you set webapp runner options. I issued the following for my application :
```sh
    heroku config:set WEBAPP_RUNNER_OPTS="--proxy-base-url https://demoa2.herokuapp.com" --app demoa2
```
* Conversely, http invocations of heroku app endpoints will be automatically redirected to their https counterparts.
* **CAREFUL** _Webapp Runner version 7 does **not** have the `--proxy-base-url` option. If you are stuck with webapp runner 7, I unfortunately do not have a solution available despite reading tons of Spring literature about valves, embedded containers and forwarded headers (some [here](http://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-developing-web-applications.html#boot-features-embedded-container) and a bit more over [there](http://docs.spring.io/spring-boot/docs/current/reference/html/howto-embedded-servlet-containers.html))_.

### Local Tomcat
* Web application is built with `mvn package` and gets deployed as a sub-directory of `$TOMCAT_HOME/webapps`. Tomcat deployment is not included in my pom.xml and deployment is manually handled with a deploy [script](server/tomcat-deploy.sh). Without changing Tomcat defaults, application is available at `http://localhost:8080/demoa2` (the script renames `demoa2.war` the archive dropped in `$TOMCAT_HOME/webapps`). This also creates a context path mismatch in static resources and that is why the deployment script adds the `demoa2` piece wherever required. There is no issue for JSPs whose context path is dynamically set using `${pageContext.request.contextPath}` variable.

* There is no `web.xml` file in `WEB-INF` folder. See in [pom.xml](server/pom.xml) how `maven-war-plugin` is told not to fail on missing `web.xml`.

* **CAREFUL** In order for Spring Boot to support a deployable war (as opposed to just running with `mvn spring-boot:run`), it has to use `spring-boot-starter-tomcat`. Spring documentation explains it [here](http://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#howto-create-a-deployable-war-file).

* If Tomcat is used behind a front-end proxy server, it does not know either (like [webapp runner at Heroku](#ssl-termination)) whether requests are being proxied. One way to tell Tomcat incoming requests are being proxied is to change the `Connector` settings in `$TOMCAT_HOME/conf/server.xml`.
```xml
    <!-- Default settings -->
    <Connector port="8080" protocol="HTTP/1.1"
               connectionTimeout="20000"
               redirectPort="8443" />
```
```xml
    <!-- Settings for proxying with Nginx from https://localhost to http://localhost:8080 -->
    <Connector port="8080" protocol="HTTP/1.1"
               connectionTimeout="20000"
               redirectPort="8443"
               proxyName="localhost" proxyPort="443" scheme="https" />
```

* If you take a peek at webapp runner [code](https://github.com/jsimone/webapp-runner/blob/master/src/main/java/webapp/runner/launch/Main.java), you can see it programmatically does the equivalent of the `Connector` settings when processing its `--proxy-base-url` option.

### Local Embedded Tomcat
* Configure your `pom.xml` so that it downloads Webapp Runner (*Very well explained [here](https://devcenter.heroku.com/articles/java-webapp-runner#configure-maven-to-download-webapp-runner) in Heroku documentation but you can of course do this without having anything to do with Heroku*).
* Package the application with `mvn package`. Run the archive with :
```sh
    java -jar target/dependency/webapp-runner.jar --port 8080 target/demoa2-1.0.war
```
* You can also run the exploded war :
```sh
    java -jar target/dependency/webapp-runner.jar --port 8080 target/demoa2-1.0
```
* It of course has the same proxying issues as with [Heroku](#ssl-termination). If you for example run Nginx at `https://localhost`, you can proxy accordingly with :
```sh
    java -jar target/dependency/webapp-runner.jar --proxy-base-url https://localhost target/demoa2-1.0
```
* `--port` is optional. Default is `8080`.

## Client
* There is no automated packaging provided and you can directly drop all the [files](client) in a web server. The only thing you may need to adapt (to your server deployment) is `oa2BaseURL` at the top of [app.js](client/app.js).
* Should you want to deploy this client in heroku, you can package up all the files the [files](client) into a `war` file and deploy it directly into Heroku (without mvn). Here is what you can do :
  * Get all the files into a directory and `cd` to that directory
  * Create archive with `jar cvf ../oa2client.war *`
  * Go back one directory and deploy it :  
`heroku deploy:war oa2client.war --app yourHerokuClientApp`
  * CAREFUL `deploy:war` is not a default Heroku command and if it complains it is not there, you have to install the following plugin beforehand:  
`heroku plugins:install heroku-cli-deploy`
* Application can play the role of any registered client (`client1` to `client10`). It has a setup phase that pulls the client list from the server (and presents them in a drop down list). The setup phase will prompt you for admin credentials in order to be able to pull the client list from the server. Past the setup phase, you play the Client role.
* Application is purely client-side (AngularJS) but illustrates the OAuth2 [Authorization Code Grant](https://tools.ietf.org/html/rfc6749#section-4.1) flow where Authorization Code is acquired to be later exchanged for an access token. Please **note** that client side applications are more likely to use other grant methods ([Implicit](https://tools.ietf.org/html/rfc6749#section-4.2) and [Resource Owner Credentials](https://tools.ietf.org/html/rfc6749#section-4.3)) since **Authorized Code Grant** is better suited for confidential clients.

## Examples
### Authorization Code Grant flow (Heroku)
* This uses Heroku free tier whose applications go down after 30 minutes of inactivity and restart upon first hit. You can use this [test page](https://demoa2.herokuapp.com/test) to check whether the application is up and running. If it is not, you will get this error page :  
<img src="https://cloud.githubusercontent.com/assets/13286393/17226997/0d87253e-54c1-11e6-83b8-48fa25f374d4.png" border="0" width="70%" />

* Refresh once or twice and you should see the following when it is finally up :  
<img src="https://cloud.githubusercontent.com/assets/13286393/17226999/0d88ad00-54c1-11e6-8ade-b1535c32a2a5.png" border="0" width="70%" />

* Point your browser to the following location :
```
https://demoa2.herokuapp.com/oauth/authorize?response_type=code&client_id=client7&redirect_uri=http://example.com
```

* You get redirected to User Sign-in page.  
<img src="https://cloud.githubusercontent.com/assets/13286393/17226998/0d877034-54c1-11e6-8943-4e7c58c7831a.png" border="0" width="70%" />

* Then on to consent page.  
<img src="https://cloud.githubusercontent.com/assets/13286393/17227000/0d8d0b48-54c1-11e6-8bdb-39af5710b8e8.png" border="0" width="70%" />

* Final redirection with authorization code.  
<img src="https://cloud.githubusercontent.com/assets/13286393/17226996/0d7f827a-54c1-11e6-8719-722744defd51.png" border="0" width="70%" />

* Exchange code for token :
```
curl -u client7:P@55w0rd7 https://demoa2.herokuapp.com/oauth/token \
     -d grant_type=authorization_code \
     -d client_id=client7 \
     -d redirect_uri=http://example.com \
     -d code=P8tZ32

{"access_token":"77bf32e1-11eb-4bd6-82fc-4d0ca124f896",
 "token_type":"bearer",
 "refresh_token":"85091f22-e779-4904-a453-a9b335fbb92f",
 "expires_in":43199,
 "scope":"A C"}
```
* Get User Information (_Response manually indented_).
```
curl -H "Authorization: Bearer 77bf32e1-11eb-4bd6-82fc-4d0ca124f896" \
    https://demoa2.herokuapp.com/user

{"details":{
     "remoteAddress":"10.5.220.194",
     "sessionId":null,
     "tokenValue":"77bf32e1-11eb-4bd6-82fc-4d0ca124f896",
     "tokenType":"Bearer",
     "decodedDetails":null
 },
 "authorities":[{"authority":"ROLE_USER"}],
 "authenticated":true,
 "userAuthentication":{
     "details":{"remoteAddress":"10.35.237.138","sessionId":"0E52DA55058011D321A3F7CCCAF9F7BC"},
     "authorities":[{"authority":"ROLE_USER"}],
     "authenticated":true,
     "principal":"user4",
     "credentials":null,
     "name":"user4"
     },
 "clientOnly":false,
 "credentials":"",
 "oauth2Request":{
     "clientId":"client7",
     "scope":["A","C"],
     "requestParameters":{
         "response_type":"code",
         "redirect_uri":"http://example.com",
         "code":"P8tZ32",
         "grant_type":"authorization_code",
         "client_id":"client7"
     },
     "resourceIds":["demoa2"],
     "authorities":[],
     "approved":true,
     "refresh":false,
     "redirectUri":"http://example.com",
     "responseTypes":["code"],
     "extensions":{},
     "refreshTokenRequest":null,
     "grantType":"authorization_code"},
 "principal":"user4",
 "name":"user4"
}
```

* Get a resource that requires having scope C
```
curl -H "Authorization: Bearer 77bf32e1-11eb-4bd6-82fc-4d0ca124f896" \
    https://demoa2.herokuapp.com/things/C/123

{"requestedBy":"user4",
 "scopedBy":"C",
 "method":"getThingsC",
 "requestedAt":1469734272758,
 "id":"123",
 "class":"com.vnet.oa2.endpoints.Things"}
```

* Getting a resource that requires having scope B is denied because user only consented to scopes A and C.
```
curl -H "Authorization: Bearer 77bf32e1-11eb-4bd6-82fc-4d0ca124f896" \
    https://demoa2.herokuapp.com/things/B/456

{"error":"insufficient_scope","error_description":"Insufficient scope for this resource","scope":"B"}
```

* Point your browser to `https://demoa2.herokuapp.com/app/manage`.  
<img src="https://cloud.githubusercontent.com/assets/13286393/17227975/e068daca-54c5-11e6-9034-f286f83c3b07.png" border="0" width="80%" />

### Resource Owner Grant
* Get Token
```
curl -u client1:P@55w0rd1 https://demoa2.herokuapp.com/oauth/token \
     -d grant_type=password \
     -d username=user4 \
     -d password=password4 \
     -d scope=A

{"access_token":"41be7af3-22b7-47b9-aff4-ee5ec04cf0e4",
 "token_type":"bearer",
 "refresh_token":"be919705-5ad9-4fb1-b717-4a8208df5101",
 "expires_in":43199,
 "scope":"A"}
```

* Point your browser to `https://demoa2.herokuapp.com/app/manage`.  
<img src="https://cloud.githubusercontent.com/assets/13286393/17228651/b8703dc6-54c8-11e6-8db3-b1596c320df7.png" border="0" width="80%" />

### Administration Application

* Point your browser to `https://demoa2.herokuapp.com/app/admin` and sign-in with admin credentials. Welcome page :
<img src="https://cloud.githubusercontent.com/assets/13286393/17230448/16c39c12-54d1-11e6-8de3-1c013c19b1b6.png" border="0" width="70%" />
* Client List  
<img src="https://cloud.githubusercontent.com/assets/13286393/17230444/16b31e3c-54d1-11e6-9666-3441adb47da5.png" border="0" width="70%" />
* User List  
<img src="https://cloud.githubusercontent.com/assets/13286393/17230445/16b3cca6-54d1-11e6-951b-c617dcf5e965.png" border="0" width="70%" />
* Approvals  
<img src="https://cloud.githubusercontent.com/assets/13286393/17230446/16b41c4c-54d1-11e6-961c-95c40b93955a.png" border="0" width="70%" />
* Tokens  
<img src="https://cloud.githubusercontent.com/assets/13286393/17230447/16b90a2c-54d1-11e6-8305-58113a1c8379.png" border="0" width="70%" />
* Token details  
<img src="https://cloud.githubusercontent.com/assets/13286393/17230442/16abcc04-54d1-11e6-95d1-299df79f783f.png" border="0" width="70%" />
* When you sign-in with super admin credentials, you can delete approvals and tokens.  
<img src="https://cloud.githubusercontent.com/assets/13286393/17230443/16af7df4-54d1-11e6-8289-104a359d2cb8.png" border="0" width="70%" />


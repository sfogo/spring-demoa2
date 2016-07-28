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

## Server
### Overview
Server was developed following the Spring OAuth2 [guide](http://projects.spring.io/spring-security-oauth/docs/oauth2.html) and combines both **Authorization Server** and **Resource Server** [OAuth roles](https://tools.ietf.org/html/rfc6749#section-1.1). It includes :
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
  * `http://myAppName.herokuapp.com`
  * `https://myAppName.herokuapp.com`

#### SSL Termination
* Heroku load balancing **terminates SSL** and **all** requests (even when initiated at Heroku HTTPS endpoints) will reach your web application over HTTP (with appropriate `x-forwarded-*` headers though). This is fine as long as your application does not include **redirected** conversations that you want to start and continue over **https** all the way through.
* If your application redirects to itself, [Webapp Runner 8](https://github.com/jsimone/webapp-runner) has the solution : its `--proxy-base-url` option tells your web application that incoming requests are being proxied and redirect URLs will consequently be properly constructed. Now, the second piece of **luck** is that Heroku commands will let you set webapp runner options. I issued the following for my application :
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
* There is no automated packaging and you can directly drop all the [files](client) in a web server. The only thing you may need to adapt (to your server deployment) is `oa2BaseURL` at the top of [app.js](client/app.js).
* Application can play the role of any registered client (`client1` to `client10`). It has a setup phase that pulls the client list from the server (and presents them in a drop down list). The setup phase will prompt you for admin credentials in order to be able to pull the client list from the server. Past the setup phase, you play the Client role.
* Application is purely client-side (AngularJS) but illustrates the OAuth2 [Authorization Code Grant](https://tools.ietf.org/html/rfc6749#section-4.1) where Authorization Code is exchanged for an access token. Please **note** that client side applications are more likely to use other grant methods ([Implicit](https://tools.ietf.org/html/rfc6749#section-4.2) and [Resource Owner Credentials](https://tools.ietf.org/html/rfc6749#section-4.3)). **Authorized Code Grant** is better suited for confidential clients.

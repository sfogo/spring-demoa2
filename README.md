# Spring OAuth2 Demo
This is a simple OAuth2 demo application whose server uses [Spring Boot](http://projects.spring.io/spring-boot),
[Spring Security OAuth2](http://projects.spring.io/spring-security-oauth) as well as [Spring MVC](http://docs.spring.io/spring/docs/current/spring-framework-reference/html/mvc.html). You may invoke the server pretty much any way you like (browser location, curl, postman) but you can also drop this [client](/client) application in a web server.

Server is packaged as a web archive (`war`). It has been deployed and tested in the following conditions :
* Embedded Tomcat in Heroku
* Local HTTP Tomcat
* Local HTTP Embedded Tomcat ([Webapp Runner](https://github.com/jsimone/webapp-runner))
* Local HTTPS Nginx + HTTP Tomcat
* Local HTTPS Nginx + HTTP Embedded Tomcat (Webapp Runner)

## Server
### Overview
Server was developed following the Spring OAuth2 [guide](http://projects.spring.io/spring-security-oauth/docs/oauth2.html) but it includes the following :
* It combines **Authorization Server** and **Resource Server** [OAuth roles](https://tools.ietf.org/html/rfc6749#section-1.1).
* Customized user login and consent pages (JSP views)
* A management page (JSP view) that enables regular users (`ROLE_USER`) to view tokens and what they have consented to clients. They can also revoke approvals and remove tokens they own. Administrators cannot access this page.
* An [AngularJS](https://angularjs.org) based administration application that allows administrators (`ROLE_ADMIN`) to manage all approvals and tokens. When an administrator signs in, they are automatically [password-granted](https://tools.ietf.org/html/rfc6749#section-4.3) an admin token that allows them to access users, clients, consents (user approvals) and tokens as Resources. Admin token is kept in a cookie named `ADMIN_ACCESS_TOKEN`. Regular Administrator (`admin`) can only view while Super Administrator (`root`) can delete any approval or token.
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
|POST|`/login`|No|Spring defaults|
|POST|`/logout`|Yes|Spring defaults|
|GET|`/oauth/confirm_access`|Yes|Custom user consent page|
|GET|`/get_login`|No|Custom login page|
|GET|`/app/admin`|Yes with `ROLE_ADMIN`|Access to administration application|
|GET|`/app/manage`|Yes with `ROLE_USER`|View approvals and tokens|
|POST|`/app/manage`|Yes with `ROLE_USER`|Submit revokes and token removals|

### Resource Endpoints
|Method|Endpoint|Required Role|Required Scope|Comment|
|---|---|---|---|---|
|GET|`/user`|Any|Any|Returns User Information|
|GET|`/admin/clients`|`ROLE_ADMIN`|`ADMIN_READ`|Client list|
|GET|`/admin/users`|`ROLE_ADMIN`|`ADMIN_READ`|User list|
|GET|`/admin/approvals`|`ROLE_ADMIN`|`ADMIN_READ`|Approval list|
|GET|`/admin/tokens`|`ROLE_ADMIN`|`ADMIN_READ`|Token list|
|DELETE|`/admin/approvals`|`ROLE_ADMIN`|`ADMIN_WRITE`|Revoke approval|
|DELETE|`/admin/tokens`|`ROLE_ADMIN`|`ADMIN_WRITE`|Remove token|
|GET|`/things/A/{id}`|`ROLE_USER`|`A`|A Things|
|GET|`/things/B/{id}`|`ROLE_USER`|`B`|B Things|
|GET|`/things/C/{id}`|`ROLE_USER`|`C`|C Things|

## Deployment
### Heroku
* Sign up for Heroku free tier in case you do not yet have an account. Create an application in Heroku : either on the Heroku portal or with the [Heroku Command Line](https://devcenter.heroku.com/categories/command-line) tools (extremely convenient). [Here](https://devcenter.heroku.com/articles/creating-apps) is how to create an application from the command line : you can name it yourself or let Heroku name it nicely for you. Either way, the application name must be added to the configuration of the Heroku mvn plugin section.
* Add the Heroku mvn [plugin](https://devcenter.heroku.com/articles/deploying-java-applications-with-the-heroku-maven-plugin) to your [pom.xml](server/pom.xml) and configure it with your application name. Make sure the pom.xml packaging is `war`.
* Deploy it with :
  * `$ mvn heroku:deploy-war`
* On the Heroku portal, you can view how it gets started inside the embedded Tomcat container :
  * `java $JAVA_OPTS -jar target/dependency/webapp-runner.jar $WEBAPP_RUNNER_OPTS --port $PORT target/demoa2-1.0.war`
* Once the application is up and running, both http and https endpoints are available :
  * `http://myAppName.herokuapp.com`
  * `https://myAppName.herokuapp.com`
* **Important HTTPS Note**
  * Heroku load balancing **terminates SSL** and **all** requests (even when initiated from Heroku HTTPS endpoints) will reach your web application over HTTP (with appropriate `x-forwarded-*` headers though). This is fine as long as your application does not include **redirect**ed conversations that you want to start and continue over **https** all the way through.
  * If your application redirects to itself, [Webapp Runner 8](https://github.com/jsimone/webapp-runner) has the solution : its `--proxy-base-url` option tells your web application that incoming requests are being proxied and redirect URLs will consequently be properly constructed. Now, the second piece of **luck** is that Heroku commands will let you set webapp runner options. I issued the following for my application :
  * `heroku config:set WEBAPP_RUNNER_OPTS="--proxy-base-url https://demoa2.herokuapp.com" --app demoa2`
  * Conversely, http invocations of heroku app endpoints will be automatically redirected to their https counterparts.
  * **CAREFUL** : webapp runner version 7 does **not** have the `--proxy-base-url` option. If you are stuck with webapp runner 7, I unfortunately do not have a solution available despite reading tons of Spring literature about valves, embedded containers and forwarded headers (some [here](http://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-developing-web-applications.html#boot-features-embedded-container) and a bit more over [there](http://docs.spring.io/spring-boot/docs/current/reference/html/howto-embedded-servlet-containers.html)).

### Local Tomcat
### Local Embedded Tomcat

# Spring OAuth2 Demo
This is a simple OAuth2 demo application whose server uses [Spring Boot](http://projects.spring.io/spring-boot),
[Spring Security OAuth2](http://projects.spring.io/spring-security-oauth) as well as [Spring MVC](http://docs.spring.io/spring/docs/current/spring-framework-reference/html/mvc.html). You may invoke the server pretty much any way you like (browser location, curl, postman) but you can also drop this [client](/client) application in a web server.

## Server
### Overview
Server was developed following the Spring OAuth2 [guide](http://projects.spring.io/spring-security-oauth/docs/oauth2.html) but it includes the following :
* It combines **Authorization Server** and **Resource Server** [OAuth roles](https://tools.ietf.org/html/rfc6749#section-1.1).
* Customized user login and consent pages (JSP views)
* A management page (JSP view) that enables regular users (`ROLE_USER`) to view tokens and what they have consented to clients. They can also revoke approvals and remove tokens.
* An [AngularJS](https://angularjs.org) based administration application that allows administrators (`ROLE_ADMIN`) to revoke consents and remove tokens. This application is a bit drastic as it allows administrators to revoke any (User, Client, Scope) approval triplet as well as remove tokens. When an administrator signs in, they are automatically [password-granted](https://tools.ietf.org/html/rfc6749#section-4.3) an admin token that allows them to access users, clients, consents (user approvals) and tokens as Resources. Admin token is kept in a cookie named `ADMIN_ACCESS_TOKEN`.
* 11 registered clients (`client0` to `client10`). `client0` is reserved and represents the administration application.
  * Client `client0` is registered with scopes `ADMIN_READ` and `ADMIN_WRITE`.
  * Other clients  are registered with scopes `A`, `B` and `C`.
* 11 users (`admin` and `user1` to `user10`). Only `admin` can access the administration application.
  * User `admin` has authority `ROLE_ADMIN`
  * Other users have authority `ROLE_USER`


### Endpoints
|Method|Endpoint|Role|Comment|
|---|---|---|---|
|Spring defaults|`/oauth/authorize`<br>`/oauth/token`<br>`/oauth/check_token`|Any|Spring defaults|
|POST|`/login`<br>`/logout`|Any|Spring defaults|
|GET|`/oauth/confirm_access`|Any|Custom user consent page|
|GET|`/get_login`|Any|Custom login page|
|GET|`/app/admin`|`ROLE_ADMIN`|Access to administration application|
|GET|`/app/manage`|`ROLE_USER`|View approvals and tokens|
|POST|`/app/manage`|`ROLE_USER`|Submit revokes and token removals|

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

# Spring OAuth2 Demo
This is a simple OAuth2 demo application whose server uses [Spring Boot](http://projects.spring.io/spring-boot),
[Spring Security OAuth2](http://projects.spring.io/spring-security-oauth) as well as [Spring MVC](http://docs.spring.io/spring/docs/current/spring-framework-reference/html/mvc.html). You may invoke the server pretty much any way you like (browser location, curl, postman) but you can also drop the [client](/client) application in a web server.

## Server
### Overview
Server was developed following the Spring OAuth2 [guide](http://projects.spring.io/spring-security-oauth/docs/oauth2.html) but it includes the following :
* It combines **Authorization Server** and **Resource Server** [OAuth roles](https://tools.ietf.org/html/rfc6749#section-1.1).
* Customized user login and consent pages (JSP views)
* An [AngularJS](https://angularjs.org) based administration application that allows an administrator to revoke consents and remove tokens.
* 11 registered clients (`client0` to `client10`). `client0` is reserved and represents the administration application.
  * Client `client0` is registered with scopes `ADMIN_READ` and `ADMIN_WRITE`.
  * Other clients  are registered with scopes `A`, `B` and `C`.
* 11 users (`admin` and `user1` to `user10`). Only `admin` can access the administration application.
  * User `admin` has authority `ROLE_ADMIN`
  * Other users have authority `ROLE_USER`

When an administrator signs in, they are automatically [password-granted](https://tools.ietf.org/html/rfc6749#section-4.3) an admin token that allows them to access users, clients, consents (user approvals) and tokens. Admin token is kept in a cookie named `ADMIN_ACCESS_TOKEN`.

### Endpoints

# Spring OAuth2 Demo
This is a simple OAuth2 demo application whose server uses [Spring Boot](http://projects.spring.io/spring-boot),
[Spring Security OAuth2](http://projects.spring.io/spring-security-oauth) as well as [Spring MVC](http://docs.spring.io/spring/docs/current/spring-framework-reference/html/mvc.html). You may invoke the server pretty much any way you like (browser location, curl, postman) but you can also drop the [client](/client) application in a web server.

## Server
Server was developed following the Spring OAuth2 [guide](http://projects.spring.io/spring-security-oauth/docs/oauth2.html) but it includes a few custom features :
* Customized user login and consent pages (JSP views)
* An [AngularJS](https://angularjs.org) based administration application that allows an administrator to revoke consents and remove tokens.
* 11 registered clients (`client0` to `client10`)
* 11 users (`admin` and `user1` to `user10`)

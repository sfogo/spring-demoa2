<%@ page language="java" %>
<%@ page import="org.springframework.security.web.WebAttributes"%>

<%@ taglib prefix="c"      uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}"/>

<html xmlns:jsp="http://java.sun.com/JSP/Page">
<head>
    <title>User Sign In</title>
    <link href="${contextPath}/css/style.css" rel="stylesheet">
    <link href="${contextPath}/css/buttons.css" rel="stylesheet">
</head>

<body class="base">

<jsp:include page="fragments/header.jsp"/>
<div class="aBox cw80 pad4 bm10 tm10">

    <form id="loginForm"
          name="loginForm"
          action="${contextPath}/login"
          method="post">

        <jsp:include page="fragments/csrf.jsp"/>

        <table style="margin:10 auto;">
            <tr>
                <td class="base iotValue">Username</td>
                <td><input class="w200p" type="text" id="username" name="username"/></td>
            </tr>

            <tr>
                <td class="base iotValue">Password</td>
                <td><input class="w200p" type="password" id="password" name="password"/></td>
            </tr>

            <% if (session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION) != null) { %>
            <tr>
                <td class="base">&nbsp</td>
                <td class="base"><span class="ex01">${SPRING_SECURITY_LAST_EXCEPTION.message}</span></td>
            </tr>
            <% } %>

            <tr align="center">
                <td class="base">&nbsp</td>
                <td><button class="button1" name="signin" type="submit"><span>Sign In</span></button></td>
            </tr>

        </table>
    </form>

</div>
<jsp:include page="fragments/footer.jsp"/>

<div id="debug" class="aBox cw80 pad4 tm10 hidden">
    <!-- ============ -->
    <!-- Request Data -->
    <!-- ============ -->
    <jsp:include page="fragments/requestData.jsp"/>

    <!-- =============== -->
    <!-- Request Headers -->
    <!-- =============== -->
    <jsp:include page="fragments/requestHeaders.jsp"/>

    <!-- ================== -->
    <!-- Request Parameters -->
    <!-- ================== -->
    <jsp:include page="fragments/requestParameters.jsp"/>

    <!-- ================== -->
    <!-- Session Attributes -->
    <!-- ================== -->
    <jsp:include page="fragments/sessionAttributes.jsp"/>

    <!-- ================== -->
    <!-- Request Attributes -->
    <!-- ================== -->
    <jsp:include page="fragments/requestAttributes.jsp"/>
</div>

</body>

</html>
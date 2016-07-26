<%@ page language="java" %>
<%@ page import="java.util.Map"%>

<%@ taglib prefix="c"      uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}"/>

<html xmlns:jsp="http://java.sun.com/JSP/Page">
<head>
    <title>User Consent</title>
    <link href="${contextPath}/css/style.css" rel="stylesheet">
    <link href="${contextPath}/css/buttons.css" rel="stylesheet">
</head>
</head>

<body class="base">

<jsp:include page="fragments/header.jsp"/>
<div class="aBox cw80 pad4 bm10 tm10">

    <div class="base pad4 cw80">Do you authorize <span class="iotValue">${authorizationRequest.clientId}</span>
    at ${authorizationRequest.redirectUri} to access your protected resources?</div>

    <form id="approvalForm"
          name="approvalForm"
          action="${contextPath}/oauth/authorize"
          method="post">
        <input name="user_oauth_approval" value="true" type="hidden"/>
        <jsp:include page="fragments/csrf.jsp"/>

        <table style="margin:10 auto;">
            <%
            Map scopes = (Map) request.getAttribute("scopes");
            for (Object scope : scopes.keySet()) {
            %>
            <tr valign="center">
                <td class="base pad4"><label><span class="iotValue"><%= scope %></span></label></td>
                <td class="base pad4" align="center">
                    <% if ("true".equals(scopes.get(scope))) { %>
                    <label><input class="malign" type="radio" name="<%= scope %>" value="true" checked="checked"/><span>Approve</span></label>
                    <label><input class="malign" type="radio" name="<%= scope %>" value="false"/><span>Deny</span></label>
                    <% } else { %>
                    <label><input class="malign" type="radio" name="<%= scope %>" value="true"/><span>Approve</span></label>
                    <label><input class="malign" type="radio" name="<%= scope %>" value="false" checked="checked"/><span>Deny</span></label>
                    <% } %>
                </td>
            </tr>
            <%
            }
            %>
            <tr>
                <td colspan="2" align="center">
                    <button class="button1" type="submit"><span>Submit</span></button>
                </td>
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
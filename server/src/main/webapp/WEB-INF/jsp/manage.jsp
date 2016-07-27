<%@ page language="java" %>
<%@ page import="java.util.Collection"%>
<%@ page import="com.vnet.oa2.Utils"%>
<%@ page import="org.springframework.security.oauth2.provider.approval.Approval"%>
<%@ page import="org.springframework.security.oauth2.common.OAuth2AccessToken"%>

<%@ taglib prefix="c"      uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}"/>

<html xmlns:jsp="http://java.sun.com/JSP/Page">
<head>
    <title>Approvals</title>
    <link href="${contextPath}/css/style.css" rel="stylesheet">
</head>
</head>

<body class="base">

<jsp:include page="fragments/header.jsp"/>
<div class="aBox cw80 pad4 bm10 tm10">

    <form id="manageForm"
          name="manageForm"
          action="${contextPath}/app/manage"
          method="post">
        <jsp:include page="fragments/csrf.jsp"/>

        <div class="base cw90 bg5 bm10 tm10 pad4"><span class="ex00">Approval Records</span></div>

        <%
        boolean hasApprovals = false;
        boolean hasTokens = false;
        Collection<Approval> approvals = (Collection<Approval>)request.getAttribute("_approvals");
        if (approvals!=null && approvals.size() > 0) {
            hasApprovals = true;
        %>
            <table class="tb01 base cw90">
                <tr>
                    <td class="td00"><span class="ex00">Client</span></td>
                    <td class="td00"><span class="ex00">Scope</span></td>
                    <td class="td00"><span class="ex00">Status</span></td>
                    <td class="td00"><span class="ex00">Expires</span></td>
                    <td class="td00"><span class="ex00">Updated</span></td>
                    <td class="td00"><span class="ex00">&nbsp;</span></td>
                </tr>
                <%
                for (Approval approval : approvals) {
                    Utils.InputKey key = new Utils.InputKey("approval", approval.getClientId(), approval.getScope());
                %>
                <tr>
                    <td class="tdUP"><span><%= approval.getClientId() %></span></td>
                    <td class="tdUP"><span><%= approval.getScope() %></span></td>
                    <td class="tdUP"><span><%= approval.getStatus() %></span></td>
                    <td class="tdUP"><span><%= approval.getExpiresAt() %></span></td>
                    <td class="tdUP"><span><%= approval.getLastUpdatedAt() %></span></td>
                    <td class="tdUP">
                        <label><input class="malign" type="radio" name="<%= key.getValue() %>" value="false" checked="checked"/><span>Keep</span></label>
                        <label><input class="malign" type="radio" name="<%= key.getValue() %>" value="true"/><span>Remove</span></label>
                    </td>
                </tr>
            <% } %>
            </table>
        <%} else { %>
            <div class="base cw90 bm10 tm10 pad4"><span>No approval records found.</span></div>
        <% } %>

        <div class="base cw90 bg5 bm10 tm10 pad4"><span class="ex00">Tokens</span></div>
        <%
        Collection<OAuth2AccessToken> tokens = (Collection<OAuth2AccessToken>) request.getAttribute("_tokens");
        if (tokens!=null && tokens.size() > 0) {
            hasTokens = true;
        %>
            <table class="tb01 base cw90">
                <tr>
                    <td class="td00"><span class="ex00">Client</span></td>
                    <td class="td00"><span class="ex00">Value</span></td>
                    <td class="td00"><span class="ex00">Type</span></td>
                    <td class="td00"><span class="ex00">Scopes</span></td>
                    <td class="td00"><span class="ex00">Expires</span></td>
                    <td class="td00"><span class="ex00">&nbsp;</span></td>
                </tr>
            <%
            for (OAuth2AccessToken token : tokens) {
                Utils.InputKey key = new Utils.InputKey("token","foo",token.getValue());
            %>
                <tr>
                    <td class="tdUP"><%= token.getAdditionalInformation().get("clientId") %></td>
                    <td class="tdUP"><%= token.getValue() %></td>
                    <td class="tdUP"><%= token.getTokenType() %></td>
                    <td class="tdUP"><%= token.getScope() %></td>
                    <td class="tdUP"><%= token.getExpiration() %></td>
                    <td class="tdUP">
                        <label><input class="malign" type="radio" name="<%= key.getValue() %>" value="false" checked="checked"/><span>Keep</span></label>
                        <label><input class="malign" type="radio" name="<%= key.getValue() %>" value="true"/><span>Remove</span></label>
                    </td>
                </tr>
            <% } %>
            </table>
        <% } else { %>
            <div class="base cw90 bm10 tm10 pad4"><span>No tokens found.</span></div>
        <%
        }

        if (hasApprovals || hasTokens) { %>
            <div class="base cw90 bm10 tm10 pad4"><button type="submit"><span>Submit</span></button></div>
        <% } %>
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
<%@ page import="com.vnet.oa2.server.UserHelper"%>
<%@ taglib prefix="c"      uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}"/>

<% final UserHelper user = new UserHelper(request); %>
<div class="aBox cw80 pad4 bg1">
    <table style="border-collapse:collapse; align:center; width:100%;">
        <tr>
        <td class="base td00">
            <% if (request.getAttribute("_headerText") == null) { %>
                <span class="iotValue">Authorization Server</span>
            <% } else { %>
                <span class="iotValue">Authorization Server&nbsp;::&nbsp;<%= request.getAttribute("_headerText").toString() %></span>
            <% } %>
        </td>

        <td class="base td00R">
        <% if (user.isThere()) { %>
            Logged in as <span class="iotValue"><%= user.getUsername() %></span>&nbsp;
            <% if (user.isAdmin()) { %>
                <a class="elm01" href="${contextPath}/app/admin">Administer</a>&nbsp;
            <% } else { %>
                <a class="elm01" href="${contextPath}/app/manage">Manage</a>&nbsp;
            <% } %>
            <a class="elm01" href="#" onclick="submitLogoutForm();">Logout</a>
        <% } %>
        </td>
        </tr>
    </table>
</div>

<% if (user.isThere()) { %>
<div>
    <script type='text/javascript'>
    function submitLogoutForm() {document.getElementById("logoutForm").submit();}
    </script>

    <form id="logoutForm"
          name="logoutForm"
          action="${contextPath}/logout"
          method="post">
        <jsp:include page="csrf.jsp"/>
    </form>
</div>
<% } %>

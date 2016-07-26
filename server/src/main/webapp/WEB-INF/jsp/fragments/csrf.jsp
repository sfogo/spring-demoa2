<!-- Cross Site Request Forgery -->
<% if (request.getAttribute("_csrf") != null) { %>
<input type="hidden" id="csrf_token" name="${_csrf.parameterName}" value="${_csrf.token}" />
<% } %>
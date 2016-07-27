<div class="aBox cw80 pad4 bm10 tm10">
    <div class="pad4">
    <% if (request.getAttribute("_message") != null) { %>
        <div><span><%= request.getAttribute("_message") %></span></div>
    <% } else { %>
         <div><span>Home</span></div>
    <% } %>

    <% if (request.getAttribute("javax.servlet.error.status_code") != null) { %>
         <div><span class="boom">HTTP Status Code</span> : <span class="umfValue"><%= request.getAttribute("javax.servlet.error.status_code") %></span></div>
    <% } %>

    <!--
    <% if (request.getAttribute("javax.servlet.error.request_uri") != null) { %>
         <div><span class="boom">Requested URI</span> : <span><%= request.getAttribute("javax.servlet.error.request_uri") %></span>
         &nbsp;<%= request.getMethod() %></div>
    <% } %>
    -->

    <% if (request.getAttribute("javax.servlet.error.message") != null) { %>
         <div><span class="li01"><%= request.getAttribute("javax.servlet.error.message") %></span></div>
    <% } else if (request.getAttribute("_error") != null) { %>
         <div><span class="li01"><%= request.getAttribute("_error") %></span></div>
    <% } %>

    <% if (request.getAttribute("_warning") != null) { %>
        <div><span class="ex01"><%= request.getAttribute("_warning") %></span></div>
    <% } %>

    <% if (request.getAttribute("javax.servlet.error.exception") != null) { %>
        <div><span class="ex01"><%= request.getAttribute("javax.servlet.error.exception") %></span></div>
    <% } %>
    </div>
</div>

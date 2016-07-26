<%@ page language="java" %>
<%@ page import="java.util.Enumeration"%>

<table class="tb01">
    <tr>
    <td class="td01 base bg5" colspan="2"><span class="ex00">Request Headers</span></td>
    </tr>
    <% {
    Enumeration headers = request.getHeaderNames();
    while (headers.hasMoreElements()) {
    String name = (String)headers.nextElement();
    String value = request.getHeader(name);
    %>
    <tr>
        <td class="td01 base"><span class="fs90"><%= name %></span></td>
        <td class="td01 base"><span class="fs90"><%= value %></span></td>
    </tr>
    <%
    }}
    %>
</table>

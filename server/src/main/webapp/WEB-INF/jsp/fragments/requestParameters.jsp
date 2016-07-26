<%@ page language="java" %>
<%@ page import="java.util.Enumeration"%>

<table class="tb01">
    <tr>
    <td class="td01 base bg5" colspan="2"><span class="ex00">Request Parameters</span></td>
    </tr>
    <% {
    Enumeration names = request.getParameterNames();
    while (names.hasMoreElements()) {
    String name = (String)names.nextElement();
    String value = request.getParameter(name);
    %>
    <tr>
        <td class="td01 base"><span class="fs90"><%= name %></span></td>
        <td class="td01 base"><span class="fs90"><%= value %></span></td>
    </tr>
    <%
    }}
    %>
</table>

<%@ page language="java" %>
<%@ page import="java.util.Enumeration"%>

<table class="tb01">
    <tr>
    <td class="td01 base bg5" colspan="2"><span class="ex00">Session Attributes</span></td>
    </tr>
    <% {
    if (request.getSession(false) != null) {
        final Enumeration names = request.getSession().getAttributeNames();
        while (names.hasMoreElements()) {
            String name = (String)names.nextElement();
            Object value = request.getSession().getAttribute(name);
    %>
    <tr>
        <td class="td01 base wrapped wCOL1"><span class="fs90"><%= name %></span></td>
        <td class="td01 base wrapped"><span class="fs90"><%= value.toString() %></span></td>
    </tr>
    <%
    }}}
    %>
</table>

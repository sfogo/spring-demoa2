<%@ page language="java" %>

<table class="tb01">
    <tr>
    <td class="td01 base bg5" colspan="2"><span class="ex00">Request</span></td>
    </tr>
    <tr>
        <td class="td01 base wrapped wCOL1"><span class="fs90">Method</span></td>
        <td class="td01 base wrapped"><span class="fs90"><%= request.getMethod() %></span></td>
    </tr>
    <tr>
        <td class="td01 base wrapped wCOL1"><span class="fs90">Path Info</span></td>
        <td class="td01 base wrapped"><span class="fs90"><%= request.getPathInfo() %></span></td>
    </tr>
    <tr>
        <td class="td01 base wrapped wCOL1"><span class="fs90">Scheme</span></td>
        <td class="td01 base wrapped"><span class="fs90"><%= request.getScheme() %></span></td>
    </tr>
    <tr>
        <td class="td01 base wrapped wCOL1"><span class="fs90">Context Path</span></td>
        <td class="td01 base wrapped"><span class="fs90"><%= request.getContextPath() %></span></td>
    </tr>
    <tr>
        <td class="td01 base wrapped wCOL1"><span class="fs90">Servlet Path</span></td>
        <td class="td01 base wrapped"><span class="fs90"><%= request.getServletPath() %></span></td>
    </tr>

    <!--
    <tr>
        <td class="td01 base wrapped wCOL1"><span class="fs90">Path Translated</span></td>
        <td class="td01 base wrapped"><span class="fs90"><%= request.getPathTranslated() %></span></td>
    </tr>
    <tr>
        <td class="td01 base wrapped wCOL1"><span class="fs90">URI</span></td>
        <td class="td01 base wrapped"><span class="fs90"><%= request.getRequestURI() %></span></td>
    </tr>
    <tr>
        <td class="td01 base wrapped wCOL1"><span class="fs90">URL</span></td>
        <td class="td01 base wrapped"><span class="fs90"><%= request.getRequestURL() %></span></td>
    </tr>
    -->

    <tr>
        <td class="td01 base wrapped wCOL1"><span class="fs90">Remote Address</span></td>
        <td class="td01 base wrapped"><span class="fs90"><%= request.getRemoteAddr() %></span></td>
    </tr>
    <tr>
        <td class="td01 base wrapped wCOL1"><span class="fs90">Remote Host</span></td>
        <td class="td01 base wrapped"><span class="fs90"><%= request.getRemoteHost() %></span></td>
    </tr>
    <tr>
        <td class="td01 base wrapped wCOL1"><span class="fs90">Remote User</span></td>
        <td class="td01 base wrapped"><span class="fs90"><%= request.getRemoteUser() %></span></td>
    </tr>
    <tr>
        <td class="td01 base wrapped wCOL1"><span class="fs90">Query</span></td>
        <td class="td01 base wrapped"><span class="fs90"><%= request.getQueryString() %></span></td>
    </tr>
</table>

<%@ page language="java" %>

<html xmlns:jsp="http://java.sun.com/JSP/Page">
<head>
    <title>Message</title>
    <link href="${pageContext.request.contextPath}/css/style.css" rel="stylesheet">
</head>

<body class="base">
<jsp:include page="fragments/header.jsp"/>
<% request.setAttribute("_message", "ERROR"); %>
<jsp:include page="fragments/messages.jsp"/>
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
<%@page isErrorPage="true" %>
<%@ include file="/WEB-INF/view/module/legacyui/template/include.jsp" %>

<openmrs:message var="pageTitle" code="missing.title" scope="page"/>
<%@ include file="/WEB-INF/view/module/legacyui/template/header.jsp" %>

<h2>Error 404</h2>

<br/><br/>

<openmrs:message code="Missing.start"/> "<b><%= request.getAttribute("javax.servlet.error.request_uri") %></b>"
<openmrs:message code="Missing.end"/>

<br/><br/>

<openmrs:extensionPoint pointId="org.openmrs.missing" type="html"/>


<%@ include file="/WEB-INF/view/module/legacyui/template/footer.jsp" %> 

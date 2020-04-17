<%@ include file="/WEB-INF/view/module/legacyui/template/include.jsp" %>

<openmrs:require privilege="View Patients" otherwise="/login.htm" redirect="/findPatient.htm"/>

<openmrs:message var="pageTitle" code="findPatient.title" scope="page"/>
<%@ include file="/WEB-INF/view/module/legacyui/template/header.jsp" %>

<h2><openmrs:message code="Patient.search"/></h2>

<br/>

<openmrs:portlet id="findPatient" url="findPatient"
                 parameters="size=full|postURL=patientDashboard.form|showIncludeVoided=false|viewType=shortEdit"/>

<openmrs:extensionPoint pointId="org.openmrs.findPatient" type="html"/>

<%@ include file="/WEB-INF/view/module/legacyui/template/footer.jsp" %>

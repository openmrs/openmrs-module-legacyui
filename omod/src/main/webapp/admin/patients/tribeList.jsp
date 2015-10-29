<%@ include file="/WEB-INF/view/module/legacyui/template/include.jsp" %>

<openmrs:require privilege="Manage Tribes" otherwise="/login.htm" redirect="/admin/patients/tribe.list" />
	
<%@ include file="/WEB-INF/view/module/legacyui/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<openmrs:message code="Tribe.module.message"/>

<%@ include file="/WEB-INF/view/module/legacyui/template/footer.jsp" %>
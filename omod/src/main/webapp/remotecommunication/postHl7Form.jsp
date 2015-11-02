<%@ include file="/WEB-INF/view/module/legacyui/template/include.jsp"%>
<%@ include file="/WEB-INF/view/module/legacyui/template/header.jsp"%>

<c:if test="${model.success}">
	OK
</c:if>
<c:if test="${!model.success}">
	<openmrs:message code="PostHl7.error" arguments="${model.error}" />
</c:if>

<%@ include file="/WEB-INF/view/module/legacyui/template/footer.jsp" %>
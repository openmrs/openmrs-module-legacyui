<%@ include file="/WEB-INF/view/module/legacyui/template/include.jsp" %>

<c:choose>
	<c:when test="${not empty model.showAnswers && not empty model.showOther}">
		<openmrs_tag:conceptField formFieldName="${model.formFieldName}" initialValue="${model.obj.conceptId}" showAnswers="${model.showAnswers}" showOther="${model.showOther}" otherValue="${model.otherValue}" />
	</c:when>
	<c:when test="${not empty model.showAnswers}">
		<openmrs_tag:conceptField formFieldName="${model.formFieldName}" initialValue="${model.obj.conceptId}" showAnswers="${model.showAnswers}" />
	</c:when>
	<c:when test="${not empty model.showOther}">
		<openmrs_tag:conceptField formFieldName="${model.formFieldName}" initialValue="${model.obj.conceptId}" showOther="${model.showOther}" otherValue="${model.otherValue}" />
	</c:when>
	<c:otherwise>
		<openmrs_tag:conceptField formFieldName="${model.formFieldName}" initialValue="${model.obj.conceptId}" />
	</c:otherwise>
</c:choose>



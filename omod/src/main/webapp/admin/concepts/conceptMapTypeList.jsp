<%@ include file="/WEB-INF/view/module/legacyui/template/include.jsp" %>

<openmrs:require privilege="Manage Concept Map Types" otherwise="/login.htm" redirect="/admin/concepts/conceptMapTypeList.list" />

<%@ include file="/WEB-INF/view/module/legacyui/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><openmrs:message code="ConceptMapType.title"/></h2>

<a href="conceptMapType.form"><openmrs:message code="ConceptMapType.add"/></a>

<br /><br />

<b class="boxHeader"><openmrs:message code="ConceptMapType.list.title"/></b>
<div class="box">
	<table cellpadding="3" cellspacing="3">
		<tr>
			<th><openmrs:message code="general.name"/></th>
			<th>&nbsp;</th>
			<th><openmrs:message code="general.description"/></th>
		</tr>
		<c:forEach var="conceptMapType" items="${conceptMapTypeList}">
			<tr <c:if test="${conceptMapType.isHidden == true }">style='color:red'</c:if>> 
				<td valign="top">
					<a href="conceptMapType.form?conceptMapTypeId=${conceptMapType.conceptMapTypeId}" 
					<c:if test="${conceptMapType.isHidden == true }">style='color:grey'</c:if>>
						<c:choose>
							<c:when test="${conceptMapType.retired}"><strike><c:out value="${conceptMapType.name}"/></strike></c:when>
							<c:otherwise><c:out value="${conceptMapType.name}"/></c:otherwise>
						</c:choose>
					</a>
				</td>
				<td>&nbsp;</td>
				<td valign="top" <c:if test="${conceptMapType.isHidden == true }">style='color:grey'</c:if>><c:out value="${conceptMapType.description}"/></td>
			</tr>
		</c:forEach>
	</table>
</div>

<%@ include file="/WEB-INF/view/module/legacyui/template/footer.jsp" %>
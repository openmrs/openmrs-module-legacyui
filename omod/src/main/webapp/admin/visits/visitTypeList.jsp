<%@ include file="/WEB-INF/view/module/legacyui/template/include.jsp" %>

<openmrs:require privilege="Manage Visit Types" otherwise="/login.htm" redirect="/admin/visits/visitType.list" />

<%@ include file="/WEB-INF/view/module/legacyui/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><openmrs:message code="VisitType.manage.title"/></h2>

<a href="visitType.form"><openmrs:message code="VisitType.add"/></a>

<br /><br />

<b class="boxHeader"><openmrs:message code="VisitType.list.title"/></b>
<form method="post" class="box">
	<table>
		<tr>
			<th> <openmrs:message code="general.name"/> </th>
			<th> <openmrs:message code="general.description"/> </th>
		</tr>
		<c:forEach var="visitType" items="${visitTypeList}">
			<tr>
				<td valign="top">
					<a href="visitType.form?visitTypeId=${visitType.visitTypeId}">
						<c:choose>
							<c:when test="${visitType.retired == true}">
								<del><c:out value="${visitType.name}"/></del>
							</c:when>
							<c:otherwise>
								<c:out value="${visitType.name}"/>
							</c:otherwise>
						</c:choose>
					</a>
				</td>
				<td valign="top"><c:out value="${visitType.description}"/></td>
			</tr>
		</c:forEach>
	</table>
</form>

<%@ include file="/WEB-INF/view/module/legacyui/template/footer.jsp" %>
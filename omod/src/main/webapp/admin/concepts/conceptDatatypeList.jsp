<%@ include file="/WEB-INF/view/module/legacyui/template/include.jsp" %>

<openmrs:require privilege="Manage Concept Datatypes" otherwise="/login.htm" redirect="/admin/concepts/conceptDatatype.list" />

<%@ include file="/WEB-INF/view/module/legacyui/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><openmrs:message code="ConceptDatatype.manage.title"/></h2>

<openmrs:extensionPoint pointId="org.openmrs.admin.concepts.conceptDatatypeList.afterTitle" type="html" />

<%--  <a href="conceptDatatype.form"><openmrs:message code="ConceptDatatype.add"/></a> --%>

<div id="conceptDatatypeListReadOnly">(<openmrs:message code="general.readonly"/>)</div>

<br />

<b class="boxHeader"><openmrs:message code="ConceptDatatype.list.title"/></b>
<form method="post" class="box">
	<table>
		<tr>
			<%-- <th> </th> --%>
			<th> <openmrs:message code="general.name"/> </th>
			<th> <openmrs:message code="general.description"/> </th>
		</tr>
		<c:forEach var="conceptDatatype" items="${conceptDatatypeList}">
			<tr> 
				<%-- <td valign="top"><input type="checkbox" name="conceptDatatypeId" value="${conceptDatatype.conceptDatatypeId}"></td> --%>
				<td valign="top"> <c:out value="${conceptDatatype.name}"/>
				</td>
				<td valign="top"><c:out value="${conceptDatatype.description}"/></td>
			</tr>
		</c:forEach>
	</table>
	<openmrs:extensionPoint pointId="org.openmrs.admin.concepts.conceptDatatypeList.inForm" type="html" />
</form>

<openmrs:extensionPoint pointId="org.openmrs.admin.concepts.conceptDatatypeList.footer" type="html" />

<%@ include file="/WEB-INF/view/module/legacyui/template/footer.jsp" %>
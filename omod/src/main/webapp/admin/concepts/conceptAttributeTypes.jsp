<%@ include file="/WEB-INF/view/module/legacyui/template/include.jsp" %>

<openmrs:require privilege="Manage Concept Attribute Types" otherwise="/login.htm" redirect="/admin/concepts/conceptAttributeTypes.list" />

<%@ include file="/WEB-INF/view/module/legacyui/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><openmrs:message code="ConceptAttributeType.manage.title"/></h2>

<a href="conceptAttributeType.form"><openmrs:message code="ConceptAttributeType.add.title"/></a>

<openmrs:extensionPoint pointId="org.openmrs.admin.concepts.conceptAttributeTypes.afterAdd" type="html" />

<br />
<br />

<b class="boxHeader"><openmrs:message code="ConceptAttributeType.list.title"/></b>
<div class="box">
    <c:choose>
        <c:when test="${ not empty attributeTypes }">
            <table>
                <tr>
                    <th> <openmrs:message code="general.name" /> </th>
                    <th> <openmrs:message code="general.description" /> </th>
                </tr>
                <c:forEach var="attrType" items="${ attributeTypes }">
                    <tr>
                        <td valign="top">
                            <a href="conceptAttributeType.form?id=${ attrType.id }">
                                <c:choose>
                                    <c:when test="${ attrType.retired }">
                                        <del><c:out value="${ attrType.name }"/></del>
                                    </c:when>
                                    <c:otherwise>
                                        <c:out value="${ attrType.name }"/>
                                    </c:otherwise>
                                </c:choose>
                            </a>
                        </td>
                        <td valign="top"><c:out value="${ attrType.description }"/></td>
                    </tr>
                </c:forEach>
            </table>
        </c:when>
        <c:otherwise>
            <openmrs:message code="general.none" />
        </c:otherwise>
    </c:choose>
</div>

<openmrs:extensionPoint pointId="org.openmrs.admin.concepts.conceptAttributeTypes.footer" type="html" />

<%@ include file="/WEB-INF/view/module/legacyui/template/footer.jsp" %>
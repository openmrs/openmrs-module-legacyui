<% session.removeAttribute(org.openmrs.web.WebConstants.OPENMRS_HEADER_USE_MINIMAL); %>
	   
<c:choose>
	<%-- useMinimalHeader should have been set in header.jsp --%>
	<c:when test="${useMinimalHeader}">
		<%@ include file="footerMinimal.jsp" %>
	</c:when>
	<c:otherwise>
		<jsp:include page="/template/footerFull.htm" />
	</c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/view/module/legacyui/template/include.jsp" %>
		<br/>
		</div>
	</div>

	<div id="footer" 
		xmlns:c="http://java.sun.com/jsp/jstl/core"
		xmlns:fn="http://java.sun.com/jsp/jstl/functions"
		xmlns:spring="http://www.springframework.org/tags">
	
		<div id="footerInner">
		
			<openmrs:extensionPoint pointId="org.openmrs.footerFullBeforeStatusBar" type="html" />
		
			<span id="localeOptions">
				<%  //removes last instance of lang= from querystring and encodes the url to avoid xml problems
					String qs = org.apache.commons.lang.StringEscapeUtils.escapeXml(request.getQueryString());
					if (qs == null)
						qs = "";
					int i = qs.lastIndexOf("lang=");
					if (i == -1)
						i = qs.length();
						
					String s = "&amp;";
					if (i == 0 || qs.indexOf("&") > 0)
						s = "";
						
					pageContext.setAttribute("qs", qs.substring(0, i) + s);
					
					org.openmrs.api.context.Context.addProxyPrivilege(org.openmrs.util.PrivilegeConstants.GET_GLOBAL_PROPERTIES);
					pageContext.setAttribute("locales", org.openmrs.api.context.Context.getAdministrationService().getPresentationLocales());
					org.openmrs.api.context.Context.removeProxyPrivilege(org.openmrs.util.PrivilegeConstants.GET_GLOBAL_PROPERTIES);
					
					pageContext.setAttribute("openmrsPlatformVersion", org.openmrs.util.OpenmrsConstants.OPENMRS_VERSION);
					pageContext.setAttribute("locale", org.openmrs.api.context.Context.getLocale());
				%>
		
				<c:forEach items="${locales}" var="loc" varStatus="status">
					<%
						java.util.Locale locTmp = (java.util.Locale) pageContext.getAttribute("loc");
						pageContext.setAttribute("locDisplayName", locTmp.getDisplayName(locTmp));
					%>
					<c:if test="${status.index != 0}">| </c:if>
					<c:if test="${fn:toLowerCase(locale) == fn:toLowerCase(loc)}">${locDisplayName}</c:if>
					<c:if test="${fn:toLowerCase(locale) != fn:toLowerCase(loc)}"><a href="?${qs}lang=${loc}">${locDisplayName}</a></c:if> 
				</c:forEach>
			</span>	
	
			<span id="buildDate"><openmrs:message code="footer.lastBuild"/>: <%= org.openmrs.web.WebConstants.BUILD_TIMESTAMP %></span>
			
			<span id="codeVersion"><openmrs:message code="footer.version"/>: ${openmrsPlatformVersion}</span>

			<c:if test="${not empty extraData}">
				<span id="extraData">${extraData}</span>
			</c:if>

			<span id="poweredBy"><a href="http://openmrs.org"><openmrs:message code="footer.poweredBy"/> <img border="0" align="top" src="<%= request.getContextPath() %>/moduleResources/legacyui/images/openmrs_logo_tiny.png"/></a></span>
		</div>
	</div>

</body>
</html>

<ul id="menu">
    <li class="first">
        <a href="${pageContext.request.contextPath}/admin/index.htm"><openmrs:message code="admin.title.short"/></a>
    </li>
    <openmrs:hasPrivilege privilege="Add Patients,Edit Patients,Delete Patients,View Patients">
        <li <c:if test='<%= request.getRequestURI().contains("patients/index") %>'>class="active"</c:if>>
            <a href="${pageContext.request.contextPath}/admin/patients/index.htm">
                <openmrs:message code="Patient.manage"/>
            </a>
        </li>
    </openmrs:hasPrivilege>
    <openmrs:globalProperty key="use_patient_attribute.tribe" defaultValue="false" var="showTribe"/>
    <c:if test="${showTribe == 'true'}">
        <openmrs:hasPrivilege privilege="Manage Tribes">
            <li <c:if test='<%= request.getRequestURI().contains("tribe") %>'>class="active"</c:if>>
                <a href="${pageContext.request.contextPath}/admin/patients/tribe.list">
                    <openmrs:message code="Tribe.manage"/>
                </a>
            </li>
        </openmrs:hasPrivilege>
    </c:if>
    <openmrs:hasPrivilege privilege="Edit Patients">
        <li <c:if test='<%= request.getRequestURI().contains("patients/findDuplicatePatients") %>'>class="active"</c:if>>
            <a href="${pageContext.request.contextPath}/admin/patients/findDuplicatePatients.htm">
                <openmrs:message code="Patient.merge.find"/>
            </a>
        </li>
    </openmrs:hasPrivilege>
    <openmrs:hasPrivilege privilege="Manage Identifier Types">
        <li <c:if test='<%= request.getRequestURI().contains("patientIdentifierType") %>'>class="active"</c:if>>
            <a href="${pageContext.request.contextPath}/admin/patients/patientIdentifierType.list">
                <openmrs:message code="PatientIdentifierType.manage"/>
            </a>
        </li>
    </openmrs:hasPrivilege>
    <openmrs:extensionPoint pointId="org.openmrs.admin.patients.localHeader" type="html">
        <c:forEach items="${extension.links}" var="link">
            <li
                    <c:if test="${fn:endsWith(pageContext.request.requestURI, link.key)}">class="active"</c:if> >
                <a href="<openmrs_tag:url value="${link.key}"/>"><openmrs:message code="${link.value}"/></a>
            </li>
        </c:forEach>
    </openmrs:extensionPoint>
</ul>

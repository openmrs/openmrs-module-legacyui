<%@ include file="/WEB-INF/view/module/legacyui/template/include.jsp" %>

<openmrs:require privilege="View Concept Proposals" otherwise="/login.htm" redirect="/admin/concepts/conceptProposal.list"/>

<%@ include file="/WEB-INF/view/module/legacyui/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<script type="text/javascript">
    function selectProposal(pid) {
        document.location = "conceptProposal.form?conceptProposalId=" + pid;
    }

    function mouseOver(row) {
        if (row.className.indexOf("searchHighlight") == -1)
            row.className = "searchHighlight " + row.className;
    }

    function mouseOut(row) {
        var c = row.className;
        row.className = c.substring(c.indexOf(" ") + 1, c.length);
    }

    function updateList() {
        var url = "conceptProposal.list?";
        url += "includeCompleted=" + document.getElementById('includeCompleted').checked;

        if (document.getElementById('orderAsc').checked)
            url += "&sortOrder=asc";
        else
            url += "&sortOrder=desc";

        if (document.getElementById('sortText').checked)
            url += "&sortOn=text";
        else if (document.getElementById('sortOccurences').checked)
            url += "&sortOn=occurences";

        document.location = url;
    }
</script>

<style>
    th {
        text-align: left;
    }
</style>

<h2><openmrs:message code="ConceptProposal.manage.title"/></h2>

<a href="proposeConcept.form"><openmrs:message code="ConceptProposal.proposeNewConcept"/></a>

<br/><br/>

<openmrs:hasPrivilege privilege="Manage Concepts">
    <table>
        <tr>
            <th><openmrs:message code="ConceptProposal.includeCompleted"/></th>
            <td><input type="checkbox"
                       <c:if test="${param.includeCompleted}">checked</c:if> id="includeCompleted" onclick="updateList()"/>
            </td>
        </tr>
        <tr>
            <th><openmrs:message code="ConceptProposal.sortOn"/></th>
            <td>
                <input type="radio" name="sortOn" id="sortText" value="text"
                       <c:if test="${param.sortOn == 'text'}">checked</c:if> onclick="updateList()"/><label
                    for="sortText"><openmrs:message code="ConceptProposal.originalText"/></label>
                <input type="radio" name="sortOn" id="sortOccurences" value="text"
                       <c:if test="${param.sortOn == null || param.sortOn == 'occurences'}">checked</c:if>
                       onclick="updateList()"/><label for="sortOccurences"><openmrs:message
                    code="ConceptProposal.occurences"/></label>
            </td>
        </tr>
        <tr>
            <th><openmrs:message code="ConceptProposal.sortOrder"/></th>
            <td>
                <input type="radio" name="sortOrder" id="orderAsc" value="asc"
                       <c:if test="${param.sortOrder == 'asc'}">checked</c:if> onclick="updateList()"/><label for="orderAsc"><openmrs:message
                    code="ConceptProposal.sortOrder.asc"/></label>
                <input type="radio" name="sortOrder" id="orderDesc" value="desc"
                       <c:if test="${param.sortOrder == null || param.sortOrder == 'desc'}">checked</c:if>
                       onclick="updateList()"/><label for="orderDesc"><openmrs:message
                    code="ConceptProposal.sortOrder.desc"/></label>
            </td>
        </tr>
    </table>

    <br/>

    <b class="boxHeader"><openmrs:message code="ConceptProposal.list.title"/></b>

    <form method="post" class="box">
        <table width="100%" cellspacing="0" cellpadding="2">
            <tr>
                <th><openmrs:message code="ConceptProposal.encounter"/></th>
                <th><openmrs:message code="ConceptProposal.originalText"/></th>
                <th><openmrs:message code="general.creator"/></th>
                <th><openmrs:message code="general.dateCreated"/></th>
                <th><openmrs:message code="ConceptProposal.occurences"/></th>
            </tr>
            <c:forEach var="map" items="${conceptProposalMap}" varStatus="rowStatus">
                <c:forEach items="${map.key}" var="conceptProposal" varStatus="varStatus">
                    <c:if test="${varStatus.first}">
                        <tr class='${rowStatus.index % 2 == 0 ? "evenRow" : "oddRow"} ${conceptProposal.state != unmapped ? "voided" : ""}'
                            onclick="selectProposal('${conceptProposal.conceptProposalId}')"
                            onmouseover="mouseOver(this)" onmouseout="mouseOut(this)">
                            <td valign="top">${conceptProposal.encounter.encounterId}</td>
                            <td valign="top">${conceptProposal.originalText}</td>
                            <td valign="top"><openmrs:format user="${conceptProposal.creator}"/></td>
                            <td valign="top">${conceptProposal.dateCreated}</td>
                            <td valign="top">${map.value}</td>
                        </tr>
                    </c:if>
                </c:forEach>
            </c:forEach>
        </table>
    </form>
</openmrs:hasPrivilege>

<%@ include file="/WEB-INF/view/module/legacyui/template/footer.jsp" %>

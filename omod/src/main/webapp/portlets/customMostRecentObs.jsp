<%@ include file="/WEB-INF/view/module/legacyui/template/include.jsp" %>
<%--
	allowNew=true/false (defaults false)
--%>

<openmrs:htmlInclude file="/scripts/calendar/calendar.js"/>

<c:set var="allowNew" value="${model.allowNew == 'true'}"/>

<%-- <openmrs:globalProperty var="conceptsToDisplay" key="${model.globalPropertyKey}" /> --%>
<c:if test="${not empty model.conceptIds}">

    <openmrs:htmlInclude file="/dwr/interface/DWRObsService.js"/>
    <openmrs:htmlInclude file="/dwr/engine.js"/>
    <openmrs:htmlInclude file="/dwr/util.js"/>

    <table>
        <c:forEach var="rawConceptId" items="${model.conceptIds}">
            <c:set var="conceptId" value="${fn:trim(rawConceptId)}"/>
            <tr>
                <td><openmrs_tag:concept conceptId="${conceptId}"/>:</td>
                <td>
                    <b>
                        <openmrs_tag:mostRecentObs concept="${conceptId}" observations="${model.patientObs}"
                                                   locale="${model.locale}" labelIfNone="general.none" showDate="true"
                                                   showEditLink="true"/>
                    </b>
                </td>
                <c:if test="${allowNew}">
                    <td>
                        <c:set var="thisConcept" value="${model.conceptMapByStringIds[conceptId]}"/>
                        <a href="javascript:showHideDiv('newCustomObs_${conceptId}')">
                            <openmrs:message code="general.new"/>
                        </a>
                    </td>
                    <td class="dashedAndHighlighted" id="newCustomObs_${conceptId}" style="display:none">

                        <openmrs:format conceptId="${conceptId}"/>
                        <c:choose>
                            <c:when test="${thisConcept.datatype.hl7Abbreviation == 'DT'}">
                                <input type="text" size="10" value="" onfocus="showCalendar(this)"
                                       id="value_${conceptId}_id"/>
                            </c:when>
                            <c:when test="${thisConcept.datatype.hl7Abbreviation == 'CWE'}">
                                <openmrs:fieldGen type="org.openmrs.Concept" formFieldName="value_${conceptId}" val=""
                                                  parameters="noBind=true|showAnswers=${conceptId}"/>
                            </c:when>
                            <c:when test="${thisConcept.datatype.hl7Abbreviation == 'BIT'}">
                                <script>
                                    $j(function () {
                                        var booleanConcepts = ["Yes", "No", "False", "True", "0", "1"];
                                        $j("#value_${conceptId}_id").autocomplete({source: booleanConcepts});
                                    });
                                </script>
                                <input type="text" id="value_${conceptId}_id"/>
                            </c:when>
                            <c:otherwise>
                                <input type="text" id="value_${conceptId}_id"/>
                            </c:otherwise>
                        </c:choose>

                        <openmrs:message code="general.onDate"/>
                        <openmrs:fieldGen type="java.util.Date" formFieldName="date_${conceptId}" val=""
                                          parameters="noBind=true"/>
                        <input type="button" value="<openmrs:message code="general.save"/>"
                               onClick="handleAddCustomObs(${conceptId})"/>
                        <input type="button" value="<openmrs:message code="general.cancel"/>"
                               onClick="showHideDiv('newCustomObs_${conceptId}')"/>
                    </td>
                </c:if>
            </tr>
        </c:forEach>
    </table>
</c:if>

<script type="text/javascript">
    function handleAddCustomObs(conceptId) {
        var encounterId = null;
        var valueText = dwr.util.getValue(document.getElementById('value_' + conceptId + '_id'));
        var obsDate = dwr.util.getValue(document.getElementById('date_' + conceptId));
        var patientId = <c:out value="${model.patient.patientId}" />;
        DWRObsService.createObs(patientId, encounterId, conceptId, valueText, obsDate, refreshPage);
    }
</script>


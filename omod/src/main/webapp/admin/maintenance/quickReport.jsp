<%@ include file="/WEB-INF/view/module/legacyui/template/include.jsp" %>

<openmrs:require privilege="View Patients" otherwise="/login.htm" redirect="/admin/maintenance/quickReport.htm"/>

<%@ include file="/WEB-INF/view/module/legacyui/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<openmrs:htmlInclude file="/scripts/calendar/calendar.js"/>

<br/>
<h2><openmrs:message code="QuickReport.manage"/></h2>
<br/>

<script type="text/javascript">
    function show(ids) {
        var rows = document.getElementById("reportOptions").rows;
        for (var i = 0; i < rows.length; i++) {
            var id = rows[i].id;
            var showRow = false;
            for (var x = 0; x < ids.length; x++) {
                if (ids[x] == id) {
                    showRow = true;
                    break;
                }
            }
            if (showRow == true)
                rows[i].style.display = "";
            else
                rows[i].style.display = "none";
        }
    }

    function clearAutoComplete() {
        if (document.getElementsByTagName) {
            var inputs = document.getElementsByTagName("input");
            for (var i = 0; i < inputs.length; i++) {
                if (inputs[i].onclick &&
                    inputs[i].onclick.toString().indexOf("showCalendar") != -1) {
                    inputs[i].setAttribute("autocomplete", "on");
                }
            }
        }
    }

</script>

<form method="get" action="${pageContext.request.contextPath}/moduleServlet/legacyui/quickReportServlet">
    <table border="0" cellspacing="2" cellpadding="2" id="reportOptions">
        <tr id="reportType">
            <td><openmrs:message code="QuickReport.type"/></td>
            <td>
                <select name="reportType">
                    <option value="RETURN VISIT DATE THIS WEEK"
                            onclick="show(['reportType', 'startDate', 'endDate', 'location'])"><openmrs:message
                            code="QuickReport.type.returnVisit"/></option>
                    <option value="ATTENDED CLINIC THIS WEEK"
                            onclick="show(['reportType', 'startDate', 'endDate', 'location'])"><openmrs:message
                            code="QuickReport.type.attendedClinic"/></option>
                    <option value="VOIDED OBS" onclick="show(['reportType', 'startDate', 'endDate'])"><openmrs:message
                            code="QuickReport.type.voidedObs"/></option>
                </select>
            </td>
        </tr>
        <tr id="startDate">
            <td><openmrs:message code="QuickReport.startDate"/></td>
            <td><input type="text" name="startDate" onFocus="showCalendar(this)"/></td>
        </tr>
        <tr id="endDate">
            <td><openmrs:message code="QuickReport.endDate"/></td>
            <td><input type="text" name="endDate" onFocus="showCalendar(this)"/></td>
        </tr>
        <tr id="location">
            <td><openmrs:message code="QuickReport.location"/></td>
            <td>
                <select name="location">
                    <option value=""><openmrs:message code="QuickReport.location.all"/></option>
                    <openmrs:forEachRecord name="location">
                        <option value="${record.locationId}"
                                <c:if test="${status.value == record.locationId}">selected</c:if>><c:out
                                value="${record.name}"/></option>
                    </openmrs:forEachRecord>
                </select>
            </td>
        </tr>


    </table>
    <br/>
    <input type="submit" value='<openmrs:message code="QuickReport.view" />' onClick="clearAutoComplete()"/>
</form>

<br/>

<%@ include file="/WEB-INF/view/module/legacyui/template/footer.jsp" %>

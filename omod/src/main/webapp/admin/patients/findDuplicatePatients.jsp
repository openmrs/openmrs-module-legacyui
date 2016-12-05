<%@ include file="/WEB-INF/view/module/legacyui/template/include.jsp" %>

<openmrs:require privilege="Edit Patients" otherwise="/login.htm" redirect="/admin/patients/findDuplicatePatients.htm"/>

<%@ include file="/WEB-INF/view/module/legacyui/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<openmrs:htmlInclude file="/scripts/dojo/dojo.js" />

<script type="text/javascript">
	dojo.require("dojo.widget.openmrs.PatientSearch");

	var searchWidget;
	var searchOn;
	
	var getCheckbox = function(patient) {
		if (typeof patient == "string") return "";
		var td = document.createElement("td");
		var input = document.createElement("input");
		input.type = "checkbox";
		input.name = "patientId";
		input.value = patient.patientId;
		td.appendChild(input);
		return td;
	}
	
	var getPatientId = function(patient) {
		if (typeof patient == "string") return "";
		return patient.patientId;
	}
	
	function selectAttribute(input) {
		if (input.checked == true) {
			// add the checked box
			var found = false;
			for (var i = 0; i < searchOn.length; i++) {
				if (searchOn[i] == input.value)
					found = true;
			}
			if (!found)
				searchOn.push(input.value);
		}
		else {
			// remove the checked box
			for (var i = 0; i < searchOn.length; i++) {
				if (searchOn[i] == input.value)
					searchOn[i] = null;
			}
		}
		return true;
	}

	function createIdentifiresList(){
		return document.getElementById("identifiers").value.replace(/\s*\,\s*/g,',').split(',');
	}

	function showSearch(e) {
		searchWidget.findObjects(e);
	}

	function searchByIndentifier(e){
		searchWidget.findObjectsByIdenfifiers(e);
	}
	
	dojo.addOnLoad( function() {
		
		searchWidget = dojo.widget.manager.getWidgetById("pSearch");
		
		searchOn = new Array();
		jQuery('#patientsFound').css('display', "none");
		
		var inputs = document.getElementsByTagName("input");
		for (var i=0; i<inputs.length; i++) {
			var input = inputs[i];
			if (input.type == "checkbox") {
				selectAttribute(input);
			}
		}
		
		var row = searchWidget.headerRow;
		var th = document.createElement("th");
		th.innerHTML = "Patient Id";
		row.insertBefore(th, row.firstChild.nextSibling);
		
		searchWidget.showAddPatientLink = false;
		
		searchWidget.getCellFunctions = function() {
			//alert("super: " + dojo.widget.openmrs.PatientSearch.prototype);
			var arr = dojo.widget.openmrs.PatientSearch.prototype.getCellFunctions();
			
			arr.splice(1, 0, getCheckbox);
			arr.splice(2, 0, getPatientId);
			
			return arr;
		};
		
		dojo.event.topic.subscribe("pSearch/objectsFound",
			function (msg){
				dojo.style.hide("patientsSelect");
				jQuery("#patientListSize").html(msg.objs.length);

				if (msg.objs.length == 1) {
					switch(msg.objs[0].constructor){
						case String: jQuery("#patientListSize").html(0); break;
						case Object: jQuery("#patientListSize").html("Only 1"); break;
					}
				} else if (msg.objs.length > 1) {
					dojo.style.show("patientsSelect");
				}
				jQuery('#patientsFound').css('display', "");
			}
		);
		
		searchWidget.findObjects = function(phrase) {
			if (searchOn.length > 1)
				DWRPatientService.findDuplicatePatients(searchOn, searchWidget.simpleClosure(searchWidget, "doObjectsFound"));
		}

		searchWidget.findObjectsByIdenfifiers = function(phrase) {
			if(createIdentifiresList().length > 1){
				DWRPatientService.findPatientsByIdentifier(createIdentifiresList(), searchWidget.simpleClosure(searchWidget, "doObjectsFound"));
			}
		}
		
	});
	
</script>

<style>
	.searchIndex, .searchIndexHighlight { display: none; }
	#searchNode, #searchInfoBar  { display: none; }
	.searchByMultiplePatientIdentifiers{ float: left; width: 400px;}
	.seperator{ float: left; position: relative;}
	.seperator::before{
		width: 1px;
		height: 100px;
		content: "";
		position: absolute;
		left: 15px;
		z-index: 99;
		background: #000;
	}
	.seperator h1{
		width: 50px;
		height: 50px;
		margin-top: 36px;
		background: #fff;
		position: absolute;
		top: -10px;
		z-index: 99;
	}
	.searchByOnlyIdentifiers{
		float: left;
		margin-left: 150px;
	}
	#patientsFound{
		clear: both;
		padding: 15px 0 0;
	}
	#identifiers{
		width: 300px;
	}
</style>

<h2><openmrs:message code="Patient.merge.title"/></h2>

<div class="searchByMultiplePatientIdentifiers">
	<openmrs:message code="Patient.merge.search_on"/><span class="required">*</span>: <br/>
	<input type="checkbox" name="attr" id="identifier" value="identifier" onclick="selectAttribute(this)" onactivate="selectAttribute(this)"/><label for="identifier"><openmrs:message code="Patient.identifier"/></label> <br/>
	<input type="checkbox" name="attr" id="gender" value="gender" onclick="selectAttribute(this)" onactivate="selectAttribute(this)"/><label for="gender"><openmrs:message code="Person.gender"/></label> <br/>
	<input type="checkbox" name="attr" id="birthdate" value="birthdate" onclick="selectAttribute(this)" onactivate="selectAttribute(this)"/><label for="birthdate"><openmrs:message code="Person.birthdate"/></label> <br/>
	<input type="checkbox" name="attr" id="givenName" value="givenName" onclick="selectAttribute(this)" onactivate="selectAttribute(this)"/><label for="givenName"><openmrs:message code="PersonName.givenName"/></label> <br/>
	<input type="checkbox" name="attr" id="middleName" value="middleName" onclick="selectAttribute(this)" onactivate="selectAttribute(this)"/><label for="middleName"><openmrs:message code="PersonName.middleName"/></label> <br/>
	<input type="checkbox" name="attr" id="familyName" value="familyName" onclick="selectAttribute(this)" onactivate="selectAttribute(this)"/><label for="familyName"><openmrs:message code="PersonName.familyName"/></label> <br/>
	<br/>
	<input type="checkbox" name="attr" id="includeVoided" value="includeVoided" onclick="selectAttribute(this)" onactivate="selectAttribute(this)"/><label for="includeVoided"><openmrs:message code="Patient.merge.includeVoided"/></label> <br/>

	<br />
	<input type="button" value='<openmrs:message code="general.search"/>' onclick="showSearch(event)" /><br />

	<i>(<openmrs:message code="Patient.merge.minimum"/>)</i>
</div>
<div class="seperator">
	<h1>or</h1>
</div>
<div class="searchByOnlyIdentifiers">
	<h3><openmrs:message code="Patient.identifier"/></h3>
	<input type="text" name="identifiers" id="identifiers"> <br />
	<span>
		(Enter Multiple Patient Identifiers with comma separated )
	</span>
	<br /><br />
	<input type="button" value='<openmrs:message code="general.search"/>' onclick="searchByIndentifier(event)" /><br />
</div>
<br /><br />

<div id="mergePatientPopup">
	<div id="mergePatientPopupLoading"><openmrs:message code="general.loading"/></div>
	<iframe id="mergePatientPopupIframe" name="mergePatientPopupIframe" width="100%" height="100%" marginWidth="0" marginHeight="0" frameBorder="0" scrolling="auto"></iframe>
</div>
<script type="text/javascript">
	$j(document).ready(function() {
		$j('#mergePatientPopup').dialog({
				title: '<openmrs:message code="Patient.merge.title"/>',
				autoOpen: false,
				draggable: false,
				resizable: false,
				width: '95%',
				modal: true,
				open: function(a, b) { $j('#mergePatientPopupLoading').show(); }
		});
		$j("#mergePatientPopupIframe").load(function() { $j('#mergePatientPopupLoading').hide(); });
	});

	function showMergePatientPopup() {
		$j('#mergePatientPopup')
			.dialog('option', 'height', $j(window).height() - 50) 
			.dialog('open');
		return true;
	}
</script>

<form action="mergePatients.form" id="patientsFound" target="mergePatientPopupIframe">
    <span id="patientListSize"></span> <openmrs:message code="Patient.returned"/>.
    <span id="patientsSelect"><openmrs:message code="Patient.merge.select"/>
	<div dojoType="PatientSearch" widgetId="pSearch" inputId="searchNode" tableHeight="1000"></div>
	<input type="hidden" name="modalMode" value="true"/>
	<input type="submit" value='<openmrs:message code="general.continue"/>' onclick="showMergePatientPopup();"/>
    </span>
</form>

<%@ include file="/WEB-INF/view/module/legacyui/template/footer.jsp" %>
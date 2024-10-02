/*
	Copyright (c) 2006, The OpenMRS Cooperative
	All Rights Reserved.
*/

dojo.provide("dojo.widget.openmrs.PersonSearch");
dojo.require("dojo.widget.openmrs.OpenmrsSearch");

importJavascriptFile(openmrsContextPath + "/dwr/interface/DWRPersonService.js");

dojo.widget.tags.addParseTreeHandler("dojo:PersonSearch");

dojo.widget.defineWidget(
	"dojo.widget.openmrs.PersonSearch",
	dojo.widget.openmrs.OpenmrsSearch,
	{
		personId: "",
		roles: "",
		canAddNewPerson: "",
		initialized: "",
		
		initializer: function(){
			dojo.debug("initializing personsearch");
			dojo.event.connect("after", this, "doObjectsFound", this, "postDoObjectsFound");
		},

		postCreate: function() {
			if (this.personId != "") {
				this.selectPerson(this.personId);
			}
			if (this.canAddNewPerson == 'true') {
		        this.initCreateNewPerson();
				this.domNode.style.width="620px";
			}
		},
		
		selectPerson: function(personId) {
			DWRPersonService.getPerson(personId, this.simpleClosure(this, "select"));
		},

		
		selectPersonListItem: function(personListItem) {
			if (personListItem.personId == null) 
				alert(personListItem);
			else 
				this.select(personListItem);
		    
		    this.hideNewPersonForm();
		    return false;
		},

		doFindObjects: function(text) {
			var tmpIncludedVoided = (this.showIncludeVoided && this.includeVoided.checked);
			var newPersonId = DWRPersonService.findPeopleByRoles(text, tmpIncludedVoided, this.roles, this.simpleClosure(this, "doObjectsFound"));
            if (this.canAddNewPerson == 'true') {
                if (this.initialized == 'true') {
                    document.getElementById("newPersonButton").disabled = false;
                }
            }
			return false;
		},
		
		postDoObjectsFound: function( ) {
			if (this.canAddNewPerson == 'true') { 
			    // Hide tmpNode place holder after search table has appeared.
				document.getElementById('tmpNode').style.display = 'none';
			}
		},
		
		getGiven : function(p) { return p.givenName == null || p.givenName === "null" ? this.noCell() : p.givenName;  },
		getMiddle: function(p) { return p.middleName == null || p.middleName === "null" ? this.noCell() : p.middleName; },
		getFamily: function(p) { return p.familyName == null || p.familyName === "null" ? this.noCell() : p.familyName; },
		getGender: function(p) {
				if (p.gender == null) { return this.noCell(); }
				
				var td = document.createElement("td");
				td.className = "personGender";
				var src = openmrsContextPath + "/images/";
				if (p.gender.toUpperCase() == "F")
					src += "female.gif";
				else if (p.gender.toUpperCase() == "M")
					src += "male.gif";
				else
					return "";
				var img = document.createElement("img");
				td.innerHTML = "<img src='" + src + "'>";
				return td;
		},
		
		getBirthdayEstimated: function(p) {
				if (typeof p == 'string') return this.noCell();
				if (p.birthdateEstimated)
					return "&asymp;";
				else
					return "";
		},
		
		getBirthday: function(p) { 
				if (typeof p == 'string') return this.noCell();
				str = this.getDateString(p.birthdate);
				return str;
		},
		
		getAge: function(p) { 
				if (typeof p == 'string') return this.noCell();
				if (p.age == null) return "";
				var td = document.createElement("td");
				td.className = 'personAge';
				var age = p.age;
				if (age < 1)
					age = "<1";
				td.innerHTML = age;
				return td;
		},
		
		getAttribute: function(p, arg2, attrName) {
			if (typeof p == 'string') return this.noCell();
			return p.attributes[attrName];
		},
		
		getIdentifier: function(p) {
			var td = document.createElement("td");
			
			if (p.patientId)
				td.onmouseover = function() { window.status = "Patient Id: " + p.patientId; };
			td.onmouseout = function() { window.status = "" };
			
			/* Because this is the first column, return 'p' */
			if (typeof p == 'string') {
				td.colSpan = this.getHeaderCellContent().length;
				td.innerHTML = p;
			}
			else if (p.identifier) {
				td.className = "patientIdentifier";
				var obj = document.createTextNode(p.identifier + " ");
				td.appendChild(obj);
			}
			
			return td;
		},

		getCellFunctions: function() {
			var arr = new Array();
			arr.push(this.simpleClosure(this, "getNumber"));
			arr.push(this.simpleClosure(this, "getIdentifier"));
			arr.push(this.simpleClosure(this, "getGiven"));
			arr.push(this.simpleClosure(this, "getMiddle"));
			arr.push(this.simpleClosure(this, "getFamily"));
			arr.push(this.simpleClosure(this, "getAge"));
			arr.push(this.simpleClosure(this, "getGender"));
			arr.push(this.simpleClosure(this, "getBirthdayEstimated"));
			arr.push(this.simpleClosure(this, "getBirthday"));
			/* personListingAttrs var from openmrsmessages.js */
			for (var i = 0; i < omsgs.personListingAttrs.length; i++) {
				arr.push(this.simpleClosure(this, "getAttribute", omsgs.personListingAttrs[i]));
			}
			return arr;
		},
		
		showHeaderRow: true,
		getHeaderCellContent: function() {
			var arr = new Array();
			arr.push('');
			arr.push(omsgs.identifier);
			arr.push(omsgs.givenName);
			arr.push(omsgs.middleName);
			arr.push(omsgs.familyName);
			arr.push(omsgs.age);
			arr.push(omsgs.gender);
			arr.push('');
			arr.push(omsgs.birthdate);
			/* personListingHeaders var from openmrsmessages.js */
			for (var i = 0; i < omsgs.personListingHeaders.length; i++) {
				arr.push(omsgs.personListingHeaders[i]);
			}
			return arr;
		},
		
		allowAutoJump: function() {
			return false;
		},

        // Override superclass.clearSearch( ) to disable newPersonButton.
        clearSearch: function() {
            dojo.widget.openmrs.PersonSearch.superclass.clearSearch.call(this);
            if (this.canAddNewPerson == 'true') {
                if (this.initialized == 'true') {
                    this.hideNewPersonForm();
                    document.getElementById("newPersonButton").disabled = true;
                }
            }
        },

        initCreateNewPerson: function( ) {
            // Create a tmpNode placeholder until the search table shows up.
            var tmpNode = document.createElement("div");
            tmpNode.id = 'tmpNode';
            tmpNode.innerHTML = "<br/><br/><br/>";
            this.domNode.appendChild(tmpNode, this.domNode.lastChild);
            // Create the New Person Button and Form.
            this.domNode.appendChild(this.createNewPersonButton(), this.domNode.lastChild);
            this.domNode.appendChild(this.createNewPersonForm(), this.domNode.lastChild);
            // Attach events.
            dojo.event.connect(document.getElementById("newPersonButton"), "onclick", this, "showNewPersonForm");                     
            dojo.event.connect(document.getElementById('cancel'), "onclick", this, "hideNewPersonForm");
            dojo.event.connect(document.getElementById('submitPerson'), "onclick", this, "submitNewPerson");
            dojo.event.connect(document.getElementById('newPersonForm'), "onkeydown", this, "validateSubmitKey");
            // Set initialized flag.
            this.initialized = 'true';
        },
        
        submitNewPerson: function(event) {
            var errors = document.getElementsByName("error");
            for (var i=0; i<errors.length; i++)
                errors[i].style.display = "none";
            var valid = true;
            var ipName = document.getElementsByName('ipName');
            if ("" == ipName[0].value) { // || "" == ipName[2].value) {
                valid = false;
                document.getElementById("errorGiven").style.display = "";
            }
            if ("" == ipName[2].value) {
                valid = false;
                document.getElementById("errorFamily").style.display = "";
            }
            var ipDate = document.getElementsByName('ipDate');
            if ("" == ipDate[0].value && "" == ipDate[1].value) {
                valid = false;
                document.getElementById("errorAge").style.display = "";
            }
            var year = new Date().getFullYear();
            var birthyear = (ipDate[0] == null || "" == ipDate[0].value) ? "" : ipDate[0].value.substr(6, 4);
            if (("" == ipDate[0].value || birthyear.length < 4 || birthyear < (year-120) || this.isFutureDate(ipDate[0].value)) && "" == ipDate[1].value) {
                valid = false;
                document.getElementById("errorAge").style.display = "";
            }
            var ipGender = document.getElementsByName('ipGender');
            if (!ipGender[0].checked && !ipGender[1].checked) {
                valid = false;
                document.getElementById("errorGender").style.display = "";
            }
            if (valid == false) return false;
            var gender = (ipGender[0].checked ? "M" : "F");
            var tmpIncludedVoided = (this.showIncludeVoided && this.includeVoided.checked);
            var format = document.getElementById('datepattern');
            DWRPersonService.createPerson(ipName[0].value, ipName[1].value, ipName[2].value, ipDate[0].value, format.value, ipDate[1].value, gender, this.simpleClosure(this, "selectPersonListItem"));
            this.hideNewPersonForm( );
            return false;
        },
        
        validateSubmitKey: function(event) {
            if (event.keyCode != dojo.event.browser.keys.KEY_ENTER) {
                if (event.keyCode == dojo.event.browser.keys.KEY_ESCAPE) {
                    this.hideNewPersonForm();
                }
                return false;
            }
            if (document.getElementById("newPersonForm").style.display == "none") {
                return false;
            }
            return this.submitNewPerson(event);
        },
        
        showNewPersonForm: function( ) {
                document.getElementById("newPersonForm").style.display = "";
                document.getElementById("newPersonButton").style.display = "none";
                var givenNameInput = document.getElementById("givenName");
            	//if (!givenNameInput.disabled && givenNameInput.display) // in case its not loaded yet (ie6)
            	//	givenNameInput.focus();
                var errors = document.getElementsByName("error");
                for (var i=0; i<errors.length; i++)
                    errors[i].style.display = "none";
        },

        hideNewPersonForm: function( ) {
                document.getElementById("newPersonForm").style.display = "none";
                document.getElementById("newPersonButton").style.display = "";
                var newPersonButton = document.getElementById("newPersonButton");
                //if (!newPersonButton.disabled && newPersonButton.display) // in case its not loaded yet (ie6) 
                //	newPersonButton.focus();
        },

        isFutureDate: function(birthdate) {
                if (birthdate == "")
                    return false;
                var currentTime = new Date().getTime();
                /* datePattern var from openmrsmessages.js */
                var datePatternStart = omsgs.datePattern.substr(0,1).toLowerCase();
                var enteredTime = new Date();
                var year, month, day;
                if (datePatternStart == 'm') { /* M-D-Y */
                    year = birthdate.substr(6, 4);
                    month = birthdate.substr(0, 2);
                    day = birthdate.substr(3, 2);
                }
                else if (datePatternStart == 'y') { /* Y-M-D */
                    year = birthdate.substr(0, 4);
                    month = birthdate.substr(3, 2);
                    day = birthdate.substr(8, 2);
                }
                else { /* (datePatternStart == 'd') D-M-Y */
                    year = birthdate.substr(6, 4);
                    month = birthdate.substr(3, 2);
                    day = birthdate.substr(0, 2);
                }
                /* alert("year: " + year + " month: " + month + " day " + day); */
                enteredTime.setYear(year);
                enteredTime.setMonth(month - 1);
                enteredTime.setDate(day);
                
                return enteredTime.getTime() > currentTime;
        },

        createNewPersonButton: function( ) {
            var buttonForm = document.createElement("form");
            var message = "<b id='newPersonButtonMsg'>" + omsgs.addNewPersonMsg + "<br/></b>";
            var button = "<input type='button' name='showhide' id='newPersonButton' value='" + omsgs.addNewPerson + "'/>";
            buttonForm.id = 'newPersonButtonForm';
            buttonForm.innerHTML = message + button;
            return buttonForm;
        },

        createNewPersonForm: function( ) {
            var tbl_top = "<div style='border: 2px solid black; background-color: rgb(224, 224, 224);' ><table><tr><th></th><th>" + omsgs.addNewPerson + ":</th></tr>";

            var r1d1 = "<tr><td>" + omsgs.givenName + "</td>";
            var r1d2 = "<td><input type='text' name='ipName' id='givenName' size='30' value=''/></td>";
            var r1d3 = "</tr><tr><td></td><td><span id='errorGiven' name='error' class='error'>" + omsgs.givenName + ": " + omsgs.nameRequired + "</span></td></tr>";
            
            var r2d1 = "<tr><td>" + omsgs.middleName + "</td>";
            var r2d2 = "<td><input type='text' name='ipName' id='middleName' size='30' value='' /></td></tr>";

            var r3d1 = "<tr><td>" + omsgs.familyName + "</td>"
            var r3d2 = "<td><input type='text' name='ipName' id='familyName' size='30' value='' /></td>";
            var r3d3 = "</tr><tr><td></td><td><span id='errorFamily' name='error' class='error'>" + omsgs.familyName + ": " + omsgs.nameRequired + "</span></td></tr>";

            var r4d1 = "<tr><td>" + omsgs.birthdate + "<br/><input type='hidden' id='datepattern' value='" + omsgs.datePattern + "'/><i style='font-weight: normal; font-size: 0.8em;'>(" + omsgs.format + ": " + omsgs.datePattern + ")</i></td>";
            var r4d2 = "<td valign='top'><input type='text' name='ipDate' id='birthdate' size='11' value='' onclick='showCalendar(this)' />" + omsgs.or + "<input type='text' name='ipDate' id='age' size='5' value='' /><td>";
            var r4d3 = "</tr><tr><td></td><td><span id='errorAge' name='error' class='error'>" + omsgs.birthdateRequired + "</span></td>";

            var r5d1 = "<tr><td>" + omsgs.gender + "</td>";
            var r5d2 = "<td>";
            for (var i=0; i<omsgs.genderArray.length; i++) {
            	r5d2 += "<input type='radio' name='ipGender' id='gender-" + omsgs.genderArray[i].key + "' value='" + omsgs.genderArray[i].key + "' /><label for='gender-" + omsgs.genderArray[i].key + "'> " + omsgs.genderArray[i].msg + " </label>";
            }
            r5d2 += "</td>";
            
            var r5d3 = "</tr><tr><td></td><td><span id='errorGender' name='error' class='error'>" + omsgs.genderRequired + "</span></td>"; 

            var r6d1 = "<tr><td></td><td><input type='submit' id='submitPerson' value='" + omsgs.personCreate + "' /><input type='button' name='showhide' id='cancel' value='" + omsgs.cancel + "'/></td></tr>";

            var tbl_bot = "<input type='hidden' name='personType' value=''/><input type='hidden' name='viewType' value='shortEdit'/></table></div>";

            var newPersonForm = document.createElement("form");
            newPersonForm.id = "newPersonForm";
            newPersonForm.style.display = "none";    
            newPersonForm.onsubmit = function() {return false};   
            newPersonForm.innerHTML = tbl_top + r1d1 + r1d2 + r1d3 + r2d1 + r2d2 + r3d1 + r3d2 + r3d3 + r4d1 + r4d2 + r4d3 + r5d1 + r5d2 + r5d3 + r6d1 + tbl_bot;

            return newPersonForm;
        }
        
	},
	"html"
);

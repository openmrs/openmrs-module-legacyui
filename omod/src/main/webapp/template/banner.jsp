<div id="banner"
     xmlns:jsp="https://jakarta.ee/xml/ns/jakartaee"
     xmlns:openmrs="urn:jsptld:/WEB-INF/view/module/legacyui/taglibs/openmrs.tld">
<a href="<%= request.getContextPath() %>/index.htm">
  <div id="logosmall"><img src="<%= request.getContextPath() %>/moduleResources/legacyui/images/openmrs_logo_text_small.png" alt="OpenMRS Logo" border="0"/></div>
</a>
<table id="bannerbar">
  <tr>
    <td id="logocell"> <img src="<%= request.getContextPath() %>/images/openmrs_logo_white.gif" alt="" class="logo-reduced61" />
    </td>
	<td id="barcell">
        <div class="barsmall" id="barsmall">
        <img align="left" src="<%= request.getContextPath() %>/moduleResources/legacyui/images/openmrs_green_bar.gif" alt="" class="bar-round-reduced50" id="bar-round-reduced50"/>
         <openmrs:hasPrivilege privilege="View Navigation Menu">
 				<%@ include file="/WEB-INF/view/module/legacyui/template/gutter.jsp" %>
 		</openmrs:hasPrivilege>
         </div>
        <script type="text/javascript">
        	function resize(){
			document.getElementById('bar-round-reduced50').style.height = document.getElementById('barsmall').offsetHeight+"px";
        	}
        	window.onload=resize;
			window.onresize=resize;
		</script>
        </div>
    </td>
  </tr>
</table>
</div>

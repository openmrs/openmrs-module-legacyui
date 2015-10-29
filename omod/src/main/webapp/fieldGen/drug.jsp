<%@ include file="/WEB-INF/view/module/legacyui/template/include.jsp" %>

<openmrs_tag:drugField formFieldName="${model.formFieldName}" initialValue="${model.initialValue}" drugs="${model.drugs}" optionHeader="${model.optionHeader}" onChange="${model.onChange}" includeVoided="${model.includeVoided}" />

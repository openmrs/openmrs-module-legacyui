<%@ include file="/WEB-INF/view/module/legacyui/template/include.jsp" %>

<openmrs_tag:listField formFieldName="${model.formFieldName}" initialValue="${model.initialValue}" list="${model.list}"
                       optionHeader="${model.optionHeader}" onChange="${model.onChange}"
                       includeVoided="${model.includeVoided}"/>

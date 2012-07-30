<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<fmt:setBundle basename="com.docdoku.server.localization.admin.workspaceEditionForm_resource"/>
<head>
    <meta content="text/html; charset=UTF-8" http-equiv="Content-Type"/>
    <title><fmt:message key="title"/></title>
    <link rel="Shortcut Icon" type="image/ico" href="<%=request.getContextPath()%>/images/favicon.ico"/>
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/css/main.css" media="screen"/>
    <meta http-equiv="Content-Script-Type" content="text/javascript"/>
    <script src="<%=request.getContextPath()%>/js/adminForm.js" type="text/javascript"></script>
</head>

<body>
<div id="page">
<%@ include file="/WEB-INF/header.jspf" %>

<div id="content">

<h2><fmt:message key="main.title"/></h2>

<h3><fmt:message key="section.title"/></h3>

<form action="<%=request.getContextPath()%>/admin/workspace/updateWorkspace" method="post" onsubmit="javascript: return checkForm();" name="workspace" id="workspace"><fieldset>
    <label for="id"><fmt:message key="section.id"/></label>
    <span id="id"><c:out value="${selectedWorkspace.id}"/></span><br/>
    
    <label for="admin"><fmt:message key="section.admin"/>:</label>
    <select id="admin" name="admin">
        <c:forEach var="item" items="${users}">
            <option <c:if test="${selectedWorkspace.admin.login==item.login}">selected="selected"</c:if> value="${item.login}">${item.name}</option>
        </c:forEach>
    </select>    
    <label for="description"><fmt:message key="section.description"/>:</label>
    <textarea id="description" name="description" cols="40" rows="4">${selectedWorkspace.description}</textarea>
    <input name="folderLocked" <c:if test="${selectedWorkspace.folderLocked}">checked="checked" </c:if>id="folderLocked" value="true" class="box" type="checkbox"/>
           <label for="folderLocked"><fmt:message key="section.freeze_folders"/></label>
    <input type="submit" class="button" value="<fmt:message key="section.save"/>" name="submit" alt="<fmt:message key="section.save"/>"/>
</fieldset></form>

</div>
<%@ include file="/WEB-INF/footer.jspf" %>
</div>
<script type="text/javascript">
    var fieldsArray = new Array(1);
    fieldsArray[0] = new Array(2);
    fieldsArray[0][0] = document.workspace.id;
    fieldsArray[0][1] = '<fmt:message key="section.id"/>';
    function checkForm(){
        return checkMandatoryFields(fieldsArray,'<fmt:message key="section.mandatoryMessage"/>');
        }
        </script>

</body>
</html>
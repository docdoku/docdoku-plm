<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
    <fmt:setBundle basename="com.docdoku.server.localization.admin.groupCreationForm_resource"/>
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
            <div id="sidebar">
                <h3><fmt:message key="sidebar.title"/></h3>
                <p><fmt:message key="sidebar.text"/></p>
            </div>
            <div id="content">

                <h2><fmt:message key="main.title"/></h2>
                <h3><fmt:message key="section.title"/></h3>
                
                <form action="<%=request.getContextPath()%>/admin/createGroup" method="post" onsubmit="javascript: return checkForm();" name="group" id="group"><fieldset>
                        <label for="id"><fmt:message key="section.id"/>*:</label>
                        <input name="id" id="id" type="text" size="20" maxlength="50"/>
                        <input type="submit" class="button" value="<fmt:message key="section.submit"/>" name="submit" alt="<fmt:message key="section.submit"/>"/>
                </fieldset></form>
                
            </div>
            <%@ include file="/WEB-INF/footer.jspf" %>
        </div>

        <script type="text/javascript">
        var fieldsArray = new Array(1);
        fieldsArray[0] = new Array(2);
        fieldsArray[0][0] = document.group.id;
        fieldsArray[0][1] = '<fmt:message key="section.id"/>';
        function checkForm(){
           return checkMandatoryFields(fieldsArray,'<fmt:message key="section.mandatoryMessage"/>');
         }
        </script>
    </body>
</html>
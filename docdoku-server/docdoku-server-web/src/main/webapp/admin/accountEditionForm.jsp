<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<fmt:setBundle basename="com.docdoku.server.localization.admin.accountEditionForm_resource"/>

<html xmlns="http://www.w3.org/1999/xhtml">
    <head>
        <meta content="text/html; charset=UTF-8" http-equiv="Content-Type"/>
        <title><fmt:message key="title"/></title>
        <link rel="Shortcut Icon" type="image/ico" href="<%=request.getContextPath()%>/images/favicon.ico"/>
        <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/css/main.css" media="screen"/>
        <meta http-equiv="Content-Script-Type" content="text/javascript"/>
        <script src="<%=request.getContextPath()%>/js/adminForm.js" type="text/javascript"></script>
    </head>
    
    <jsp:useBean id="account" type="com.docdoku.core.common.Account" scope="session"/>
    
    <body>
        <div id="page">
            <%@ include file="/WEB-INF/header.jspf" %>
            
            <div id="content">
                
                <h2><fmt:message key="main.title"/></h2>
                
                
                <h3><fmt:message key="section.title"/></h3>
                
                <form action="<%=request.getContextPath()%>/admin/updateAccount" method="post" onsubmit="javascript: return checkForm();" name="account" id="account"><fieldset>
                        <label for="login"><fmt:message key="section.login"/></label>
                        <span id="login"><c:out value="${account.login}"/></span><br/>
                        
                        <label for="name"><fmt:message key="section.name"/>*:</label>
                        <input value="${account.name}" name="name" id="name" type="text" size="20" maxlength="255"/>                       
                        
                        <label for="email"><fmt:message key="section.email"/>*:</label>
                        <input value="${account.email}" name="email" id="email" type="text" size="20" maxlength="255"/>
                        
                        
                        <label for="password"><fmt:message key="section.password"/>*:</label>
                        <input name="password" id="password" disabled="disabled" type="password" size="20" maxlength="50"/>
                        
                        <input name="changePassword" id="changePassword" class="box" type="checkbox"
                               onclick="document.account.password.disabled=!changePassword.checked"/>
                        <label for="changePassword"><fmt:message key="section.change_password"/></label>
                        <input type="submit" class="button" value="<fmt:message key="section.save"/>" name="submit" alt="<fmt:message key="section.save"/>"/>
                </fieldset></form>
                
            </div>
            <%@ include file="/WEB-INF/footer.jspf" %>
            
        </div>
        <script type="text/javascript">
        var fieldsArray = new Array(3);
        fieldsArray[0] = new Array(2);
        fieldsArray[1] = new Array(2);
        fieldsArray[2] = new Array(2);
        fieldsArray[0][0] = document.account.name;
        fieldsArray[0][1] = '<fmt:message key="section.name"/>';
        fieldsArray[1][0] = document.account.email;
        fieldsArray[1][1] = '<fmt:message key="section.email"/>';
        fieldsArray[2][0] = document.account.password;
        fieldsArray[2][1] = '<fmt:message key="section.password"/>';
        function checkForm(){
           return checkMandatoryFields(fieldsArray,'<fmt:message key="section.mandatoryMessage"/>');
        }
        </script>
        
    </body> 
</html>
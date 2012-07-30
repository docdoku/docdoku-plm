<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
    <fmt:setBundle basename="com.docdoku.server.localization.registrationForm_resource"/>
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
                <p><fmt:message key="sidebar.text">
                    <fmt:param value="<%=request.getContextPath()%>"/>
                </fmt:message></p>         
            </div>
            <div id="content">
                <h2><fmt:message key="main.title"/></h2>
                
                <h3><fmt:message key="section.title"/></h3>
                
                <form name="registerForm" action="<%=request.getContextPath()%>/register" method="post" onsubmit="javascript: return checkForm();">
                    <fieldset>
                        <label for="login"><fmt:message key="section.userid"/>* :</label>
                        <input name="login" id="login" type="text" size="20" maxlength="50"/>
                        
                        <label for="name"><fmt:message key="section.name"/>* :</label>
                        <input name="name" id="name" type="text" size="20" maxlength="255"/>
                        
                        <label for="email"><fmt:message key="section.email"/>* :</label>
                        <input name="email" id="email" type="text" size="20" maxlength="255"/>
                        
                        <label for="password"><fmt:message key="section.password"/>* :</label>
                        <input name="password" id="password" type="password" size="20" maxlength="50"/>
                        
                        <label for="confirmpassword"><fmt:message key="section.confirmPassword"/>* :</label>
                        <input name="confirmpassword" id="confirmpassword" type="password" size="20" maxlength="50"/>
                        
                        <hr/>
                        <input type="submit" class="button" value="<fmt:message key="section.signup"/>" name="submit" alt="<fmt:message key="section.signup"/>"/>
                    </fieldset>
                </form>
            </div>
            
            <%@ include file="/WEB-INF/footer.jspf" %>
        </div>
        <script type="text/javascript">
        var fieldsArray = new Array(5);
        fieldsArray[0] = new Array(2);
        fieldsArray[1] = new Array(2);
        fieldsArray[2] = new Array(2);
        fieldsArray[3] = new Array(2);
        fieldsArray[4] = new Array(2);
        fieldsArray[0][0] = document.registerForm.login;
        fieldsArray[0][1] = '<fmt:message key="section.userid"/>';
        fieldsArray[1][0] = document.registerForm.name;
        fieldsArray[1][1] = '<fmt:message key="section.name"/>';
        fieldsArray[2][0] = document.registerForm.email;
        fieldsArray[2][1] = '<fmt:message key="section.email"/>';
        fieldsArray[3][0] = document.registerForm.password;
        fieldsArray[3][1] = '<fmt:message key="section.password"/>';
        fieldsArray[4][0] = document.registerForm.confirmpassword;
        fieldsArray[4][1] = '<fmt:message key="section.confirmPassword"/>';
        function checkForm(){
          if (checkMandatoryFields(fieldsArray,'<fmt:message key="section.mandatoryMessage"/>')){
            return areEquals(document.registerForm.password,document.registerForm.confirmpassword,'<fmt:message key="section.confirmMessage"/>');
          } else return false;
        }
        </script>
        
    </body>
</html>
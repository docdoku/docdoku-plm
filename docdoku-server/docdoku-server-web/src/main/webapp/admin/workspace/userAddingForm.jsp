<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
    <fmt:setBundle basename="com.docdoku.server.localization.admin.userAddingForm_resource"/>
    <head>
        <meta content="text/html; charset=UTF-8" http-equiv="Content-Type"/>
        <title><fmt:message key="title"/></title>
        <link rel="Shortcut Icon" type="image/ico" href="<%=request.getContextPath()%>/images/favicon.ico"/>
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
                
                <form action="<%=request.getContextPath()%>/admin/workspace/addUser?group=${param["group"]}" method="post"><fieldset>
                        <label for="login"><fmt:message key="section.login"/>:</label>
                        <input name="login" id="login" type="text" size="20" maxlength="50"/>
                        <input type="submit" class="button" value="<fmt:message key="section.add"/>" name="submit" alt="<fmt:message key="section.add"/>"/>
                </fieldset></form>
                
            </div>
            <%@ include file="/WEB-INF/footer.jspf" %>
        </div>
        
    </body>
</html>
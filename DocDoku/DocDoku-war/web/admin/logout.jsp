<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
    <fmt:setBundle basename="com.docdoku.server.localization.admin.logout_resource"/>
    <head>
        <meta content="text/html; charset=UTF-8" http-equiv="Content-Type"/>
        <title><fmt:message key="title"/></title>
        <link rel="Shortcut Icon" type="image/ico" href="<%=request.getContextPath()%>/images/favicon.ico"/>
        <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/css/docdoku.css" media="screen"/>
    </head>
    
    <c:remove var="account" scope="session"/>
    <c:remove var="administeredWorkspaces" scope="session"/>
    <c:remove var="regularWorkspaces" scope="session"/>
    <c:remove var="selectedWorkspace" scope="session"/> 
    <%new com.sun.appserv.security.ProgrammaticLogin().logout();%>
    
    <body>
        
        <div id="page">
            <%@ include file="/WEB-INF/header.jspf" %>
            <div id="content">
                <h3><fmt:message key="section.title"/></h3>   
                <p><fmt:message key="section.text">
                    <fmt:param value="<%=request.getContextPath()%>"/>
                </fmt:message></p>
            </div>
        </div>
        
    </body>
</html>
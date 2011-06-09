<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
    <fmt:setBundle basename="com.docdoku.server.localization.explorer_resource"/>
    <head>
        <meta content="text/html; charset=UTF-8" http-equiv="Content-Type"/>
        <meta name="gwt:property" content="locale=<%=request.getLocale()%>"/>
        <title><fmt:message key="title"/></title>
        <link rel="Shortcut Icon" type="image/ico" href="<%=request.getContextPath()%>/images/favicon.ico"/>
        <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/css/explorer.css"/>
        <script type="text/javascript">
            var inputs = {
                workspaceID: "${workspaceID}",
                login: "${login}"
            };
            
        </script>   
    </head>    
    <body>      
        <div id="page">
            <%@ include file="/WEB-INF/header.jspf" %>
        </div>
        <div id="content"></div>
        <script type="text/javascript" src="<%=request.getContextPath()%>/gwtExplorer/gwtExplorer.nocache.js"></script>
    </body>
</html>
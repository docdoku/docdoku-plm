<%@ page contentType="text/html;charset=UTF-8" isErrorPage="true" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">
    <head>
        <meta content="text/html; charset=UTF-8" http-equiv="Content-Type"/>
        <title>DocDoku: error</title>
        <link rel="Shortcut Icon"
              type="image/ico" href="<%=request.getContextPath()%>/images/favicon.ico"/>
        <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/css/main.css" media="screen"/>
    </head>
    <%
            application.log("Unexpected error:", exception);
    %>
    <body>
        
        <div id="page">
            
            <%@ include file="/WEB-INF/header.jspf" %>
            
            <div id="content">
                
                <div id="main">
                    <h2>Error</h2>
                    
                    <h3>Error:</h3>
                    <blockquote>
                        <c:out value="<%=exception.getMessage()%>"/>
                    </blockquote>
                    
                </div>
                
            </div>
            
        </div>
        
    </body>
</html>
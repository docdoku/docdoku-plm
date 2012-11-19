<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
    <fmt:setBundle basename="com.docdoku.server.localization.index_resource"/>
    <head>
        <meta content="text/html; charset=UTF-8" http-equiv="Content-Type"/>
        <title><fmt:message key="title"/></title>

        <link rel="Shortcut Icon" type="image/ico" href="<%=request.getContextPath()%>/images/favicon.ico"/>
        <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/css/font-awesome.css"/>
        <link rel="stylesheet/less" type="text/css" href="<%=request.getContextPath()%>/less/common/style.less"/>

        <script src="<%=request.getContextPath()%>/js/lib/jquery-1.7.1.min.js"></script>
        <script src="<%=request.getContextPath()%>/js/lib/less-1.2.1.min.js"></script>
        <script src="<%=request.getContextPath()%>/js/lib/bootstrap-2.0.2.min.js"></script>
    </head> 
    
    <body>

        <div id="page">

            <div id="content">

Sorry, this room is full. <a href="${roomLink}">Click here</a> to try again.  

            </div>
        </div>
    </body>
</html>
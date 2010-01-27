<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
    <fmt:setBundle basename="com.docdoku.server.localization.download_resource"/>
    <head>
        <meta content="text/html; charset=UTF-8" http-equiv="Content-Type"/>
        <title><fmt:message key="title"/></title>
        <link rel="Shortcut Icon" type="image/ico" href="<%=request.getContextPath()%>/images/favicon.ico"/>
        <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/css/docdoku.css" media="screen"/>
        <meta http-equiv="Content-Script-Type" content="text/javascript"/>
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
                
                <h3><fmt:message key="section1.title1"/></h3>
                
                <p>
                    <fmt:message key="section.supported"/><br/>
                    <img src="<%=request.getContextPath()%>/images/windows.gif" alt="Windows"/> windows<br/>
                    <img src="<%=request.getContextPath()%>/images/linux.gif" alt="Linux"/> linux<br/>
                    <img src="<%=request.getContextPath()%>/images/apple.gif" alt="Mac"/> mac<br/>
                </p>
                <p><fmt:message key="section1.text1"/></p>
                <p><fmt:message key="section1.text2"/></p>
                <!--<script type="text/javascript" src="http://java.com/js/deployJava.js"></script>
                <script src="<%=request.getContextPath()%>/js/deployJava.js"></script>
                <script type="text/javascript">
                    deployJava.createWebStartLaunchButton('<%=new java.net.URL(new java.net.URL(request.getRequestURL().toString()), request.getContextPath()+"/apps/docdoku_client.jnlp")%>','1.6.0_04');
                </script>-->
                <a href="<%=request.getContextPath()%>/apps/docdoku_client.jnlp">Go!</a>
            </div>
            <%@ include file="/WEB-INF/footer.jspf" %>
        </div>
        
    </body>
</html>
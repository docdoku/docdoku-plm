<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
    <fmt:setBundle basename="com.docdoku.server.localization.help.help_resource"/>
    <head>
        <meta content="text/html; charset=UTF-8" http-equiv="Content-Type"/>
        <title><fmt:message key="title"/></title>
        <link rel="Shortcut Icon" type="image/ico" href="<%=request.getContextPath()%>/images/favicon.ico"/>
        <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/css/main.css" media="screen"/>
    </head>
    
    <body>
        
        <div id="page">
            
            <%@ include file="/WEB-INF/header.jspf" %>
            
            <div id="content">
                
                <h2><fmt:message key="help.title"/></h2>
                
                <div align="center">
                    <h3><a href="<%=request.getContextPath()%>/help/help.jsp"><fmt:message key="category1.title"/></a> | <a href="<%=request.getContextPath()%>/help/adminHelp.jsp"><fmt:message key="category2.title"/></a> | <a href="<%=request.getContextPath()%>/help/documentHelp.jsp"><fmt:message key="category3.title"/></a> | <a href="<%=request.getContextPath()%>/help/workflowHelp.jsp"><fmt:message key="category4.title"/></a></h3>
                    <h3><a href="<%=request.getContextPath()%>/help/templateHelp.jsp"><fmt:message key="category5.title"/></a> | <a href="<%=request.getContextPath()%>/help/tagHelp.jsp"><fmt:message key="category6.title"/></a> | <a href="<%=request.getContextPath()%>/help/aboutHelp.jsp"><fmt:message key="category8.title"/></a></h3>
                </div>
                
                <div class="headline">
                    <fmt:message key="category4.title"/>
                </div>
                
                <h4><fmt:message key="category4.question1"/></h4>
                <blockquote><p><fmt:message key="category4.answer1"/></p></blockquote>
                <h4><fmt:message key="category4.question2"/></h4>
                <blockquote><p><fmt:message key="category4.answer2"/></p></blockquote>
                <h4><fmt:message key="category4.question3"/></h4>
                <blockquote><p><fmt:message key="category4.answer3"/></p></blockquote>
                <h4><fmt:message key="category4.question4"/></h4>
                <blockquote><p><fmt:message key="category4.answer4"/></p></blockquote>
                <h4><fmt:message key="category4.question5"/></h4>
                <blockquote><p><fmt:message key="category4.answer5"/></p></blockquote>
                
            </div>
            <%@ include file="/WEB-INF/footer.jspf" %>
        </div>
        
    </body>
</html>

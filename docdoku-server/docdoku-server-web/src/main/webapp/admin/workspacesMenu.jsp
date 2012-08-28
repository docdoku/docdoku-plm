<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<fmt:setBundle basename="com.docdoku.server.localization.admin.workspacesMenu_resource"/>
    <head>
        <meta content="text/html; charset=UTF-8" http-equiv="Content-Type"/>
        <title><fmt:message key="title"/></title>
        <link rel="Shortcut Icon" type="image/ico" href="<%=request.getContextPath()%>/images/favicon.ico"/>
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
                    
                        <h3><fmt:message key="section.title"/></h3>
                        <p><a href="<%=request.getContextPath()%>/admin/workspaceCreationForm.jsp"><fmt:message key="section.create"/></a></p>
                        <form action="#" method="get" name="workspace" id="workspace"><fieldset>
                                <label for="selectedWorkspace"><fmt:message key="section.select"/>:</label>
                                <select id="selectedWorkspace" name="selectedWorkspace">
                                    <option disabled="disabled"><fmt:message key="section.administered"/></option>
                                    <c:forEach var="item" items="${administeredWorkspaces}">
                                        <option <c:if test="${selectedWorkspace.id==item.value.id}">selected="selected"</c:if> value="${item.value.id}">${item.value.id}</option>
                                    </c:forEach>
                                </select>        
                                    <a onclick="document.workspace.action='<%=request.getContextPath()%>/admin/workspace/workspaceEditionForm';document.workspace.submit();"
                                                                                                                                                         href="#"><fmt:message key="section.edit"/></a> | <a onclick="document.workspace.action='<%=request.getContextPath()%>/admin/workspace/manageUsers';document.workspace.submit();" href="#"><fmt:message key="section.manage"/></a>
                        </fieldset></form>
                
            </div>
            
            <%@ include file="/WEB-INF/footer.jspf" %>
        </div>

    </body>
</html>
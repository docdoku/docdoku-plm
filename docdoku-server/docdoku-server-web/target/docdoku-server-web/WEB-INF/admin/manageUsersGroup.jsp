<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
    <fmt:setBundle basename="com.docdoku.server.localization.admin.manageUsersGroup_resource"/>
    <head>
        <meta content="text/html; charset=UTF-8" http-equiv="Content-Type"/>
        <title><fmt:message key="title"/></title>
        <link rel="Shortcut Icon" type="image/ico" href="<%=request.getContextPath()%>/images/favicon.ico"/>
        <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/css/main.css" media="screen"/>
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
                <h3><fmt:message key="section.title1"/> ${group.id} <fmt:message key="section.title2"/></h3>
                
                <form action="<%=request.getContextPath()%>/admin/workspace/manageUsersGroup?action=remove&group=${group.id}" id="users" name="users" method="post"><fieldset>
                        <table>
                            <tr>
                                <th scope="col">&nbsp;</th>
                                <th scope="col"><fmt:message key="section.name"/></th>
                                <th scope="col"><fmt:message key="section.email"/></th>
                            </tr>
                            <c:forEach var="user" items="${group.users}">
                                <tr>
                                    <td valign="middle">&nbsp;<input type="checkbox" value="${user.login}" class="box" name="users"/></td>
                                    <td>${user.name}</td>
                                    <td><a href="mailto:${user.email}">${user.email}</a></td>                                  
                                </tr>
                            </c:forEach>                           
                        </table>
                        <a href="<%=request.getContextPath()%>/admin/workspace/manageUsers"><fmt:message key="section.back"/></a> | <a href="<%=request.getContextPath()%>/admin/workspace/userAddingForm.jsp?group=${group.id}"><fmt:message key="section.addUser"/></a> | <a href="#" onclick="document.users.submit();"><fmt:message key="section.remove"/></a>
                </fieldset></form>                
            </div>
            <%@ include file="/WEB-INF/footer.jspf" %>
        </div>
        
    </body>
</html>
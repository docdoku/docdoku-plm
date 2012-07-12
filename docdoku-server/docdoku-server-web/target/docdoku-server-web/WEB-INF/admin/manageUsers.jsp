<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
    <fmt:setBundle basename="com.docdoku.server.localization.admin.manageUsers_resource"/>
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
                <h3><fmt:message key="section.title1"/> ${selectedWorkspace.id} <fmt:message key="section.title2"/></h3>

                <form action="<%=request.getContextPath()%>/admin/workspace/manageUsers" method="post"><fieldset>
                        <table>
                            <tr>
                                <th scope="col">&nbsp;</th>
                                <th scope="col"><fmt:message key="section.name"/></th>
                                <th scope="col"><fmt:message key="section.email"/></th>
                                <th scope="col"><fmt:message key="section.right"/></th>
                            </tr>
                            <c:forEach var="user" items="${users}">
                                <c:if test="${(usersGroups[user.login]==null) || (userMembers[user.login]!=null) || (user.login == selectedWorkspace.admin.login)}">
                                    <tr>
                                        <td valign="middle"><c:choose>
                                                <c:when test="${user.login == selectedWorkspace.admin.login}">&nbsp;</c:when>
                                                <c:otherwise>&nbsp;<input type="checkbox" value="${user.login}" class="box" name="users"/></c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td><span <c:if test="${userMembers[user.login]==null && user.login != selectedWorkspace.admin.login}">class="disable"</c:if>>${user.name}</span></td>
                                        <td><a href="mailto:${user.email}">${user.email}</a></td>
                                        <td><c:choose>
                                                <c:when test="${userMembers[user.login]==null}"></c:when>
                                                <c:when test="${userMembers[user.login].readOnly}"><fmt:message key="section.read"/></c:when>
                                                <c:otherwise><fmt:message key="section.full"/></c:otherwise>
                                            </c:choose>
                                        </td>
                                    </tr>
                                </c:if>
                            </c:forEach>
                            <c:forEach var="group" items="${groups}">
                                <tr>
                                    <td valign="middle">
                                        &nbsp;<input type="checkbox" value="${group.id}" class="box" name="groups"/>
                                    </td>
                                    <td><a href="<%=request.getContextPath()%>/admin/workspace/manageUsersGroup?group=${group.id}"><span <c:if test="${groupMembers[group.id]==null}">class="disable"</c:if>>${group.id}</span></a></td>
                                    <td></td>
                                    <td><c:choose>
                                            <c:when test="${groupMembers[group.id]==null}"></c:when>
                                            <c:when test="${groupMembers[group.id].readOnly}"><fmt:message key="section.read"/></c:when>
                                            <c:otherwise><fmt:message key="section.full"/></c:otherwise>
                                        </c:choose>
                                    </td>
                                </tr>
                            </c:forEach>
                        </table>
                        <select name="action" id="action" onchange="submit();">
                            <option><fmt:message key="section.actions"/></option>
                            <option disabled="disabled">----------------</option>
                            <option value="remove"><fmt:message key="section.remove"/></option>
                            <option value="disable"><fmt:message key="section.disable"/></option>
                            <option value="enable"><fmt:message key="section.enable"/></option>
                            <option disabled="disabled">----------------</option>
                            <option value="read"><fmt:message key="section.read"/></option>
                            <option value="full"><fmt:message key="section.full"/></option>
                        </select>
                        <a href="<%=request.getContextPath()%>/admin/workspace/userAddingForm.jsp"><fmt:message key="section.addUser"/></a> | <a href="<%=request.getContextPath()%>/admin/workspace/groupCreationForm.jsp"><fmt:message key="section.createGroup"/></a>
                </fieldset></form>
            </div>
            <%@ include file="/WEB-INF/footer.jspf" %>
        </div>

    </body>
</html>
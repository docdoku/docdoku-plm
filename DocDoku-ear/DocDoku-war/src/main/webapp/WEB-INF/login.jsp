<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
    <fmt:setBundle basename="com.docdoku.server.localization.index_resource"/>
    <head>
        <meta content="text/html; charset=UTF-8" http-equiv="Content-Type"/>
        <title><fmt:message key="title"/></title>
        <meta name="keywords" content="<fmt:message key="meta.keywords"/>" />
        <meta name="description" content="<fmt:message key="meta.description"/>" />
        <link rel="Shortcut Icon" type="image/ico" href="<%=request.getContextPath()%>/images/favicon.ico"/>
        <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/css/docdoku.css" media="screen"/>
    </head>

    <body>
        <div id="page">
            <%@ include file="/WEB-INF/header.jspf" %>

            <div id="content">

                <div id="panel">
                    <div id="loginpanel">
                        <h3><fmt:message key="login.title"/></h3>

                        <form action="<%=request.getContextPath()%>/home" method="post"><fieldset>
                                <p><label for="login"><fmt:message key="login.user"/></label>
                                    <input name="login" id="login" type="text" size="20" maxlength="50"/>
                                </p>

                                <p><label for="password"><fmt:message key="login.password"/></label>
                                    <input name="password" id="password" type="password" size="20" maxlength="50"/>
                                </p>

                                <p><input type="submit" class="button" value="Login" name="auth" alt="Login"/>
                                </p>
                                <br/>
<!--                                <p><a href="<%=request.getContextPath()%>/faces/recoveryForm.xhtml"><fmt:message key="login.recovery"/></a></p>-->
                                <p><a href="<%=request.getContextPath()%>/registrationForm.jsp"><fmt:message key="login.subscribe"/></a></p>
                            </fieldset>
                        </form>
                    </div>

                </div>
            </div>

            <%@ include file="/WEB-INF/footer.jspf" %>
        </div>

    </body>
</html>
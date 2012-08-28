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
        <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/css/font-awesome.css"/>
        <link rel="stylesheet/less" type="text/css" href="<%=request.getContextPath()%>/less/common/style.less"/>

        <script src="<%=request.getContextPath()%>/js/lib/jquery-1.7.1.min.js"></script>
        <script src="<%=request.getContextPath()%>/js/lib/less-1.2.1.min.js"></script>
        <script src="<%=request.getContextPath()%>/js/lib/bootstrap-2.0.2.min.js"></script>

    </head>

    <body>

        <div id="page">
            <%@ include file="/WEB-INF/header.jspf" %>



            <div id="content">
                <div class="well" id="login_form_container">
                    <h3><i class="icon-lock"></i><fmt:message key="login.title"/></h3>
                    <form action="<%=request.getContextPath()%>/home" method="post" id="login_form">

                        <p><label for="login"><fmt:message key="login.user"/></label>
                            <input name="login" id="login" type="text" size="20" maxlength="50"/>
                        </p>

                        <p><label for="password"><fmt:message key="login.password"/></label>
                            <input name="password" id="password" type="password" size="20" maxlength="50"/>
                        </p>

                        <p id="login_button_container">
                            <input type="submit" class="btn header_btn-custom" value="Login" name="auth" alt="Login" id="login_button"/>
                        </p>

                        <p>
                            <a href="<%=request.getContextPath()%>/faces/recoveryRequestForm.xhtml"><fmt:message key="login.recovery"/></a>
                        </p>

                    </form>
                </div>

            </div>

            <%@ include file="/WEB-INF/footer.jspf" %>
        </div>
    </body>
</html>
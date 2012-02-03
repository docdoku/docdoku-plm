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
        <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/css/bootstrap.css"/>
        <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/css/explorer.css"/>
        <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/css/header-nav.css"/>
        <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/css/sidebar-nav.css"/>
        <script src="<%=request.getContextPath()%>/js/lib/jquery-1.7.1.min.js"></script>
        <script src="<%=request.getContextPath()%>/js/lib/bootstrap.min.js"></script>
    <script src="<%=request.getContextPath()%>/js/lib/underscore.js"></script>
    <script src="<%=request.getContextPath()%>/js/lib/backbone.js"></script>
        <script type="text/javascript">
            $(document).ready(function () {
                //                $("'.dropdown-toggle'").dropdown();
            });
            var inputs = {
                workspaceID: "${workspaceID}",
                login: "${login}"
            };
            
        </script>   
    </head>    
    <body>

        <%@ include file="/WEB-INF/explorer_header.jspf" %>

        <div class="container-fluid">
            <div class="row-fluid">
                <div class="span2">
                    <div class="sidebar-nav">
                        <h1 id="workspace_name">${workspaceID}</h1>
<!--                         <ul id="sidebar_filetree_container">
                            
                            <li class="closed_folder" id="account_home_folder">${login}</li>
 
                            <li class="open_folder">Formations                                                        
                                <ul>
                                    <li><a href="#" class="subfolder closed_folder">Subfolder 1</a></li>
                                    <li><a href="#" class="subfolder closed_folder">Subfolder 2</a></li>
                                    <li><a href="#" class="subfolder closed_folder">Subfolder 3</a></li>
                                    <li><a href="#" class="subfolder closed_folder">Subfolder 4</a></li>
                                </ul>
                            </li>

                            <li class="closed_folder">Sico</li>

                            <li class="closed_folder">Webinage</li>
                            
                        </ul> -->
                    </div>                    
                </div>
                <div class="span7">
                    <div class="content">

                    </div>                     
                </div>
                <div class="span3"></div>               
            </div>
        </div>
    </body>

    <script src="<%=request.getContextPath()%>/js/sidebar-nav.js"></script>

</html>
<%@ page import="com.docdoku.core.common.*,com.docdoku.core.util.FileIO" contentType="text/html;charset=UTF-8"
         language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<fmt:setBundle basename="com.docdoku.server.localization.document_resource"/>
<head>
    <meta content="text/html; charset=UTF-8" http-equiv="Content-Type"/>
    <title><fmt:message key="title"/></title>
    <link rel="Shortcut Icon" type="image/ico" href="<%=request.getContextPath()%>/images/favicon.ico"/>
    <script type="text/javascript" src="<%=request.getContextPath()%>/js/switchPlayer.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/js/swfobject/swfobject.js"></script>

    <link rel="stylesheet/less" type="text/css" href="<%=request.getContextPath()%>/less/document/style.less"/>
    <script type="text/javascript" src="<%=request.getContextPath()%>/js/lib/less-1.3.3.min.js"></script>

    <script type="text/javascript" src="<%=request.getContextPath()%>/js/lib/jquery-1.8.2.min.js"></script>

    <script language="JavaScript">
        $(function () {
            if(location.hash){
                $("#header_left_menu a").css("backgroundColor", "transparent");
                $(".well:visible").hide();
                $(window.location.hash).show();
                ongletSelectionne();
            }

            $(window).bind('hashchange', function (e) {
                $(".well:visible").hide();
                $(window.location.hash).show();
                $("#header_left_menu a").css("backgroundColor", "transparent");
                ongletSelectionne();
            });

            function ongletSelectionne (){
                switch($(window.location.hash).attr('id'))
                {
                    case "general":
                        $(".g").css("backgroundColor", "#213251");
                        break;
                    case "iteration":
                        $(".i").css("backgroundColor", "#213251");
                        break;
                    case "attribut":
                        $(".a").css("backgroundColor", "#213251");
                        break;
                    case "file":
                        $(".f").css("backgroundColor", "#213251");
                        break;
                    case "link":
                        $(".l").css("backgroundColor", "#213251");
                        break;
                    default:
                        $("#header_left_menu").css("backgroundColor", "transparent");
                }
            }
        });
    </script>
</head>

<body>
<div class="navbar">
    <div class="navbar-fixed-top">
        <div class="navbar-inner">
            <div class="container-fluid">
                <div class="nav-collapse">
                    <ul class="nav" id="header_left_menu">
                        <li><img alt="docdoku_plm" src="/images/plm_logo2.png" class="brand-plm"/></li>
                        <li><a class="brand" style="color: white">&nbsp;&nbsp;&nbsp;DocDoku<strong>PLM</strong>&nbsp;&nbsp;&nbsp;</a></li>
                        <li><a><b>${docm}</b></a></li>
                        <li><a href="#general" style="color: white ; background-color: #213251" class="g"><fmt:message key="section1.title"/></a></li>
                        <li><a href="#iteration" style="color: white" class="i"><fmt:message key="section2.title"/></a></li>
                        <li><a href="#attribut" style="color: white" class="a"><fmt:message key="section2.attributs"/></a></li>
                        <li><a href="#file" style="color: white" class="f"><fmt:message key="sidebar.title1"/></a></li>
                        <li><a href="#link" style="color: white" class="l"><fmt:message key="sidebar.title2"/></a></li>
                    </ul>
                </div>
            </div>
        </div>
    </div>
</div>
<div id="page">
    <div id="content">
        <div id="sidebar">
            <div id="file" class="well">
                <center><h3><fmt:message key="sidebar.title1"/></h3></center><br />
                <c:if test="${docm.lastIteration.attachedFiles.size()!=0}">
                    <c:forEach var="item" varStatus="status" items="${docm.lastIteration.attachedFiles}">
                        <c:set scope="request" var="context" value="<%=request.getContextPath()%>"/>
                        <c:set scope="request" var="filePath" value="${context}/files/${item.fullName}"/>
                        <c:set scope="request" var="fileName" value="${item.name}"/>
                        <c:set scope="request" var="index" value="${status.index}"/>
                        <jsp:useBean scope="request" type="java.lang.String" id="filePath"/>
                        <c:choose>
                            <c:when test="<%=FileIO.isDocFile(filePath)%>">
                                <%@include file="/WEB-INF/docPlayer.jspf" %>
                            </c:when>
                            <c:when test="<%=FileIO.isAVFile(filePath)%>">
                                <%@include file="/WEB-INF/audioVideoPlayer.jspf" %>
                            </c:when>
                            <c:when test="<%=FileIO.isImageFile(filePath)%>">
                                <%@include file="/WEB-INF/imagePlayer.jspf" %>
                            </c:when>
                            <c:otherwise>
                                <a href="<%=filePath%>">${item.name}</a>
                            </c:otherwise>
                        </c:choose>
                        <br />
                        <br />
                    </c:forEach>
                </c:if>
                <c:if test="${docm.lastIteration.attachedFiles.size()==0}">
                    <div class="empty"><p><fmt:message key="section2.noFile"/></p></div>
                </c:if>
            </div>
            <div id="link" class="well">
                <center><h3><fmt:message key="sidebar.title2"/></h3></center><br />
                <p>
                    <c:if test="${docm.lastIteration.linkedDocuments.size()!=0}">
                        <c:forEach var="item" items="${docm.lastIteration.linkedDocuments}">
                            <li><a href="<%=request.getContextPath()%>/documents/${item.toDocumentWorkspaceId}/${item.toDocumentDocumentMasterId}/${item.toDocumentDocumentMasterVersion}">${item.toDocumentDocumentMasterId}-${item.toDocumentDocumentMasterVersion}-${item.toDocumentIteration}</a></li><br />
                        </c:forEach>
                    </c:if>
                    <c:if test="${docm.lastIteration.linkedDocuments.size()==0}">
                        <div class="empty"><p><fmt:message key="section2.noLink"/></p></div>
                    </c:if>
                </p>
            </div>
            <div id="attribut" class="well">
                <center><h3><fmt:message key="section2.attributs"/></h3></center><br />
                <p>
                <c:if test="${attr.size()!=0}">
                    <table class="table table-striped table-condensed">
                        <thead>
                        <tr>
                            <th><fmt:message key="section2.name"/></th>
                            <th><fmt:message key="section2.value"/></th>
                        </tr>
                        </thead>
                        <c:forEach var="item" items="${attr}">
                            <tbody class="items">
                            <tr>
                                <td>${item.name}</td>

                                <c:if test="${item.value.class=='class java.lang.Boolean'}">
                                    <c:if test="${item.value=='true'}">
                                        <td><input type="checkbox" checked="checked" disabled="disabled"></td>
                                    </c:if>
                                    <c:if test="${item.value=='false'}">
                                        <td><input type="checkbox" disabled="disabled"></td>
                                    </c:if>
                                </c:if>
                                <c:if test="${item.value.class=='class java.util.Date'}">
                                    <td><fmt:formatDate value="${item.value}" pattern="dd/MM/yyyy"/></td>
                                </c:if>
                                <c:if test="${item.class=='class com.docdoku.core.meta.InstanceURLAttribute'}">
                                    <td><a href="${item.value}">${item.value}</a></td>
                                </c:if>

                                <c:if test="${item.value.class!='class java.lang.Boolean' && item.value.class!='class java.util.Date' && item.class!='class com.docdoku.core.meta.InstanceURLAttribute'}">
                                    <td>${item.value}</td>
                                </c:if>
                            </tr>
                            </tbody>
                        </c:forEach>
                    </table>
                </c:if>
                <c:if test="${attr.size()==0}">
                    <div class="empty"><p><fmt:message key="section2.noAttribut"/></p></div>
                </c:if>
                </p>
            </div>
        </div>
    </div>
    <div id="main">
        <div id="general" class="well" style="display: inline">
            <center><h3><fmt:message key="section1.title"/></h3></center><br />
            <table class="table table-striped table-condensed">
                <tbody>
                <tr>
                    <th scope="row"><fmt:message key="section1.author"/>:</th>
                    <td>${docm.author.name}</td>
                </tr>
                <tr>
                    <th scope="row"><fmt:message key="section1.date"/>:</th>
                    <td><fmt:formatDate type="both" value="${docm.creationDate}"/></td>
                </tr>
                <tr>
                    <th scope="row"><fmt:message key="section1.type"/>:</th>
                    <td>${docm.type}</td>
                </tr>
                <tr>
                    <th scope="row"><fmt:message key="section1.titledoc"/>:</th>
                    <td>${docm.title}</td>
                </tr>
                <tr>
                    <th scope="row"><fmt:message key="section1.checkout_user"/>:</th>
                    <td>${docm.checkOutUser.name}</td>
                </tr>
                <tr>
                    <th scope="row"><fmt:message key="section1.checkout_date"/>:</th>
                    <td><fmt:formatDate type="both" value="${docm.checkOutDate}"/></td>
                </tr>
                <tr>
                    <th scope="row"><fmt:message key="section1.state"/>:</th>
                    <td>${docm.lifeCycleState}</td>
                </tr>
                <tr>
                    <th scope="row"><fmt:message key="section1.tag"/>:</th>
                    <td>${docm.tags}</td>
                </tr>
                <c:if test="${docm.description!=''}">
                    <tr>
                        <th scope="row"><fmt:message key="section1.description"/>:</th><td></td>
                    </tr>
                    <tr>
                        <td></td><td></td>
                    </tr>
                </c:if>
                </tbody>
            </table>
            <c:if test="${docm.description!=''}">
                <textarea name="" rows="5" cols="135" readonly="readonly" style="width: 492px">${docm.description}</textarea>
            </c:if>
        </div>
    </div>
    <div id="iteration" class="well">
        <center><h3><fmt:message key="section2.title"/></h3></center><br />
        <table class="table table-striped table-condensed">
            <tbody>
            <tr>
                <th scope="row"><fmt:message key="section2.iteration"/>:</th>
                <td>${docm.lastIteration.iteration}</td>
            </tr>
            <tr>
                <th scope="row"><fmt:message key="section2.author"/>:</th>
                <td>${docm.lastIteration.author.name}</td>
            </tr>
            <tr>
                <th scope="row"><fmt:message key="section2.date"/>:</th>
                <td><fmt:formatDate type="both" value="${docm.lastIteration.creationDate}"/></td>
            </tr>
            <tr>
                <th scope="row"><fmt:message key="section2.revisionNote"/>:</th>
                <td>${docm.lastIteration.revisionNote}</td>
            </tr>
            </tbody>
        </table>
    </div>
</div>
</body>
</html>
<%@ page import="com.docdoku.core.common.*,com.docdoku.core.util.FileIO" contentType="text/html;charset=UTF-8" language="java" %>
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

        <script language="JavaScript">
            <!--
            var dlVisible=false;
            var lienVisible=false;
            var attributVisible=false;
            var documentVisible=false;
            var iterationVisible=false;

            function show(id,visible)
            {
                if(document.getElementById) {
                    document.getElementById(id).style.visibility = 'visible';
                } else if(document.layers) {
                    document.layers[id].visibility = 'show';
                } else if(document.all)
                    document.all(id).style.visibility = 'visible';

                visible = true;
            }

            function hide(id,visible)
            {
                if(document.getElementById)
                    document.getElementById(id).style.visibility = 'hidden';
                else if(document.layers)
                    document.layers[id].visibility = 'hide';
                else if(document.all)
                    document.all(id).style.visibility = 'hidden';

                visible=false;
            }

            function show_hide(id)
            {
                if (id == 'dl')
                {
                    hide('lien',lienVisible);
                    hide('attribut',attributVisible);
                    hide('document',documentVisible);
                    hide('iteration',iterationVisible);
                    if (!dlVisible)
                        show(id,dlVisible);
                    else
                        hide(id,dlVisible);
                }
                else if (id == 'lien')
                {
                    hide('dl',dlVisible);
                    hide('attribut',attributVisible);
                    hide('document',documentVisible);
                    hide('iteration',iterationVisible);
                    if (!lienVisible)
                        show(id,lienVisible);
                    else
                        hide(id,lienVisible);
                }
                else if (id == 'attribut')
                {
                    hide('dl',dlVisible);
                    hide('lien',lienVisible);
                    hide('document',documentVisible);
                    hide('iteration',iterationVisible);
                    if (!attributVisible)
                        show(id,attributVisible);
                    else
                        hide(id,attributVisible);
                }
                else if (id == 'document')
                {
                    hide('dl',dlVisible);
                    hide('lien',lienVisible);
                    hide('attribut',attributVisible);
                    hide('iteration',iterationVisible);
                    if (!documentVisible)
                        show(id,documentVisible);
                    else
                        hide(id,documentVisible);
                }
                else if (id == 'iteration')
                {
                    hide('dl',dlVisible);
                    hide('lien',lienVisible);
                    hide('attribut',attributVisible);
                    hide('document',documentVisible);
                    if (!iterationVisible)
                        show(id,iterationVisible);
                    else
                        hide(id,iterationVisible);
                }
            }

            function hide_all()
            {
                hide('dl',dlVisible);
                hide('lien',lienVisible);
                hide('attribut',attributVisible);
                hide('document',documentVisible);
                hide('iteration',iterationVisible);
            }
            // -->
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
                                <li><a class="brand" href="#" style="color: white">&nbsp;&nbsp;&nbsp;DocDoku<strong>PLM</strong>&nbsp;&nbsp;&nbsp;</a></li>
                                <li class="active"><a href="#" OnClick="hide_all();">${docm}</a></li>
                                <li><a href="#" OnClick="show_hide('document');" style="color: white"><fmt:message key="section1.title"/></a></li>
                                <li><a href="#" OnClick="show_hide('iteration');" style="color: white"><fmt:message key="section2.title"/></a></li>
                                <li><a href="#" OnClick="show_hide('attribut');" style="color: white"><fmt:message key="section2.attributs"/></a></li>
                                <li><a href="#" OnClick="show_hide('dl');" style="color: white"><fmt:message key="sidebar.title1"/></a></li>
                                <li><a href="#" OnClick="show_hide('lien');" style="color: white"><fmt:message key="sidebar.title2"/></a></li>
                            </ul>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div id="page">
            <div id="content">
                <div id="sidebar">
                    <div id="dl" style="visibility:hidden;" class="well">
                    <!--<div id="dl" class="well">-->
                        <center><h3><fmt:message key="sidebar.title1"/></h3></center>
                        <c:forEach var="item" varStatus="status" items="${docm.lastIteration.attachedFiles}">
                            <c:set scope="request" var="context" value="<%=request.getContextPath()%>" />
                            <c:set scope="request" var="filePath" value="${context}/files/${item.fullName}" />
                            <c:set scope="request" var="fileName" value="${item.name}" />
                            <c:set scope="request" var="index" value="${status.index}"/>
                            <jsp:useBean scope="request" type="java.lang.String" id="filePath"/>
                            <c:choose>
                                <c:when test="<%=FileIO.isDocFile(filePath)%>" >
                                    <%@include file="/WEB-INF/docPlayer.jspf"%>
                                </c:when>
                                <c:when test="<%=FileIO.isAVFile(filePath)%>" >
                                    <%@include file="/WEB-INF/audioVideoPlayer.jspf"%>
                                </c:when>
                                <c:when test="<%=FileIO.isImageFile(filePath)%>" >
                                    <%@include file="/WEB-INF/imagePlayer.jspf" %>
                                </c:when>
                                <c:otherwise>
                                    <table border="0">
                                        <tr>
                                            <td width="14px" height="14px">&nbsp;</td>
                                            <td><a href="<%=filePath%>">${item.name}coucou</a></td>
                                        </tr>
                                    </table>
                                </c:otherwise>
                            </c:choose>
                        </c:forEach>
                    </div>

                    <div id="lien" style="visibility:hidden;" class="well">
                        <center><h3 ><fmt:message key="sidebar.title2"/></h3></center>
                        <p>
                            <c:forEach var="item" items="${docm.lastIteration.linkedDocuments}">
                                <a href="<%=request.getContextPath()%>/documents/${item.toDocumentWorkspaceId}/${item.toDocumentDocumentMasterId}/${item.toDocumentDocumentMasterVersion}">${item.toDocumentDocumentMasterId}-${item.toDocumentDocumentMasterVersion}-${item.toDocumentIteration}</a><br/>
                            </c:forEach>
                        </p>
                    </div>

                    <!--<div id="attribut" style="visibility:hidden;" class="well">-->
                    <div id="attribut" class="well">
                        <center><h3 ><fmt:message key="section2.attributs"/></h3></center>
                        <p>
                            <c:forEach var="item" items="${docm.lastIteration.linkedDocuments}">
                                <a href="<%=request.getContextPath()%>/documents/${item.toDocumentWorkspaceId}/${item.toDocumentDocumentMasterId}/${item.toDocumentDocumentMasterVersion}">${item.toDocumentDocumentMasterId}-${item.toDocumentDocumentMasterVersion}-${item.toDocumentIteration}</a><br/>
                            </c:forEach>
                        </p>
                    </div>
                </div>
            </div>

            <div id="main">
                <div id="document" style="visibility:hidden;" class="well">
                    <center><h3><fmt:message key="section1.title"/></h3></center>
                    <table class="tab" border="0">
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
                        <tr>
                            <th scope="row"><fmt:message key="section1.description"/>:</th></tr>
                        <tr><td><textarea name="" rows="5" cols="35" readonly="readonly">${docm.description}</textarea></td>
                        </tr>
                    </table>
                </div>
            </div>

            <div id="iteration" style="visibility:hidden;" class="well"  style="width: 5000px">
                <center><h3><fmt:message key="section2.title"/></h3></center>
                <table class="tab2">
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
                </table>
            </div>
            <div id="footer">
                <p>Copyright 2006-2013 - <a href="http://www.docdoku.com">DocDoku</a></p>
            </div>
        </div>
    </body>
</html>
<%@ page import="com.docdoku.core.common.*,com.docdoku.core.util.FileIO" contentType="text/html;charset=UTF-8"
         language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">

<fmt:setBundle basename="com.docdoku.server.localization.part_resource"/>
<head>
    <meta content="text/html; charset=UTF-8" http-equiv="Content-Type"/>
    <title><fmt:message key="title"/></title>
    <link rel="Shortcut Icon" type="image/ico" href="<%=request.getContextPath()%>/images/favicon.ico"/>
    <link rel="stylesheet/less" type="text/css" href="<%=request.getContextPath()%>/less/part-permalink/style.less"/>
    <script type="text/javascript" src="<%=request.getContextPath()%>/js/lib/less-1.3.3.min.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/js/lib/jquery-1.8.2.min.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/js/lib/bootstrap-2.2.2.min.js"></script>
</head>

<body>
    <div id="header" class="navbar navbar-fixed-top">
        <div class="navbar-inner">
            <div class="nav-collapse collapse">
                <div class="container-fluid">
                    <img id="brand-logo" alt="docdoku_plm" src="/images/plm_logo2.png"/>
                    <a class="brand" href="/">DocDoku<strong>PLM</strong></a>
                </div>
            </div>
        </div>
    </div>
    <div id="page">

        <h3>${partRevision.partMaster.number}-${partRevision.version}</h3>

        <div id="content">

            <div class="tabs">

                <ul class="nav nav-tabs">
                    <li class="active"><a href="#tab-part-general" data-toggle="tab"><fmt:message key="tabs.general"/></a></li>
                    <li><a href="#tab-part-iteration" data-toggle="tab"><fmt:message key="tabs.iteration"/></a></li>
                    <li><a href="#tab-part-attributes" data-toggle="tab"><fmt:message key="tabs.attributes"/></a></li>
                    <li><a href="#tab-part-files" data-toggle="tab"><fmt:message key="tabs.nativeCADFile"/></a></li>
                    <li><a href="#tab-part-links" data-toggle="tab"><fmt:message key="tabs.links"/></a></li>
                </ul>

                <div class="tab-content form-horizontal">

                    <div id="tab-part-general" class="tab-pane active">
                        <table class="table table-striped table-condensed">
                            <tbody>
                                <tr>
                                    <th scope="row"><fmt:message key="general.author"/>:</th>
                                    <td>${partRevision.author.name}</td>
                                </tr>
                                <tr>
                                    <th scope="row"><fmt:message key="general.date"/>:</th>
                                    <td><fmt:formatDate type="both" value="${partRevision.creationDate}"/></td>
                                </tr>
                                <tr>
                                    <th scope="row"><fmt:message key="general.type"/>:</th>
                                    <td>${partRevision.partMaster.type}</td>
                                </tr>
                                <tr>
                                    <th scope="row"><fmt:message key="general.name"/>:</th>
                                    <td>${partRevision.partMaster.name}</td>
                                </tr>
                                <tr>
                                    <th scope="row"><fmt:message key="general.checkout_user"/>:</th>
                                    <td>${partRevision.checkOutUser.name}</td>
                                </tr>
                                <tr>
                                    <th scope="row"><fmt:message key="general.checkout_date"/>:</th>
                                    <td><fmt:formatDate type="both" value="${partRevision.checkOutDate}"/></td>
                                </tr>
                                <tr>
                                    <th scope="row"><fmt:message key="general.lifeCycleState"/>:</th>
                                    <td>${partRevision.lifeCycleState}</td>
                                </tr>
                                <tr>
                                    <th scope="row"><fmt:message key="general.description"/>:</th>
                                    <td>${partRevision.description}</td>
                                </tr>
                            </tbody>
                        </table>
                    </div>

                    <div id="tab-part-iteration" class="tab-pane">
                        <table class="table table-striped table-condensed">
                            <tbody>
                                <tr>
                                    <th scope="row"><fmt:message key="iteration.number"/>:</th>
                                    <td>${partRevision.lastIteration.iteration}</td>
                                </tr>
                                <tr>
                                    <th scope="row"><fmt:message key="iteration.author"/>:</th>
                                    <td>${partRevision.lastIteration.author.name}</td>
                                </tr>
                                <tr>
                                    <th scope="row"><fmt:message key="iteration.date"/>:</th>
                                    <td><fmt:formatDate type="both" value="${partRevision.lastIteration.creationDate}"/></td>
                                </tr>
                                <tr>
                                    <th scope="row"><fmt:message key="iteration.iterationNote"/>:</th>
                                    <td>${partRevision.lastIteration.iterationNote}</td>
                                </tr>
                            </tbody>
                        </table>
                    </div>

                    <div id="tab-part-attributes" class="tab-pane">
                        <c:if test="${attr.size()!=0}">
                            <table class="table table-striped table-condensed">
                                <thead>
                                <tr>
                                    <th><fmt:message key="attributes.name"/></th>
                                    <th><fmt:message key="attributes.value"/></th>
                                </tr>
                                </thead>
                                <c:forEach var="item" items="${attr}">
                                    <tbody>
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
                                            <td><a target="blank" href="${item.value}">${item.value}</a></td>
                                        </c:if>

                                        <c:if test="${item.value.class!='class java.lang.Boolean' && item.value.class!='class java.util.Date' && item.class!='class com.docdoku.core.meta.InstanceURLAttribute'}">
                                            <td>${item.value}</td>
                                        </c:if>
                                    </tr>
                                    </tbody>
                                </c:forEach>
                            </table>
                        </c:if>
                    </div>

                    <div id="tab-part-files" class="tab-pane files">
                        <c:if test="${partRevision.lastIteration.nativeCADFile!=null}">
                            <a href="${context}/files/${partRevision.lastIteration.nativeCADFile.fullName}">${partRevision.lastIteration.nativeCADFile.name}</a>
                        </c:if>
                    </div>

                    <div id="tab-part-links" class="tab-pane">
                        <c:if test="${partRevision.lastIteration.linkedDocuments.size()!=0}">
                            <ul>
                            <c:forEach var="item" items="${partRevision.lastIteration.linkedDocuments}">
                                <li><a href="<%=request.getContextPath()%>/documents/${item.targetDocumentWorkspaceId}/${item.targetDocumentDocumentMasterId}/${item.targetDocumentDocumentMasterVersion}">${item.targetDocumentDocumentMasterId}-${item.targetDocumentDocumentMasterVersion}-${item.targetDocumentIteration}</a></li>
                            </c:forEach>
                            </ul>
                        </c:if>
                    </div>

                </div>
            </div>

        </div>
    </div>
</body>
</html>
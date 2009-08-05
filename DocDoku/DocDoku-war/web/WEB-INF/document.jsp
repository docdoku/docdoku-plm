<%@ page import="com.docdoku.core.entities.*,java.io.File,java.util.Set,com.docdoku.server.http.FileConverter,com.docdoku.server.http.PlayerProperties,com.docdoku.core.util.FileIO,com.docdoku.server.http.FileAVT" contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
    <fmt:setBundle basename="com.docdoku.server.localization.document_resource"/>
    <head>
        <meta content="text/html; charset=UTF-8" http-equiv="Content-Type"/>
        <title><fmt:message key="title"/></title>
        <link rel="Shortcut Icon" type="image/ico" href="<%=request.getContextPath()%>/images/favicon.ico"/>
        <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/css/docdoku.css" media="screen"/>
        <script src="<%=request.getContextPath()%>/js/switchPlayer.js"></script>

    </head>

    <body>
        <div id="page">
            <div id="content">

                <div id="sidebar">
                    <div id="wrapper">
                    <h3><fmt:message key="sidebar.title1"/></h3>
                    <p>
       <%

        String filePath = null;
        String fileName = null;
       
        int i = 0;
       %>

    <c:forEach var="item" items="${mdoc.lastIteration.attachedFiles}">
        <%
        BinaryResource file = (BinaryResource) pageContext.getAttribute("item");
        filePath = request.getContextPath()+"/files/" + file.getFullName();
        fileName = file.getName();
        String extension = FileIO.getExtension(filePath);
        if(extension != null){
        
        String id = "player" + i;     
        String idObject = id + "3";
        String type = "application/x-shockwave-flash";    
        FileAVT fileAVT= new FileAVT(filePath);
        request.setAttribute("fileName", fileName);
        request.setAttribute("filePath", filePath);
        request.setAttribute("type", type);
        request.setAttribute("extension", extension);
        request.setAttribute("context", request.getContextPath());
        request.setAttribute("idObject", idObject);
        request.setAttribute("id", id);
        request.setAttribute("i", i);      
          if (fileAVT.getTypeFile().equals("textFile")) {
          %>

                      <%@include file="/WEB-INF/swfPlayer.jspf"%>

          <%
          } else if (fileAVT.getTypeFile().equals("audioVideo")) {
         %>

                       <%@include file="/WEB-INF/audioVideoPlayer.jspf"%>

          <% }else if (fileAVT.getTypeFile().equals("image")) {
          %>
                          
                          <%@include file="/WEB-INF/imagePlayer.jspf" %>
          <%
            }else{

          %>
            <table border="0" style="border-color:green">
                <tr>
                    <td width="8px" height="8px">&nbsp;</td>
                    <td align="left" width="180px"><a href="<%=filePath%>"><%=fileName%></a></td>
                </tr>
            </table>
             
          <%
                }
        }else{  %>

        <table border="0" style="border-color:green">
                <tr>
                    <td width="8px" height="8px">&nbsp;</td>
                    <td align="left" width="180px"><a href="<%=filePath%>"><%=fileName%></a></td>
                </tr>
         </table>
         
        <%
        }
            i++;
         %>

                    </c:forEach>
                
                <h3><fmt:message key="sidebar.title2"/></h3>
                <p>
                    <c:forEach var="item" items="${mdoc.lastIteration.linkedDocuments}">
                        <a href="<%=request.getContextPath()%>/documents/${item.toDocumentWorkspaceId}/${item.toDocumentMasterDocumentId}/${item.toDocumentMasterDocumentVersion}">${item.toDocumentMasterDocumentId}-${item.toDocumentMasterDocumentVersion}-${item.toDocumentIteration}</a><br/>
                    </c:forEach>
                </p>

                </div>
                </div>
            </div>

            <div id="main">
                <h2>${mdoc}</h2>

                <h3><fmt:message key="section1.title"/></h3>

                <table>
                    <tr>
                        <th scope="row"><fmt:message key="section1.author"/>:</th>
                        <td>${mdoc.author.name}</td>
                    </tr>
                    <tr>
                        <th scope="row"><fmt:message key="section1.date"/>:</th>
                        <td><fmt:formatDate value="${mdoc.creationDate}"/></td>
                    </tr>
                    <tr>
                        <th scope="row"><fmt:message key="section1.titledoc"/>:</th>
                        <td>${mdoc.title}</td>
                    </tr>
                    <tr>
                        <th scope="row"><fmt:message key="section1.checkout_user"/>:</th>
                        <td>${mdoc.checkOutUser.name}</td>
                    </tr>
                    <tr>
                        <th scope="row"><fmt:message key="section1.checkout_date"/>:</th>
                        <td><fmt:formatDate value="${mdoc.checkOutDate}"/></td>
                    </tr>
                    <tr>
                        <th scope="row"><fmt:message key="section1.state"/>:</th>
                        <td>${mdoc.lifeCycleState}</td>
                    </tr>
                    <tr>
                        <th scope="row"><fmt:message key="section1.tag"/>:</th>
                        <td>${mdoc.tags}</td>
                    </tr>
                    <tr>
                        <th scope="row"><fmt:message key="section1.description"/>:</th>
                        <td><textarea name="" rows="5" cols="35" readonly="readonly">${mdoc.description}</textarea></td>
                    </tr>
                </table>
            </div>

            <h3><fmt:message key="section2.title"/></h3>

            <table>
                <tr>
                    <th scope="row"><fmt:message key="section2.iteration"/>:</th>
                    <td>${mdoc.lastIteration.iteration}</td>
                </tr>
                <tr>
                    <th scope="row"><fmt:message key="section2.author"/>:</th>
                    <td>${mdoc.lastIteration.author.name}</td>
                </tr>
                <tr>
                    <th scope="row"><fmt:message key="section2.date"/>:</th>
                    <td><fmt:formatDate value="${mdoc.lastIteration.creationDate}"/></td>
                </tr>
                <tr>
                    <th scope="row"><fmt:message key="section2.revisionNote"/>:</th>
                    <td>${mdoc.lastIteration.revisionNote}</td>
                </tr>
            </table>
        </div>
    </body>
</html>
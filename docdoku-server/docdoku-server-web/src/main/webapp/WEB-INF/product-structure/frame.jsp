<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html>
<fmt:setBundle basename="com.docdoku.server.localization.explorer_resource"/>
<head>
    <meta content="text/html; charset=UTF-8" http-equiv="Content-Type"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>v 0.2 - <fmt:message key="title"/></title>

    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/css/font-awesome.css"/>
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/css/jquery.treeview.css"/>
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/css/product-structure.css"/>
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/css/navigation-controls.css"/>
    <link rel="stylesheet/less" type="text/css" href="<%=request.getContextPath()%>/less/product-structure/style.less">

    <script src="<%=request.getContextPath()%>/js/lib/jquery-1.7.1.min.js"></script>

    <script type="text/javascript">

        var APP_CONFIG = {
            workspaceId:"${workspaceID}",
            productId:"${productID}",
            login:"${login}"
        };

        $(document).ready(function() {
            populateProductsMenu();

            _.templateSettings = {
                evaluate : /\{\[([\s\S]+?)\]\}/g,
                interpolate : /\{\{([\s\S]+?)\}\}/g
            };

        });

    </script>

    <script type="text/x-underscore-template" id="part_metadata_template">
        <h1><i class="icon-cog"></i>{{getNumber}}</h1>
        <button type="button" class="close" id="part_metadata_close_button">×</button>
        <div class="row">
            <div class="span7">
                <div class="span6">
                    <span class="part_metadata_key">Version :</span>
                    <span class="part_metadata_value" id="part_metadata_version">{{getVersion}}</span>
                </div>
                <div class="span6">
                    <span class="part_metadata_key">Iteration :</span>
                    <span class="part_metadata_value" id="part_metadata_iteration">{{getIteration}}</span>
                </div>
            </div>
            <div class="span4">
                <div>
                    <span class="part_metadata_key">Description :</span>
                    <span class="part_metadata_value" id="part_metadata_description">{{getDescription}}</span>
                </div>
            </div>
        </div>
    </script>


</head>
<body>

<div id="workspace">

    <div id="frameContainer"></div>        

    <div class="modal hide fade" id="issueModal">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal">×</button>
            <h3 id="issueTitle"></h3>
        </div>
        <div class="modal-body">
            <p><b>Author:</b> <span id="issueAuthor"></span></p>
            <p><b>Date:</b> <span id="issueDate"></span></p>
            <p><b>Description:</b> <span id="issueDesc"></span></p>
            <p><b>Comment:</b> <span id="issueComment"></span></p>
            <p><b>Zone:</b> <span id="issueZone"></span></p>
            <p><b>Responsible:</b> <span id="issueResponsible"></span></p>
            <p><b>Criticity:</b> <span id="issueCriticity"></span></p>
            <p><b>MSN:</b> <span id="issueMsn"></span></p>
        </div>
        <div class="modal-footer">
            <a href="#" class="btn" data-dismiss="modal">Close</a>
        </div>
    </div>

</div>


<script src="<%=request.getContextPath()%>/js/lib/less-1.2.1.min.js"></script>

<script src="<%=request.getContextPath()%>/js/lib/date.format.js"></script>
<script src="<%=request.getContextPath()%>/js/lib/underscore-1.3.1.min.js"></script>
<script src="<%=request.getContextPath()%>/js/lib/mustache-0.5.0-dev.js"></script>
<script src="<%=request.getContextPath()%>/js/lib/kumo.js"></script>
<script src="<%=request.getContextPath()%>/js/lib/jquery-ui-1.8.19.min.js"></script>
<script src="<%=request.getContextPath()%>/js/lib/jquery.maskedinput-1.3.js"></script>
<script src="<%=request.getContextPath()%>/js/lib/backbone-0.9.2.min.js"></script>
<script src="<%=request.getContextPath()%>/js/lib/bootstrap-2.0.2.min.js"></script>


<script src="<%=request.getContextPath()%>/js/product-structure/models/part.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/product-structure/models/instance.js"></script>
<script src="<%=request.getContextPath()%>/js/product-structure/collections/partCollection.js"></script>
<script src="<%=request.getContextPath()%>/js/product-structure/views/part_node_view.js"></script>
<script src="<%=request.getContextPath()%>/js/product-structure/views/part_item_view.js"></script>
<script src="<%=request.getContextPath()%>/js/product-structure/views/part_metadata_view.js"></script>
<script src="<%=request.getContextPath()%>/js/product-structure/views/bom_item_view.js"></script>
<script src="<%=request.getContextPath()%>/js/product-structure/app.js"></script>

<script type="text/javascript" src="<%=request.getContextPath()%>/js/lib/jquery.treeview.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/lib/visualization/Three.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/lib/visualization/Stats.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/lib/visualization/threex.domevent.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/lib/visualization/VisualizationUtils.js"></script>

<script type="text/javascript" src="<%=request.getContextPath()%>/js/product-structure/ControlManager.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/product-structure/PinManager.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/product-structure/TrackballControlsCustom.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/product-structure/SceneManager.js"></script>

</body>
</html>
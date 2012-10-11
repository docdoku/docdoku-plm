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

    <link rel="Shortcut Icon" type="image/ico" href="<%=request.getContextPath()%>/images/favicon.ico"/>
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/css/font-awesome.css"/>
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/css/jquery.treeview.css"/>
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/css/product-structure.css"/>
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/css/navigation-controls.css"/>
    <link rel="stylesheet/less" type="text/css" href="<%=request.getContextPath()%>/less/product-structure/style.less">

    <script src="<%=request.getContextPath()%>/js/lib/jquery-1.7.1.min.js"></script>

    <script src="<%=request.getContextPath()%>/js/lib/require/1.0.8/require.min.js"></script>

    <script src="<%=request.getContextPath()%>/js/lib/less-1.2.1.min.js"></script>

    <script src="<%=request.getContextPath()%>/js/lib/date.format.js"></script>
    <script src="<%=request.getContextPath()%>/js/lib/underscore-1.4.2.min.js"></script>
    <script src="<%=request.getContextPath()%>/js/lib/mustache-0.5.0-dev.js"></script>
    <script src="<%=request.getContextPath()%>/js/lib/kumo.js"></script>
    <script src="<%=request.getContextPath()%>/js/lib/jquery-ui-1.8.19.min.js"></script>
    <script src="<%=request.getContextPath()%>/js/lib/jquery.maskedinput-1.3.js"></script>
    <script src="<%=request.getContextPath()%>/js/lib/backbone-0.9.2.min.js"></script>
    <script src="<%=request.getContextPath()%>/js/lib/bootstrap-2.0.2.min.js"></script>

    <script type="text/javascript" src="<%=request.getContextPath()%>/js/product-structure/models/instance.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/js/product-structure/models/levelGeometry.js"></script>

    <script type="text/javascript" src="<%=request.getContextPath()%>/js/lib/jquery.treeview.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/js/lib/modernizr.custom.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/js/lib/visualization/Three.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/js/lib/visualization/Stats.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/js/lib/visualization/threex.domevent.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/js/lib/visualization/threex.windowresize.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/js/lib/visualization/VisualizationUtils.js"></script>

    <script type="text/javascript" src="<%=request.getContextPath()%>/js/product-structure/ControlManager.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/js/product-structure/TrackballControlsCustom.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/js/product-structure/SceneManager.js"></script>

    <script type="text/javascript" src="<%=request.getContextPath()%>/js/product-structure/ControlManager.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/js/product-structure/TrackballControlsCustom.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/js/product-structure/SceneManager.js"></script>

    <script type="text/javascript">

        var APP_CONFIG = {
            workspaceId:"${workspaceID}",
            productId:"${productID}",
            login:"${login}"
        };

        var isIpad = navigator.userAgent.indexOf("iPad") != -1;

        $(document).ready(function() {

            require.config({
                baseUrl: "${request.contextPath}/js/product-structure",
                paths: {
                    "require": "../lib/require/1.0.8/require.min",
                    "text": "../lib/require/1.0.8/text.min"
                },
                locale: "<%=request.getLocale()%>"
            });


            require(["frameApp"], function(FrameView) {
                new FrameView();
            });

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

<div id="frameWorkspace">

    <div id="frameContainer"></div>        

    <div class="modal hide fade" id="markerModal">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal">×</button>
            <h3 id="markerTitle"></h3>
        </div>
        <div class="modal-body">
            <p><b>Description:</b> <span id="markerDesc"></span></p>
        </div>
        <div class="modal-footer">
            <a href="#" class="btn" data-dismiss="modal">Close</a>
        </div>
    </div>

</div>

</body>
</html>
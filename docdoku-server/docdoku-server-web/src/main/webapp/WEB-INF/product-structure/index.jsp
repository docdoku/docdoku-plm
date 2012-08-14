<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html>
<fmt:setBundle basename="com.docdoku.server.localization.explorer_resource"/>
<head>
    <meta content="text/html; charset=UTF-8" http-equiv="Content-Type"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title><fmt:message key="title"/></title>

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
        });

    </script>

    <script type="text/x-underscore-template" id="part_metadata_template">
        <h1><i class="icon-cog"></i>{{getNumber}}</h1>
        <button type="button" class="close" id="part_metadata_close_button">×</button>
        <div class="row">
            <div class="span7">
                <div class="span12">
                    <span class="part_metadata_key">Version :</span>
                    <span class="part_metadata_value" id="part_metadata_version">{{getVersion}}</span>
                </div>
                <div class="span12">
                    <span class="part_metadata_key">Iteration :</span>
                    <span class="part_metadata_value" id="part_metadata_iteration">{{getIteration}}</span>
                </div>
            </div>
            <div class="span4">
                <div class="span12">
                    <span class="part_metadata_key">Description :</span>
                    <span class="part_metadata_value" id="part_metadata_description">{{getDescription}}</span>
                </div>
            </div>
        </div>
    </script>


</head>
<body>
    <%@ include file="/WEB-INF/explorer_header.jspf" %>
    <div class="container-fluid" id="workspace">
        <div class="row-fluid">
            <div class="span3">
                <div class="well sidebar-nav">

                    <div id="nav_list_action_bar">

                        <div id="nav_list_controls">
                                <!--<a class="btn header_btn-custom" id="nav_list_collapse" href="#">
                                   <i class="icon-resize-small"></i>
                               </a>
                               <a class="btn header_btn-custom" id="nav_list_expand" href="#">
                                   <i class="icon-resize-full"></i>
                               </a>-->
                            <div id="search_control_container">
                                <input type="search"/>
                                <a class="btn header_btn-custom" id="nav_list_search_button" href="#">
                                    <i class="icon-search"></i>
                                </a>
                            </div>
                        </div>
                    </div>

                    <nav class="nav nav-list treeview" id="product_nav_list">

                    </nav>

                    <select id="context_selector_list">
                        <option>contexte</option>
                        <option>2</option>
                        <option>3</option>
                        <option>4</option>
                        <option>5</option>
                    </select>

                </div>
            </div>

            <div class="span9">

                <div id="content_header_bar" class="span10">
                    <div class="btn-group" data-toggle="buttons-radio">
                        <button class="btn header_btn-custom active" id="scene_view_btn"><i class="icon-eye-open"></i></button>
                        <button class="btn header_btn-custom" id="metadata_view_btn"><i class="icon-info-sign"></i></button>
                        <button class="btn header_btn-custom" id="bom_view_btn"><i class="icon-list"></i></button>
                    </div>
                </div>

                <div id="content">

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
                        </div>
                        <div class="modal-footer">
                            <a href="#" class="btn" data-dismiss="modal">Close</a>
                        </div>
                    </div>

                    <div id="container" class="span10"></div>

                    <div id="navigationMenu" class="span1">
                        <div id="moveSpace">
                            <a href="#" class="moveBtnSide moveBtnTop"><i class="icon-caret-up icon-navigation"></i></a>
                            <a href="#" class="moveBtnSide moveBtnLeft"><i class="icon-caret-left icon-navigation"></i></a>
                            <a href="#" class="moveBtnSide moveBtnRight"><i
                                    class="icon-caret-right icon-navigation"></i></a>
                            <a href="#" class="moveBtnSide moveBtnBottom"><i
                                    class="icon-caret-down icon-navigation"></i></a>
                            <a href="#" class="moveBtn moveBtnCenter"><i class="icon-undo icon-navigation"></i></a>
                        </div>

                        <div id="moveMap">
                            <a href="#" class="moveBtnSide moveBtnTop"><i class="icon-caret-up icon-navigation"></i></a>
                            <a href="#" class="moveBtnSide moveBtnLeft"><i class="icon-caret-left icon-navigation"></i></a>
                            <a href="#" class="moveBtnSide moveBtnRight"><i
                                    class="icon-caret-right icon-navigation"></i></a>
                            <a href="#" class="moveBtnSide moveBtnBottom"><i
                                    class="icon-caret-down icon-navigation"></i></a>
                            <a href="#" class="moveBtn moveBtnCenter"><i class="icon-fullscreen icon-navigation"></i></a>
                        </div>

                        <div id="managePin">
                            <a href="#" class="moveBtnSide moveBtnLeft"><i class="icon-minus icon-pin-manager"></i></a>
                            <a href="#" class="moveBtnSide moveBtnRight"><i class="icon-plus icon-pin-manager"></i></a>
                            <a href="#" class="moveBtn moveBtnCenter"><i id="pinState" class="icon-pin icon-pin-full icon-navigation"></i></a>
                        </div>
                    </div>

                    <div id="zoomContainer" class="span10">
                        <div id="zoomBar">
                            <input id="zoomRange" type="range" min="0" max="100" value="50" step="1"/>
                        </div>
                    </div>

                    <div id="content_footer_bar">
                        <h1></h1>
                    </div>

                </div>

                <div id="part_metadata_container">

                </div>


                <div id="bom_table_container">
                    <table id="bom_table" class="table table-striped">
                        <thead>
                            <tr>
                                <th>Part Number</th>
                                <th>Name</th>
                                <th>Revision</th>
                                <th>Iteration</th>
                                <th>Quantity</th>
                            </tr>
                        </thead>
                        <tbody>
                        </tbody>
                    </table>
                </div>

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
<script type="text/javascript" src="<%=request.getContextPath()%>/js/product-structure/models/levelGeometry.js"></script>
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
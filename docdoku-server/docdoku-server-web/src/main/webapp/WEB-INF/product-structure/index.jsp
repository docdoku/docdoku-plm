<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html>
<fmt:setBundle basename="com.docdoku.server.localization.explorer_resource"/>
<head>
    <meta content="text/html; charset=UTF-8" http-equiv="Content-Type"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="chrome=1"/>
    <title><fmt:message key="title"/></title>

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


            require(["app"], function(AppView) {
                new AppView();
            });

            populateProductsMenu();

        });

    </script>

    <script type="text/x-underscore-template" id="part_metadata_template">
        <h1><i class="icon-cog"></i>{{getNumber}}</h1>
        <button type="button" class="close" id="part_metadata_close_button">×</button>
        <div class="row">
            <div class="span5">
                <div class="span4">
                    <span class="part_metadata_key">Name :</span>
                    <span class="part_metadata_value">{{getName}}</span>
                </div>
                <div class="span4">
                    <span class="part_metadata_key">Version :</span>
                    <span class="part_metadata_value">{{getVersion}}</span>
                </div>
                <div class="span4">
                    <span class="part_metadata_key">Iteration :</span>
                    <span class="part_metadata_value">{{getIteration}}</span>
                </div>
            </div>
            <div class="span4">
                <div class="span4">
                    <span class="part_metadata_key">Description :</span>
                    <span class="part_metadata_value">{{getDescription}}</span>
                </div>
                <div class="span4">
                    <span class="part_metadata_key">Author :</span>
                    <span class="part_metadata_value" id="part_metadata_author"><a>{{getAuthor}} <i class="icon-facetime-video"></i></a></span>
                </div>
            </div>
        </div>
    </script>


</head>
<body>
<%@ include file="/WEB-INF/header.jspf" %>

<div id="workspace">

    <div id="leftPanel">

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

                <select id="ata_selector">
                    <option val="">All ATA Chapters</option>
                    <optgroup label="AIRCRAFT GENERAL">
                        <option val="">ATA 05 - TIME LIMITS/MAINTENANCE CHECKS</option>
                        <option val="">ATA 06 - DIMENSIONS AND AREAS</option>
                        <option val="">ATA 07 - LIFTING AND SHORING</option>
                        <option val="">ATA 08 - LEVELING AND WEIGHING</option>
                        <option val="">ATA 09 - TOWING AND TAXIING</option>
                        <option val="">ATA 10 - PARKING, MOORING, STORAGE AND RETURN TO SERVICE</option>
                        <option val="">ATA 11 - PLACARDS AND MARKINGS</option>
                        <option val="">ATA 12 - SERVICING - ROUTINE MAINTENANCE</option>
                    </optgroup>
                    <optgroup label="AIRFRAME SYSTEMS">
                        <option val="">ATA 20 - STANDARD PRACTICES – AIRFRAME</option>
                        <option val="">ATA 21 - AIR CONDITIONING AND PRESSURIZATION</option>
                        <option val="">ATA 22 - AUTOFLIGHT</option>
                        <option val="">ATA 23 - COMMUNICATIONS</option>
                        <option val="">ATA 24 - ELECTRICAL POWER</option>
                        <option val="">ATA 25 - EQUIPMENT/FURNISHINGS</option>
                        <option val="">ATA 26 - FIRE PROTECTION</option>
                        <option val="">ATA 27 - FLIGHT CONTROLS</option>
                        <option val="">ATA 28 - FUEL</option>
                        <option val="">ATA 29 - HYDRAULIC POWER</option>
                        <option val="">ATA 30 - ICE AND RAIN PROTECTION</option>
                        <option val="">ATA 31 - INDICATING / RECORDING SYSTEM</option>
                        <option val="">ATA 32 - LANDING GEAR</option>
                        <option val="">ATA 33 - LIGHTS</option>
                        <option val="">ATA 34 - NAVIGATION</option>
                        <option val="">ATA 35 - OXYGEN</option>
                        <option val="">ATA 36 - PNEUMATIC</option>
                        <option val="">ATA 37 - VACUUM</option>
                        <option val="">ATA 38 - WATER/WASTE</option>
                        <option val="">ATA 44 - CABIN SYSTEMS</option>
                        <option val="">ATA 45 - DIAGNOSTIC AND MAINTENANCE SYSTEM</option>
                        <option val="">ATA 46 - INFORMATION SYSTEMS</option>
                        <option val="">ATA 47 - NITROGEN GENERATION SYSTEM</option>
                        <option val="">ATA 48 - IN FLIGHT FUEL DISPENSING</option>
                        <option val="">ATA 49 - AIRBORNE AUXILIARY POWER</option>
                        <option val="">ATA 50 - CARGO AND ACCESSORY COMPARTMENTS</option>
                    </optgroup>
                    <optgroup label="STRUCTURE">
                        <option val="">ATA 51 - STANDARD PRACTICES AND STRUCTURES - GENERAL</option>
                        <option val="">ATA 52 - DOORS</option>
                        <option val="">ATA 53 - FUSELAGE</option>
                        <option val="">ATA 54 - NACELLES/PYLONS</option>
                        <option val="">ATA 55 - STABILIZERS</option>
                        <option val="">ATA 56 - WINDOWS</option>
                        <option val="">ATA 57 - WINGS</option>
                    </optgroup>
                    <optgroup label="POWER PLANT">
                        <option val="">ATA 61 - PROPELLERS</option>
                        <option val="">ATA 71 - POWER PLANT</option>
                        <option val="">ATA 72 - ENGINE - RECIPROCATING</option>
                        <option val="">ATA 73 - ENGINE - FUEL AND CONTROL</option>
                        <option val="">ATA 74 - IGNITION</option>
                        <option val="">ATA 75 - BLEED AIR</option>
                        <option val="">ATA 76 - ENGINE CONTROLS</option>
                        <option val="">ATA 77 - ENGINE INDICATING</option>
                        <option val="">ATA 78 - EXHAUST</option>
                        <option val="">ATA 79 - OIL</option>
                        <option val="">ATA 80 - STARTING</option>
                    </optgroup>
                </select>

            </div>
        </div>

        <div id="product_nav_list_container">
            <nav class="nav nav-list treeview" id="product_nav_list">

            </nav>
        </div>

        <div id="config_spec_container">
            <span>Config Spec</span>
            <select id="context_selector_list">
                <option selected="">Latest</option>
            </select>
        </div>

    </div>

    <div id="mainPanel">

        <div id="top_controls_container">
            <div class="btn-group" data-toggle="buttons-radio" id="top_buttons">
                <button class="btn header_btn-custom active" id="scene_view_btn"><i class="icon-eye-open"></i></button>
                <button class="btn header_btn-custom" id="metadata_view_btn"><i class="icon-info-sign"></i></button>
                <button class="btn header_btn-custom" id="bom_view_btn"><i class="icon-list"></i></button>
            </div>
            <button class="btn header_btn-custom" id="export_scene_btn"><i class="icon-upload-alt"></i> <b>Export</b></button>
        </div>

        <div id="center_container">
            <div id="side_controls_container">
                <div id="navigationMenu">
                    <div id="moveSpace">
                        <a href="#" class="moveBtnSide moveBtnTop"><i class="icon-caret-up icon-navigation"></i></a>
                        <a href="#" class="moveBtnSide moveBtnLeft"><i class="icon-caret-left icon-navigation"></i></a>
                        <a href="#" class="moveBtnSide moveBtnRight"><i class="icon-caret-right icon-navigation"></i></a>
                        <a href="#" class="moveBtnSide moveBtnBottom"><i class="icon-caret-down icon-navigation"></i></a>
                        <a href="#" class="moveBtn moveBtnCenter"><i class="icon-undo icon-navigation"></i></a>
                    </div>

                    <div id="moveMap">
                        <a href="#" class="moveBtnSide moveBtnTop"><i class="icon-caret-up icon-navigation"></i></a>
                        <a href="#" class="moveBtnSide moveBtnLeft"><i class="icon-caret-left icon-navigation"></i></a>
                        <a href="#" class="moveBtnSide moveBtnRight"><i class="icon-caret-right icon-navigation"></i></a>
                        <a href="#" class="moveBtnSide moveBtnBottom"><i class="icon-caret-down icon-navigation"></i></a>
                        <a href="#" class="moveBtn moveBtnCenter"><i class="icon-fullscreen icon-navigation"></i></a>
                    </div>

                    <div id="manageMarker">
                        <a href="#" class="moveBtnSide moveBtnLeft"><i class="icon-minus icon-marker-manager"></i></a>
                        <a href="#" class="moveBtnSide moveBtnRight"><i class="icon-plus icon-marker-manager"></i></a>
                        <a href="#" class="moveBtn moveBtnCenter"><i id="markerState" class="icon-marker icon-marker-full icon-navigation"></i></a>
                    </div>

                    <div id="layer-wrapper"><ul id="layer-header"></ul><nav><ul></ul></nav></div>

                </div>
            </div>
            <div id="scene_container">
                <div id="container"></div>
            </div>
        </div>

        <div id="bottom_controls_container">
            <div id="zoomBar">
                <input id="zoomRange" type="range" min="0" max="100" value="50" step="1" />
            </div>
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

        <div id="part_metadata_container">

        </div>

    </div>

    <div class="modal hide fade" id="markerModal">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal">×</button>
            <h3 id="markerTitle"></h3>
        </div>
        <div class="modal-body">
            <p><b>Description :</b> <span id="markerDesc"></span></p>
        </div>
        <div class="modal-footer">
            <a href="#" class="btn" data-dismiss="modal">Close</a>
            <a href="#" class="btn btn-danger">Delete marker</a>
        </div>
    </div>

    <div class="modal hide fade" id="creationMarkersModal">
        <div class="modal-header">
            <input type="text" placeholder="Name" />
        </div>
        <div class="modal-body">
            <textarea type="text" placeholder="Description"></textarea>
        </div>
        <div class="modal-footer">
            <a href="#" class="btn cancel">Cancel</a>
            <a href="#" class="btn btn-primary">Save marker</a>
        </div>
    </div>

    <div class="modal hide fade" id="exportSceneModal">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal">×</button>
            <h3 id="exportSceneTitle">Embed Scene</h3>
        </div>
        <div class="modal-body">
            <textarea type="text" placeholder="">Lorem ipsum dolor sit alamet</textarea>
        </div>
    </div>

    <div class="modal hide fade" id="webRtcModal">
        <div class="modal-header">
            <h3></h3>
            <button type="button" class="btn btn-primary" data-dismiss="modal" id="hangup_webrtc_call_btn">hangup</button>
        </div>
        <div class="modal-body">
        </div>
    </div>

</div>

</body>
</html>
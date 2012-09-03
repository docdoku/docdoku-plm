define([
    "collections/layer_collection",
    "models/layer"
], function (
    LayerCollection,
    Layer
) {

    var STATE = { FULL : 0, TRANSPARENT : 1, HIDDEN : 2};
    var mouse = new THREE.Vector2(),
        offset = new THREE.Vector3(),
        INTERSECTED, SELECTED,
        projector = new THREE.Projector(),
        domEvent;

    var LayerManager = function( scene, camera, renderer, controls, container ) {
        this.scene = scene;
        this.camera = camera;
        this.meshs = [];
        this.state = STATE.FULL;
        this.renderer = renderer;
        this.controls = controls;
        this.container = container;
        this.markerStateControl = $('#pinState');
        this.layersCollection = new LayerCollection();

        this.markerMaterial = new THREE.MeshLambertMaterial({
            color: 0xFF0000,
            opacity: 1,
            transparent: true
        });

        domEvent = new THREEx.DomEvent(camera, container);
    };

    LayerManager.prototype = {

        addMeshFromMarker: function(marker) {
            // set up the sphere vars
            var radius = 50,
                segments = 16,
                rings = 16;

            // create a new mesh with
            // sphere geometry - we will cover
            // the sphereMaterial next!
            var markerMesh = new THREE.Mesh(
                new THREE.SphereGeometry(
                    radius,
                    segments,
                    rings
                ),
                this.markerMaterial
            );

            markerMesh.position.set(marker.getX(), marker.getY(), marker.getZ());

            var self = this;

            domEvent.bind(markerMesh, 'click', function(){
                if (self.state != STATE.HIDDEN) {
                    self.showPopup(marker);
                }
            });

            // add the sphere to the scene
            this.scene.add( markerMesh );

            //save the mesh for further reuse
            this.meshs[marker.cid] = markerMesh;

            markerMesh.geometry.dynamic = true;
        },

        removeMeshFromMarker: function(marker) {
            this._removeMesh(marker.cid);
        },

        removeAllMeshs: function() {
            for (var cid in this.meshs) {
                this._removeMesh(cid);
            }
        },

        _removeMesh: function(cid) {
            domEvent.unbind(this.meshs[cid], 'click');
            this.scene.remove(this.meshs[cid]);
            delete this.meshs[cid];
        },

        createLayer: function(name) {
            var layer = new Layer({
                name : name
            });
            this.layersCollection.add(layer);
            return layer;
        },

        removeLayer: function(layer) {
            this.layersCollection.remove(layer);
        },

        rescaleMarkers: function(value) {
            _.each(this.meshs, function(markerMesh) {
                markerMesh.scale.x += value;
                markerMesh.scale.y += value;
                markerMesh.scale.z += value;
            });
        },

        changeMarkerState: function() {
            switch(this.state) {
                case STATE.FULL  :
                    this.markerStateControl.removeClass('icon-pin-full').addClass('icon-pin-empty');
                    this.changeOpacityOnMarker(0.4);
                    this.state = STATE.TRANSPARENT;
                    break;
                case STATE.TRANSPARENT :
                    this.markerStateControl.removeClass('icon-pin-empty').addClass('icon-pin-dotted');
                    this.changeOpacityOnMarker(0);
                    this.state = STATE.HIDDEN;
                    break;
                case STATE.HIDDEN:
                    this.markerStateControl.removeClass('icon-pin-dotted').addClass('icon-pin-full');
                    this.changeOpacityOnMarker(1);
                    this.state = STATE.FULL;
                    break;
            }
        },

        changeOpacityOnMarker: function(opacity) {
            _.each(this.meshs, function(markerMesh) {
                markerMesh.material.opacity = opacity;
            });
        },

        bindControlEvents: function() {
            var self = this;
            $('#managePin .moveBtnLeft').click(function() {
                self.rescaleMarkers(-0.5);
            });

            $('#managePin .moveBtnRight').click(function() {
                self.rescaleMarkers(0.5);
            });

            $('#managePin .moveBtnCenter').click(function() {
                self.changeMarkerState();
            });
        },

        showPopup: function(marker) {
            $('#issueTitle').text(marker.getTitle());
            $('#issueAuthor').text(marker.getDescription());
            $('#issueDate').text(marker.getDescription());
            $('#issueDesc').text(marker.getDescription());
            $('#issueComment').html(marker.getDescription());
            $('#issueZone').text(marker.getDescription());
            $('#issueResponsible').text(marker.getDescription());
            $('#issueCriticity').text(marker.getDescription());
            $('#issueModal').modal('show');
        }

    }

    return LayerManager;

});
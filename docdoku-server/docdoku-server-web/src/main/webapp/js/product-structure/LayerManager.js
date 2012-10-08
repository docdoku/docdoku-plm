define([
    "collections/layer_collection",
    "models/layer",
    "views/layers-list-view"
], function (
    LayerCollection,
    Layer,
    LayersListView
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
        this.markerStateControl = $('#markerState');
        this.layersCollection = new LayerCollection();
        domEvent = new THREEx.DomEvent(camera, container);
    };

    LayerManager.prototype = {

        renderList: function() {
            new LayersListView({collection: this.layersCollection}).render();
        },

        addMeshFromMarker: function(marker, material) {
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
                material
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

            marker.set('onScene', true);
        },

        removeMeshFromMarker: function(marker) {
            this._removeMesh(marker.cid);
            marker.set('onScene', false);
        },

        _removeMesh: function(cid) {
            domEvent.unbind(this.meshs[cid], 'click');
            this.scene.remove(this.meshs[cid]);
            delete this.meshs[cid];
        },

        createLayer: function(name) {
            var layerId = guid();
            if (name) {
                var layer = new Layer({
                    _id: layerId,
                    name : name
                });
            } else {
                var layer = new Layer({_id: layerId});
            }
            this.layersCollection.create(layer);
            return layer;
        },

        removeLayer: function(layer) {
            this.layersCollection.remove(layer);
        },

        removeAllLayers: function() {
            this.layersCollection.each(function(layer) {
                layer.trigger('remove');
            });
            this.layersCollection.reset({silent: true});
        },

        rescaleMarkers: function(value) {
            for (var cid in this.meshs) {
                var currentMesh = this.meshs[cid];
                currentMesh.scale.x += value;
                currentMesh.scale.y += value;
                currentMesh.scale.z += value;
            };
        },

        changeMarkerState: function() {
            switch(this.state) {
                case STATE.FULL  :
                    this.markerStateControl.removeClass('icon-marker-full').addClass('icon-marker-empty');
                    this.changeOpacityOnMarker(0.4);
                    this.state = STATE.TRANSPARENT;
                    break;
                case STATE.TRANSPARENT :
                    this.markerStateControl.removeClass('icon-marker-empty').addClass('icon-marker-dotted');
                    this.changeOpacityOnMarker(0);
                    this.state = STATE.HIDDEN;
                    break;
                case STATE.HIDDEN:
                    this.markerStateControl.removeClass('icon-marker-dotted').addClass('icon-marker-full');
                    this.changeOpacityOnMarker(1);
                    this.state = STATE.FULL;
                    break;
            }
        },

        changeOpacityOnMarker: function(opacity) {
            for (var cid in this.meshs) {
                this.meshs[cid].material.opacity = opacity;
            }
        },

        bindControlEvents: function() {
            var self = this;
            $('#manageMarker .moveBtnLeft').click(function() {
                self.rescaleMarkers(-0.5);
            });

            $('#manageMarker .moveBtnRight').click(function() {
                self.rescaleMarkers(0.5);
            });

            $('#manageMarker .moveBtnCenter').click(function() {
                self.changeMarkerState();
            });
        },

        showPopup: function(marker) {
            $('#markerTitle').text(marker.getTitle());
            $('#markerDesc').text(marker.getDescription());
            $('#markerModal').modal('show');
        }

    }

    return LayerManager;

});
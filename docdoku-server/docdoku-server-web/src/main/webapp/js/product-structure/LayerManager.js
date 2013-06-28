define([
    "collections/layer_collection",
    "models/layer",
    "views/layers-list-view",
    "views/marker_info_modal_view"
], function (
    LayerCollection,
    Layer,
    LayersListView,
    MarkerInfoModalView
) {

    var STATE = { FULL : 0, TRANSPARENT : 1};

    var LayerManager = function( scene, camera, renderer, controls, container ) {
        this.scene = scene;
        this.camera = camera;
        this.meshs = [];
        this.state = STATE.FULL;
        this.renderer = renderer;
        this.controls = controls;
        this.container = container;
        this.markerStateControl = $('#markerState i');
        this.layersCollection = new LayerCollection();
        this.domEvent = new THREEx.DomEvent(this.camera, container);
        this.markerScale = new THREE.Vector3(1,1,1);
    };

    LayerManager.prototype = {

        renderList: function() {
            new LayersListView({collection: this.layersCollection}).render();
        },

        updateCamera: function(camera, controls) {
            this.camera = camera;
            this.controls = controls;

            this.layersCollection.each(function(layer) {
                layer._removeAllMarkersFromScene();
            });

            this.domEvent = new THREEx.DomEvent(camera, this.container);

            this.layersCollection.each(function(layer) {
                if(layer.get('shown'))
                    layer._addAllMarkersToScene();
            });
        },

        addMeshFromMarker: function(marker, material) {
            // set up the sphere vars
            var radius = 50,
                segments = 16,
                rings = 16;

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

            this.domEvent.bind(markerMesh, 'click', function(){
                if (self.state != STATE.HIDDEN) {
                    self.showPopup(marker);
                }
            });

            // add the sphere to the scene
            this.scene.add( markerMesh );

            // rescale the marker to the others markers scale
            markerMesh.scale = this.markerScale;

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
            this.domEvent.unbind(this.meshs[cid], 'click');
            this.scene.remove(this.meshs[cid]);
            delete this.meshs[cid];
        },

        createLayer: function(name) {
            var layer;
            if (name) {
                layer = new Layer({
                    name : name
                });
            } else {
                layer = new Layer();
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

        rescaleMarkers: function() {
            for (var cid in this.meshs) {
                var currentMesh = this.meshs[cid];
                currentMesh.scale = this.markerScale;
            }
        },

        changeMarkerState: function() {
            switch(this.state) {
                case STATE.FULL  :
                    this.markerStateControl.removeClass('icon-circle').addClass('icon-circle-blank');
                    this.changeOpacityOnMarker(0.4);
                    this.state = STATE.TRANSPARENT;
                    break;
                case STATE.TRANSPARENT :
                    this.markerStateControl.removeClass('icon-circle-blank').addClass('icon-circle');
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


        showPopup: function(marker) {
            var mimv = new MarkerInfoModalView({model:marker});
            $("body").append(mimv.render().el);
            mimv.openModal();
        }

    };

    return LayerManager;

});
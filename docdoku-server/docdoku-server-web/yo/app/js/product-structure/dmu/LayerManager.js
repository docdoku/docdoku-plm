/*global define,App,THREE*/
define([
    'collections/layer_collection',
    'models/layer',
    'views/layers-list-view',
    'views/marker_info_modal_view'
], function (LayerCollection, Layer, LayersListView, MarkerInfoModalView) {
	'use strict';

    var STATE = { FULL: 0, TRANSPARENT: 1};

    var LayerManager = function () {
        this.meshs = [];
        this.state = STATE.FULL;
        this.markerStateControl = App.$ControlsContainer ? App.$ControlsContainer.find('#markerState i') : [];
        this.layersCollection = new LayerCollection();
        this.markerScale = new THREE.Vector3(1, 1, 1);
        this.markers = [];
    };

    LayerManager.prototype = {

        renderList: function () {
            App.layersListView = new LayersListView({collection: this.layersCollection});
            App.layersListView.render();
        },

        removeAllMeshesFromMarkers: function () {
            var cid;
            var currentMesh;
            for (cid in this.meshs) {
                currentMesh = this.meshs[cid];
                App.sceneManager.scene.remove(currentMesh);
            }

            App.sceneManager.reDraw();
        },

        addMeshFromMarker: function (marker, material) {
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
            markerMesh.markerId = marker.cid;

            // add the sphere to the scene
            App.sceneManager.scene.add(markerMesh);
            App.sceneManager.reDraw();

            // rescale the marker to the others markers scale
            markerMesh.scale.copy(this.markerScale);

            //save the mesh for further reuse
            this.meshs[marker.cid] = markerMesh;
            this.markers[marker.cid] = marker;

            markerMesh.geometry.dynamic = true;

            marker.set('onScene', true);
        },

        onMarkerClicked: function (markerId) {
            var marker = this.markers[markerId];
            this.showPopup(marker);
        },

        removeMeshFromMarker: function (marker) {
            this._removeMesh(marker.cid);
            marker.set('onScene', false);
        },

        _removeMesh: function (cid) {
            App.sceneManager.scene.remove(this.meshs[cid]);
            App.sceneManager.reDraw();
            delete this.meshs[cid];
        },

        createLayer: function (name) {
            var layer;

            var randomColor =
                Math.ceil((Math.random() * (0xF))).toString(16) +
                Math.ceil((Math.random() * (0xF))).toString(16) +
                Math.ceil((Math.random() * (0xF))).toString(16) +
                Math.ceil((Math.random() * (0xF))).toString(16) +
                Math.ceil((Math.random() * (0xF))).toString(16) +
                Math.ceil((Math.random() * (0xF))).toString(16);

            if (name) {
                layer = new Layer({
                    name: name,
                    color: randomColor
                });
            } else {
                layer = new Layer({
                    color: randomColor
                });
            }

            this.layersCollection.create(layer, {success: function () {
                App.collaborativeController.sendLayersRefresh('create layer');
            }});
            return layer;
        },

        removeLayer: function (layer) {
            this.layersCollection.remove(layer);
        },

        removeAllLayers: function () {
            this.layersCollection.each(function (layer) {
                layer.trigger('remove');
            });
            this.layersCollection.reset({silent: true});
        },

        rescaleMarkers: function () {
            for (var cid in this.meshs) {
                var currentMesh = this.meshs[cid];
                currentMesh.scale.copy(this.markerScale);
            }
            App.sceneManager.reDraw();
        },

        changeMarkerState: function () {
            switch (this.state) {
                case STATE.FULL  :
                    this.markerStateControl.removeClass('fa-circle').addClass('fa-circle-o');
                    this.changeOpacityOnMarker(0.4);
                    this.state = STATE.TRANSPARENT;
                    break;
                case STATE.TRANSPARENT :
                    this.markerStateControl.removeClass('fa-circle-o').addClass('fa-circle');
                    this.changeOpacityOnMarker(1);
                    this.state = STATE.FULL;
                    break;
            }
        },

        changeOpacityOnMarker: function (opacity) {
            for (var cid in this.meshs) {
                this.meshs[cid].material.opacity = opacity;
            }
            App.sceneManager.reDraw();
        },


        showPopup: function (marker) {
            var mimv = new MarkerInfoModalView({model: marker});
            window.document.body.appendChild(mimv.render().el);
            mimv.openModal();
        }

    };

    return LayerManager;
});

/*global _,define,App*/
define([
    'backbone',
    'models/marker',
    'collections/marker_collection'
], function (Backbone, Marker, MarkerCollection) {
    'use strict';
    var Layer = Backbone.Model.extend({

        defaults: {
            name: App.config.i18n.NEW_LAYER,
            shown: true,
            editingName: false,
            editingMarkers: false
        },

        toJSON: function () {
            return _.pick(this.attributes, 'id', 'name', 'color');
        },

        setEditingName: function (editingName) {
            this.set('editingName', editingName);
        },

        getColor: function () {
            return this.get('color');
        },
        setColor: function (color) {
            this.material.color.set(parseInt('0x' + color, 16));
            this.material.needsUpdate = true;
            return this.set('color', color);
        },

        toggleEditingMarkers: function () {
            if (this.get('editingMarkers')) {
                this.collection.setAllEditingMarkers(false);
            } else {
                this.collection.setAllEditingMarkers(false);
                this.setEditingMarkers(true);
            }
        },

        setEditingMarkers: function (editingMarkers) {
            this.set('editingMarkers', editingMarkers);
            if (editingMarkers) {
                App.sceneManager.startMarkerCreationMode(this);
            } else {
                App.sceneManager.stopMarkerCreationMode(this);
            }
        },

        toggleShow: function () {
            var shown = !this.get('shown');
            this.setShown(shown);
        },

        setShown: function (shown) {
            this.set('shown', shown);
            if (shown) {
                this._addAllMarkersToScene();
            } else {
                this._removeAllMarkersFromScene();
            }
        },

        initialize: function () {
            this.material = App.sceneManager.createLayerMaterial(parseInt('0x' + this.get('color'), 16));
            this.markers = new MarkerCollection();
            this.markers.on('add', this._addMarkerToScene, this);
            this.markers.on('remove', this._removeMarkerFromScene, this);
            this.markers.on('reset', this._onResetMarkers, this);
            this.on('remove', this._removeAllMarkersFromScene, this);
            this.on('change:id', this._setMarkersUrl);
            if (!this.isNew()) {
                this._setMarkersUrl();
                this.markers.fetch({reset: true});
            }

            _.bindAll(this);
        },

        _setMarkersUrl: function () {
            this.markers.urlLayer = this.url();
        },

        getMarkers: function () {
            return this.markers;
        },

        countMarkers: function () {
            return this.getMarkers().length;
        },

        getMaterial: function () {
            return this.material;
        },

        createMarker: function (title, description, x, y, z) {
            var marker = new Marker({
                title: title,
                description: description,
                x: x,
                y: y,
                z: z
            });
            this.getMarkers().create(marker, {success: function () {
                App.collaborativeController.sendMarkersRefresh('create marker');
            }});

            return marker;
        },

        removeMarker: function (marker) {
            this.getMarkers().remove(marker);
        },

        removeAllMarkers: function () {
            this._removeAllMarkersFromScene();
            this.getMarkers().reset();
        },

        _addAllMarkersToScene: function () {
            _.each(this.getMarkers().notOnScene(), this._addMarkerToScene, this);
        },

        _addMarkerToScene: function (marker) {
            App.sceneManager.layerManager.addMeshFromMarker(marker, this.material);
        },

        _removeAllMarkersFromScene: function () {
            _.each(this.getMarkers().onScene(), this._removeMarkerFromScene, this);
        },

        _removeMarkerFromScene: function (marker) {
            App.sceneManager.layerManager.removeMeshFromMarker(marker);
        },

        getHexaColor: function () {
            return '#' + this.get('color');
        },

        _onResetMarkers: function () {
            this._removeAllMarkersFromScene();
            this.getMarkers().each(this._addMarkerToScene, this);
        }

    });

    return Layer;

});

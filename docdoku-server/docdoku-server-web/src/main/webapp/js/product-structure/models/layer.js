/*global sceneManager*/
define([
    "models/marker",
    "collections/marker_collection",
    "!i18n!localization/nls/product-structure-strings"
], function (
    Marker,
    MarkerCollection,
    i18n
) {

    var Layer = Backbone.Model.extend({

        defaults: {
            name : i18n.NEW_LAYER,
            shown: true,
            editingName: false,
            editingMarkers: false
        },

        toJSON: function() {
            return _.pick(this.attributes, 'id', 'name');
        },

        setEditingName: function(editingName) {
            this.set('editingName', editingName);
        },

        toggleEditingMarkers: function() {
            if (this.get('editingMarkers')) {
                this.collection.setAllEditingMarkers(false);
            } else {
                this.collection.setAllEditingMarkers(false);
                this.setEditingMarkers(true);
            }
        },

        setEditingMarkers: function(editingMarkers) {
            this.set('editingMarkers', editingMarkers);
            if(editingMarkers){
                sceneManager.startMarkerCreationMode(this);
            }else{
                sceneManager.stopMarkerCreationMode(this);
            }
        },

        toggleShow: function() {
            var shown = !this.get('shown');
            this.setShown(shown);
        },

        setShown: function(shown) {
            this.set('shown', shown);
            if(shown){
                this._addAllMarkersToScene();
            }else{
                this._removeAllMarkersFromScene();
            }
        },

        initialize: function() {
            if (!this.has('color')) {

              var  randomColor = Math.ceil((Math.random()*(0xF))).toString(16)
                            + Math.ceil((Math.random()*(0xF))).toString(16)
                            + Math.ceil((Math.random()*(0xF))).toString(16)
                            + Math.ceil((Math.random()*(0xF))).toString(16)
                            + Math.ceil((Math.random()*(0xF))).toString(16)
                            + Math.ceil((Math.random()*(0xF))).toString(16);

                this.set('color', randomColor);
            }
            this.material = new THREE.MeshLambertMaterial({
                color:  parseInt('0x' + this.get('color'), 16),
                opacity: 1,
                transparent: true
            });
            this.markers = new MarkerCollection();
            this.markers.on("add", this._addMarkerToScene, this);
            this.markers.on("remove", this._removeMarkerFromScene, this);
            this.markers.on("reset", this._onResetMarkers, this);
            this.on('remove', this._removeAllMarkersFromScene, this);
            this.on('change:id', this._setMarkersUrl);
            if (!this.isNew()) {
                this._setMarkersUrl();
                this.markers.fetch({reset:true});
            }
        },

        _setMarkersUrl: function() {
            this.markers.urlLayer = this.url();
        },

        getMarkers: function() {
            return this.markers;
        },

        countMarkers: function() {
            return this.getMarkers().length;
        },

        getMaterial: function() {
            return this.material;
        },

        createMarker: function(title, description, x, y, z) {
            var marker = new Marker({
                title: title,
                description: description,
                x: x,
                y: y,
                z: z
            });
            this.getMarkers().create(marker);
            return marker;
        },

        removeMarker: function(marker) {
            this.getMarkers().remove(marker);
        },

        removeAllMarkers: function() {
            this._removeAllMarkersFromScene();
            this.getMarkers().reset();
        },

        _addAllMarkersToScene: function() {
            _.each(this.getMarkers().notOnScene(), this._addMarkerToScene, this);
        },

        _addMarkerToScene: function(marker) {
            sceneManager.layerManager.addMeshFromMarker(marker, this.material);
        },

        _removeAllMarkersFromScene: function() {
            _.each(this.getMarkers().onScene(), this._removeMarkerFromScene, this);
        },

        _removeMarkerFromScene: function(marker) {
            sceneManager.layerManager.removeMeshFromMarker(marker);
        },

        getHexaColor: function() {
            return "#" + this.get('color');
        },

        _onResetMarkers: function() {
            this.getMarkers().each(this._addMarkerToScene, this);
        }

    });

    return Layer;

});

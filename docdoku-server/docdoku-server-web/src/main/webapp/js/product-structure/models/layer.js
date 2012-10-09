define([
    "models/marker",
    "collections/marker_collection"
], function (
    Marker,
    MarkerCollection
) {

    var Layer = Backbone.Model.extend({

        defaults: {
            name : "new layer",
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
            editingMarkers ? sceneManager.startMarkerCreationMode(this) : sceneManager.stopMarkerCreationMode(this);
        },

        toggleShow: function() {
            var shown = !this.get('shown');
            this.setShown(shown);
        },

        setShown: function(shown) {
            this.set('shown', shown);
            shown ? this._addAllMarkersToScene() : this._removeAllMarkersFromScene();
        },

        initialize: function() {
            if (!this.has('color')) {
                //http://paulirish.com/2009/random-hex-color-code-snippets/
                this.set('color', ('00000'+(Math.random()*16777216<<0).toString(16)).substr(-6))
            }
            this.material = new THREE.MeshLambertMaterial({
                color: "0x" + this.get('color'),
                opacity: 1,
                transparent: true
            });
            this.markers = new MarkerCollection([], {urlLayer: this.url()});
            this.markers.on("add", this._addMarkerToScene, this);
            this.markers.on("remove", this._removeMarkerFromScene, this);
            this.markers.on("reset", this._onResetMarkers, this);
            this.markers.fetch();
            this.on('remove', this._removeAllMarkersFromScene, this);
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

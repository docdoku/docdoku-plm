define([
    "models/marker",
    "collections/marker_collection"
], function (
    Marker,
    MarkerCollection
) {

    var Layer = Backbone.Model.extend({

        initialize: function() {
            this.set('markers', new MarkerCollection());
            this.getMarkers().on("reset", this._addAllMarkersToScene, this);
            this.getMarkers().on("add", this._addMarkerToScene, this);
            this.getMarkers().on("remove", this._removeMarkerFromScene, this);
            this.on('remove', this._removeAllMarkers, this);
        },

        getMarkers: function() {
            return this.get('markers');
        },

        createMarker: function(title, description, x, y, z) {
            this.getMarkers().add({
                title: title,
                description: description,
                x: x,
                y: y,
                z: z
            });
        },

        removeMarker: function(marker) {
            this.getMarkers().remove(marker);
        },

        _addAllMarkersToScene: function() {
            this._removeAllMarkers();
            this.getMarkers().each(this._addMarkerToScene, this);
        },

        _addMarkerToScene: function(marker) {
            sceneManager.layerManager.addMeshFromMarker(marker);
        },

        _removeAllMarkers: function() {
            this.getMarkers().each(this._removeMarkerFromScene, this);
        },

        _removeMarkerFromScene: function(marker) {
            sceneManager.layerManager.removeMeshFromMarker(marker);
        }

    });

    return Layer;

});

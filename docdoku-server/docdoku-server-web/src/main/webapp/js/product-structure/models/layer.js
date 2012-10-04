define([
    "models/marker",
    "collections/marker_collection"
], function (
    Marker,
    MarkerCollection
) {

    var Layer = Backbone.Model.extend({

        initialize: function() {
            this.set('material', new THREE.MeshLambertMaterial({
                //http://paulirish.com/2009/random-hex-color-code-snippets/
                color: this.has('color') ? this.get('color') : "0x" + (Math.random()*0xFFFFFF<<0).toString(16),
                opacity: 1,
                transparent: true
            }));
            this.set('markers', new MarkerCollection());
            this.getMarkers().on("add", this._addMarkerToScene, this);
            this.getMarkers().on("remove", this._removeMarkerFromScene, this);
            this.on('remove', this._removeAllMarkers, this);
        },

        getMarkers: function() {
            return this.get('markers');
        },

        countMarkers: function() {
            return this.getMarkers().length;
        },

        getMaterial: function() {
            return this.get('material');
        },

        createMarker: function(title, description, x, y, z) {
            var marker = new Marker({
                title: title,
                description: description,
                x: x,
                y: y,
                z: z
            });
            this.getMarkers().add(marker);
            return marker;
        },

        removeMarker: function(marker) {
            this.getMarkers().remove(marker);
        },

        removeAllMarkers: function() {
            this._removeAllMarkers();
            this.getMarkers().reset();
        },

        _addAllMarkersToScene: function() {
            this._removeAllMarkers();
            this.getMarkers().each(this._addMarkerToScene, this);
        },

        _addMarkerToScene: function(marker) {
            sceneManager.layerManager.addMeshFromMarker(marker, this.getMaterial());
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

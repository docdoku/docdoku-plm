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
            editing: false
        },

        toggleEditing: function() {
            this.set('editing', !this.get('editing'));
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
            this.set('markers', new MarkerCollection());
            this.getMarkers().on("add", this._addMarkerToScene, this);
            this.getMarkers().on("remove", this._removeMarkerFromScene, this);
            this.on('remove', this._removeAllMarkersFromScene, this);
        },

        getMarkers: function() {
            return this.get('markers');
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
            this.getMarkers().add(marker);
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
        }

    });

    return Layer;

});

define([
    "views/layer-item-view",
    "views/layer-header-view"
], function (
    LayerItemView,
    LayerHeaderView
) {

    var LayersListView = Backbone.View.extend({

        el: 'div#layer-wrapper',

        events: {
            "layers:setAllShown" : "setAllShown",
            "layers:addLayer" : "addLayer"
        },

        initialize: function() {
            this.listContainer = this.$("nav > ul");
            this.collection.bind('add', this.addOne, this);
            this.collection.bind('reset', this.addAll, this);
            this.collection.fetch();
        },

        addAll: function() {
            if (this.collection.length > 0) {
                this.listContainer.empty();
            }
            this.collection.each(this.addOne, this);
        },

        addOne: function(layer) {
            /* if this is the first layer, remove the empty view */
            if (this.collection.length == 1) {
                this.listContainer.empty();
            }
            var layerItemView = new LayerItemView({model: layer});
            this.listContainer.prepend(layerItemView.render().el);
        },

        render: function() {
            var headerContainer = this.$("ul#layer-header");
            headerContainer.html(new LayerHeaderView().render().el);
            if (this.collection.isEmpty()) {
                this.addEmptyView();
            }
            return this;
        },

        template_empty_view: "<li>No layers</li>",

        addEmptyView: function() {
            this.listContainer.append(this.template_empty_view);
        },

        setAllShown: function(e, allShown) {
            e.stopPropagation();
            this.collection.setAllShown(allShown);
        },

        addLayer: function(e) {
            e.stopPropagation();
            var layer = sceneManager.layerManager.createLayer();
            layer.set('editingName', true);
        }

    });

    return LayersListView;

});
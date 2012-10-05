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
            "layers:toggleAllShown" : "toggleAllShown",
            "layers:addLayer" : "addLayer"
        },

        initialize: function() {
            this.collection.bind('add', this.addOne, this);
        },

        addOne: function(layer) {
            var layerItemView = new LayerItemView({model: layer});

            /* if this is the first layer, remove the empty view */
            if (this.collection.length == 1) {
                this.listContainer.empty();
            }

            this.listContainer.prepend(layerItemView.render().el);
        },

        render: function() {
            this.$el.html(this.template);
            this.listContainer = this.$("nav > ul");
            var headerContainer = this.$("ul#layer-header");
            headerContainer.html(new LayerHeaderView().render().el);
            if (this.collection.isEmpty()) {
                this.addEmptyView();
            }
            return this;
        },

        template: '<ul id="layer-header"></ul><nav><ul></ul></nav>',

        template_empty_view: "<li>No layers</li>",

        addEmptyView: function() {
            this.listContainer.append(this.template_empty_view);
        },

        toggleAllShown: function(e, allShown) {
            e.stopPropagation();
            this.collection.toggleAllShown(allShown);
        },

        addLayer: function(e) {
            e.stopPropagation();
            var layer = sceneManager.layerManager.createLayer();
            layer.set('editing', true);
        }

    });

    return LayersListView;

});
define([
    "views/layer-item-view",
    "views/layer-header-view"
], function (
    LayerItemView,
    LayerHeaderView
) {

    var LayersListView = Backbone.View.extend({

        el: 'div#layer-wrapper',

        initialize: function() {
            this.collection.bind('add', this.addOne, this);
        },

        addOne: function(layer) {
            var layerItemView = new LayerItemView({model: layer});
            this.listContainer.prepend(layerItemView.render().el);
        },

        render: function() {
            this.$el.html(this.template);
            this.listContainer = this.$("nav > ul");
            var headerContainer = this.$("ul#layer-header");
            headerContainer.html(new LayerHeaderView().render().el);
            return this;
        },

        template: '<ul id="layer-header"></ul><nav><ul></ul></nav>'

    });

    return LayersListView;

});
/*global sceneManager*/
define([
    "views/layer-item-view",
    "views/layer-header-view",
    "i18n!localization/nls/product-structure-strings"
], function (
    LayerItemView,
    LayerHeaderView,
    i18n
) {

    var LayersListView = Backbone.View.extend({

        el: 'div#layer-wrapper',

        events: {
            "layers:setAllShown" : "setAllShown",
            "layers:addLayer" : "addLayer"
        },

        initialize: function() {
            this.listContainer = this.$("nav > ul");
            this.listenTo(this.collection, 'add', this.addOne)
                .listenTo(this.collection, 'remove', this.onRemove)
                .listenTo(this.collection, 'reset', this.addAll);
            this.collection.fetch({reset:true});
        },

        addAll: function() {
            if (this.collection.length > 0) {
                this.listContainer.empty();
            }
            this.collection.each(this.addOne, this);
        },

        onRemove:function(){
            if (this.collection.length <= 0) {
                this.listContainer.empty();
                this.addEmptyView();
            }
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
            this.$el.prepend(new LayerHeaderView().render().el);
            if (this.collection.isEmpty()) {
                this.addEmptyView();
            }
            return this;
        },

        template_empty_view: "<li>"+i18n.NO_LAYERS+"</li>",

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
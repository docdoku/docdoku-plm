define(["text!templates/layer_controls.html","!i18n!localization/nls/product-structure-strings"], function (template,i18n) {

    var LayerHeaderView = Backbone.View.extend({

        tagName: 'div',

        className:'btn-group',

        events: {
            "click button.toggleAllShow" : "toggleAllShow",
            "click button.addLayer"   : "addLayer"
        },

        initialize: function() {
            this.allShown = true;
        },

        template: Mustache.compile(template),

        render: function() {
            this.$el.html(this.template({i18n:i18n}));
            this.$el.toggleClass('shown', this.allShown);
            return this;
        },

        toggleAllShow: function() {
            this.allShown = !this.allShown;
            this.render();
            this.$el.trigger('layers:setAllShown', this.allShown);
        },

        addLayer: function() {
            this.$el.trigger('layers:addLayer');
        }

    });

    return LayerHeaderView;

});
define(function() {

    var LayerHeaderView = Backbone.View.extend({

        tagName: 'li',

        events: {
            "click i.start" : "toggleAllShow",
            "click i.end"   : "addLayer"
        },

        initialize: function() {
            this.allShown = true;
        },

        template: '<i class="icon-eye-open start"></i>Layers<i class="icon-plus end"></i>',

        render: function() {
            this.$el.html(this.template);
            this.$el.toggleClass('shown', this.allShown)
            return this;
        },

        toggleAllShow: function() {
            this.allShown = !this.allShown;
            this.render();
            this.$el.trigger('layers:setAllShown', this.allShown);
        },

        addLayer: function() {
            this.$el.trigger('layers:addLayer')
        }

    });

    return LayerHeaderView;

});
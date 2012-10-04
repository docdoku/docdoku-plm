define(function() {

    var LayerHeaderView = Backbone.View.extend({

        tagName: 'li',

        template: '<i class="icon-eye-open start"></i>Layers<i class="icon-plus end"></i>',

        render: function() {
            this.$el.html(this.template);
            return this;
        }

    });

    return LayerHeaderView;

});
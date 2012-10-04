define(function() {

    var LayerItemView = Backbone.View.extend({

        tagName: 'li',

        initialize: function() {
            this.model.getMarkers().on('add remove reset', this.render, this);
        },

        template: "<i class=\"icon-eye-open start\"></i>{{ name }} ({{ countMarkers }})<i class=\"icon-pencil end\"></i>",

        render: function() {
            var data = {
                name: this.model.get('name'),
                countMarkers: this.model.countMarkers()
            }
            this.$el.html(Mustache.render(this.template, data));
            return this;
        }

    });

    return LayerItemView;

});
/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/layer_controls.html'
], function (Backbone, Mustache, template) {
    'use strict';
    var LayerHeaderView = Backbone.View.extend({
        tagName: 'div',
        className: 'btn-group',

        events: {
            'click button.toggleAllShow': 'toggleAllShow',
            'click button.addLayer': 'addLayer'
        },

        initialize: function () {
            this.allShown = true;
        },

        render: function () {
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n}));
            this.$el.toggleClass('shown', this.allShown);
            return this;
        },

        toggleAllShow: function () {
            this.allShown = !this.allShown;
            this.render();
            this.$el.trigger('layers:setAllShown', this.allShown);
        },

        addLayer: function () {
            this.$el.trigger('layers:addLayer');
        }

    });

    return LayerHeaderView;

});

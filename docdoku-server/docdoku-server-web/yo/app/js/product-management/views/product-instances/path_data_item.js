/*global define,_*/
define([
    'backbone',
    'mustache',
    'text!templates/product-instances/path_data_item.html'
], function (Backbone, Mustache, template) {
    'use strict';
    var PathDataItemView = Backbone.View.extend({

        initialize: function () {
            _.bindAll(this);
        },

        render: function () {
            var path = '';

            for (var i=0; i<this.model.length; i++) {
                var part = this.model[i];
                path += part.name ? part.name + ' < ' + part.number + ' >' : '< ' + part.number + ' >';
                path += ' <i class="fa fa-chevron-right"></i> ';
            }

            this.$el.html(template);
            this.$('div.well').first().append(path);
            this.$('.fa.fa-chevron-right').last().remove();
            return this;
        }

    });

    return PathDataItemView;
});

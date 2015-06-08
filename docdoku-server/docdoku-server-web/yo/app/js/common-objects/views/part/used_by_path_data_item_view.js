/*global _,$,define,App*/
define([
    'backbone',
    'mustache',
    'text!common-objects/templates/part/used_by_path_data_item.html'
], function (Backbone, Mustache, template) {
    'use strict';
    var UsedByPathDataItemView = Backbone.View.extend({

        tagName: 'li',
        className: 'used-by-item well',

        initialize: function () {
            _.bindAll(this);
        },

        render: function () {
            var data = {
                i18n: App.config.i18n,
                model: this.model
            };
            this.$el.html(Mustache.render(template, data));
            var self = this;
            var partsPath = this.model.getPartsPath();

            _.each(partsPath, function (part) {
                var path = part.name ? part.name + ' < ' + part.number + ' >' : '< ' + part.number + ' >';
                self.$('.path-description').append(path);
                self.$('.path-description').append('<i class="fa fa-chevron-right">');
            });

            self.$('.fa.fa-chevron-right').last().remove();


            return this;
        }

    });

    return UsedByPathDataItemView;
});


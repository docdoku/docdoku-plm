/*global _,define,App*/
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

            var partLinks = this.model.getPartLinks();
            var $pathDescription = this.$('.path-description');
            _.each(partLinks, function (partLink) {
                var text =  partLink.name + ' < ' + partLink.number + ' >';
                if(partLink.referenceDescription){
                    text+=' ('+partLink.referenceDescription+')';
                }
                $pathDescription.append(text + ' <i class="fa fa-long-arrow-right"> ');
            });

            this.$('.fa.fa-long-arrow-right').last().remove();

            return this;
        }

    });

    return UsedByPathDataItemView;
});


/*global _,define,App,require*/
define([
    'backbone',
    'mustache',
    'text!common-objects/templates/part/used_by_list_item_part.html'
], function (Backbone, Mustache, template) {
    'use strict';
    var UsedByListItemPartView = Backbone.View.extend({

        tagName: 'li',
        className: 'used-by-item well',

        events:{
            'click a.reference': 'toPartDetailView'
        },

        initialize: function () {
            _.bindAll(this);
        },

        render: function () {
            var data = {
                i18n: App.config.i18n,
                model: this.model
            };

            this.$el.html(Mustache.render(template, data));
            return this;
        },

        toPartDetailView:function(){
            this.$el.trigger('close-modal-request');
            var part = this.model;
            part.fetch().success(function () {
                require(['common-objects/views/part/part_modal_view'],function(PartModalView){
                    var partModalView = new PartModalView({
                        model: part
                    });
                    partModalView.show();
                });
            });
        }

    });

    return UsedByListItemPartView;
});

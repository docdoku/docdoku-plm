/*global _,define,App*/
define([
    'backbone',
    'mustache',
    'common-objects/views/part/used_by_path_data_item_view',
    'text!common-objects/templates/part/used_by_pd_instance_list.html'
], function (Backbone, Mustache, UsedByListItemView, template) {
    'use strict';
    var UsedByListView = Backbone.View.extend({

        className: 'used-by-items-view',

        initialize: function () {
            _.bindAll(this);
        },

        render: function () {
            var data = {
                serialNumber: this.options.collection.at(0).getSerialNumber(),
                i18n: App.config.i18n
            };

            this.$el.html(Mustache.render(template, data));
            this.bindDomElements();

            this.usedByProductInstanceViews = [];
            this.options.collection.each(this.addProductInstanceView.bind(this));

            return this;
        },

        bindDomElements: function () {
            this.productInstancesUL = this.$('#used-by-product-instances');
        },

        addProductInstanceView: function (model) {
            var usedByView = new UsedByListItemView({
                model: model
            }).render();

            this.usedByProductInstanceViews.push(usedByView);
            this.productInstancesUL.append(usedByView.$el);
        }

    });

    return UsedByListView;
});

/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/product/product_list_item.html',
    'views/product/product_details_view'
], function (Backbone, Mustache, template, ProductDetailsView) {
    'use strict';
    var ProductListItemView = Backbone.View.extend({

        events: {
            'click input[type=checkbox]': 'selectionChanged',
            'click td.product_id': 'openDetailsView'
        },

        tagName: 'tr',

        initialize: function () {
            this._isChecked = false;
        },

        render: function () {
            this.$el.html(Mustache.render(template, {model: this.model, url: this.model.getIndexUrl(), i18n: App.config.i18n}));
            this.$checkbox = this.$('input[type=checkbox]');
            this.trigger('rendered', this);
            return this;
        },

        selectionChanged: function () {
            this._isChecked = this.$checkbox.prop('checked');
            this.trigger('selectionChanged', this);
        },

        isChecked: function () {
            return this._isChecked;
        },

        check: function () {
            this.$checkbox.prop('checked', true);
            this._isChecked = true;
        },

        unCheck: function () {
            this.$checkbox.prop('checked', false);
            this._isChecked = false;
        },

        openDetailsView: function () {
            var that = this;
            var pdv = new ProductDetailsView({model: that.model});
            window.document.body.appendChild(pdv.render().el);
            pdv.openModal();
        }

    });

    return ProductListItemView;
});

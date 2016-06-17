/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/configuration/configuration_list_item.html',
    'views/configuration/configuration_details_view',
    'views/product/product_details_view',
    'models/configuration_item',
    'common-objects/utils/date'
], function (Backbone, Mustache, template, ConfigurationDetailsView, ProductDetailsView, ConfigurationItem, date) {
    'use strict';
    var ConfigurationListItemView = Backbone.View.extend({

        events: {
            'click input[type=checkbox]': 'selectionChanged',
            'click .configuration_id':'openConfigurationDetailView',
            'click .product_id':'openProductDetailView'
        },

        tagName: 'tr',

        initialize: function () {
            this._isChecked = false;
            this.listenTo(this.model, 'change', this.render);
        },

        render: function () {
            this.$el.html(Mustache.render(template, {model: this.model, i18n: App.config.i18n}));
            this.$checkbox = this.$('input[type=checkbox]');
            if (this.isChecked()) {
                this.check();
                this.trigger('selectionChanged', this);
            }
            this.trigger('rendered', this);
            date.dateHelper(this.$('.date-popover'));
            return this;
        },

        openConfigurationDetailView:function(){
            var model = this.model;
            model.fetch().success(function(){
                var view = new ConfigurationDetailsView({model: model});
                window.document.body.appendChild(view.render().el);
                view.openModal();
            });
        },

        openProductDetailView:function(){
            var model = new ConfigurationItem();
            model.set('_id',this.model.getConfigurationItemId());
            model.fetch().success(function(){
                var view = new ProductDetailsView({model:model});
                window.document.body.appendChild(view.render().el);
                view.openModal();
            });
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
        }

    });

    return ConfigurationListItemView;
});

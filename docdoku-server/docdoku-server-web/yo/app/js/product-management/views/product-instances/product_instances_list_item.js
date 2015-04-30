/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/product-instances/product_instances_list_item.html',
    'views/product-instances/product_instance_modal',
    'common-objects/utils/date'
], function (Backbone, Mustache, template, ProductInstanceModalView, date) {
    'use strict';
    var ProductInstancesListItemView = Backbone.View.extend({

        events: {
            'click input[type=checkbox]': 'selectionChanged',
            'click td.reference': 'openEditView'
        },

        tagName: 'tr',

        initialize: function () {
            this._isChecked = false;
        },
        render: function () {
            this.$el.html(Mustache.render(template, {
                model: this.model,
                i18n: App.config.i18n,
                bomUrl: this.model.getBomUrl(),
                sceneUrl:this.model.getSceneUrl(),
                zipUrl: this.model.getZipUrl(),
                isReadOnly: this.model.isReadOnly(),
                isFullAccess: this.model.isFullAccess()
            }));
            this.$checkbox = this.$('input[type=checkbox]');
            this.model.on('change', this.render, this);
            this.bindUserPopover();
            date.dateHelper(this.$('.date-popover'));
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

        bindUserPopover: function () {
            this.$('.author-popover').userPopover(this.model.getUpdateAuthor(), this.model.getSerialNumber(), 'left');
        },

        unCheck: function () {
            this.$checkbox.prop('checked', false);
            this._isChecked = false;
        },

        openEditView: function () {
            var model = this.model;
            model.fetch().success(function () {
                var view = new ProductInstanceModalView({model: model});
                view.render();
                window.document.body.appendChild(view.el);
            });
        }
    });

    return ProductInstancesListItemView;
});

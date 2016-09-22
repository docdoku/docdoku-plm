/*global App,define*/
define([
    'backbone',
    'mustache',
    'text!templates/baselines/baseline_list_item.html',
    'views/baselines/baseline_detail_view',
    'common-objects/utils/date'
], function (Backbone, Mustache, template, BaselineDetailView, date) {
    'use strict';
    var BaselineListItemView = Backbone.View.extend({
        events: {
            'click input[type=checkbox]': 'selectionChanged',
            'click td.reference': 'openDetailView'
        },

        tagName: 'tr',

        initialize: function () {
            this._isChecked = false;
        },

        render: function () {
            this.$el.html(Mustache.render(template, {
                i18n: App.config.i18n,
                model: this.model
            }));

            this.$checkbox = this.$('input[type=checkbox]');
            this.bindUserPopover();
            date.dateHelper(this.$('.date-popover'));

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

        bindUserPopover: function () {
            this.$('.author-popover').userPopover(this.model.getAuthorLogin(), App.config.i18n.BASELINE, 'left');
        },

        openDetailView: function () {
            var model = this.model;
            model.fetch().success(function () {
                new BaselineDetailView({model: model}).render();
            });
        }

    });

    return BaselineListItemView;
});

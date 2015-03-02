/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/baselines/baselines_list_item.html',
    'views/baselines/baseline_edit_view'
], function (Backbone, Mustache, template, BaselineEditView) {
    'use strict';
    var BaselineListItemView = Backbone.View.extend({
        events: {
            'click input[type=checkbox]': 'selectionChanged',
            'click td.reference': 'openEditView'
        },

        tagName: 'tr',

        initialize: function () {
            this._isChecked = false;
        },

        render: function () {
            this.$el.html(Mustache.render(template, {model: this.model, i18n: App.config.i18n}));
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

        openEditView: function () {
            new BaselineEditView({model: this.model, isForBaseline: true}).render();
        }
    });

    return BaselineListItemView;
});

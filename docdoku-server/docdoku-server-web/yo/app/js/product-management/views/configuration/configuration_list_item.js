/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/configuration/configuration_list_item.html',
    'views/configuration/configuration_details_view',
    'common-objects/utils/date'
], function (Backbone, Mustache, template, ConfigurationDetailsView, date) {
    'use strict';
    var ConfigurationListItemView = Backbone.View.extend({

        events: {
            'click input[type=checkbox]': 'selectionChanged',
            'click .configuration_id':'openConfigurationDetailView'
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

        openConfigurationDetailView:function(){
            var that = this;
            var view = new ConfigurationDetailsView({model: that.model});
            window.document.body.appendChild(view.render().el);
            view.openModal();
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

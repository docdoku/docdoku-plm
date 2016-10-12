/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/admin-options.html',
    'common-objects/models/admin',
    'common-objects/views/alert'
], function (Backbone, Mustache, template, Admin, AlertView) {
    'use strict';

    var AdminOptionsView = Backbone.View.extend({

        events: {
            'submit #platform-options-form': 'onSubmit'
        },

        initialize: function () {
        },

        render: function () {
            var _this = this;
            Admin.getPlatformOptions().then(function (options) {
                _this.$el.html(Mustache.render(template, {
                    i18n: App.config.i18n,
                    options: options
                }));
                _this.bindDOMElements();
                _this.fillOptions(options);
            });

            return this;
        },

        bindDOMElements: function () {
            this.$registrationStrategySelect = this.$('#registration-strategy');
            this.$workspaceCreationStrategy = this.$('#workspace-creation-verification-strategy');
            this.$notifications = this.$('.notifications');
        },

        fillOptions: function (options) {
            this.$registrationStrategySelect.val(options.registrationStrategy);
            this.$workspaceCreationStrategy.val(options.workspaceCreationStrategy);
        },

        onSubmit: function () {
            Admin.setPlatformOptions({
                registrationStrategy: this.$registrationStrategySelect.val(),
                workspaceCreationStrategy: this.$workspaceCreationStrategy.val()
            }).then(this.onSuccess.bind(this), this.onError.bind(this));
            return false;
        },

        onError: function (xhr) {
            this.$notifications.append(new AlertView({
                type: 'error',
                message: xhr.responseText
            }).render().$el);
        },

        onSuccess: function () {
            this.$notifications.append(new AlertView({
                type: 'success',
                message: App.config.i18n.SAVED
            }).render().$el);
        }

    });

    return AdminOptionsView;
});

/*global _,define,App*/
define([
    'backbone',
    'mustache',
    'collections/configurations',
    'text!templates/configuration/configuration_content.html'
], function (Backbone, Mustache, ConfigurationCollection, template) {
    'use strict';
	var ConfigurationContentView = Backbone.View.extend({
        partials: {
        },
        events: {
        },
        initialize: function () {
            _.bindAll(this);
        },
        render: function () {
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n}, this.partials));
            this.bindDomElements();
            return this;
        },
        bindDomElements: function () {
        }
    });
    return ConfigurationContentView;
});

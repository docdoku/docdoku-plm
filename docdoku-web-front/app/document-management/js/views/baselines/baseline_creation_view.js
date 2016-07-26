/*global _,define,App*/
define([
	'backbone',
	'mustache',
	'common-objects/collections/baselines',
	'text!templates/baselines/baseline_creation_view.html',
    'common-objects/views/alert'
], function (Backbone, Mustache, Baselines, template, AlertView) {

    'use strict';

	var BaselineCreationView = Backbone.View.extend({

		events: {
			'submit #baseline_creation_form': 'onSubmitForm',
			'hidden #baseline_creation_modal': 'onHidden'
		},

		initialize: function () {
			_.bindAll(this);
		},

		render: function () {
			this.$el.html(Mustache.render(template, {i18n: App.config.i18n}));
			this.bindDomElements();
            this.$inputBaselineName.customValidity(App.config.i18n.REQUIRED_FIELD);

            return this;
		},

		bindDomElements: function () {
			this.$modal = this.$('#baseline_creation_modal');
            this.$notifications = this.$el.find('.notifications').first();
			this.$inputBaselineName = this.$('#inputBaselineName');
			this.$inputBaselineDescription = this.$('#inputBaselineDescription');
		},

		onSubmitForm: function (e) {
            this.$notifications.empty();
            var data = {
                name: this.$inputBaselineName.val(),
                description: this.$inputBaselineDescription.val()
            };

            this.collection.create(data, {
                wait: true,
                success: this.onBaselineCreated,
                error: this.onError
            });

			e.preventDefault();
			e.stopPropagation();
			return false;
		},

		onBaselineCreated: function (e) {
            if (e.message) {
                this.trigger('warning', e.message);
            }
			this.closeModal();
		},

		onError: function (model, error) {
            var errorMessage = error ? error.responseText : model;

            this.$notifications.append(new AlertView({
                type: 'error',
                message: errorMessage
            }).render().$el);
		},

		openModal: function () {
			this.$modal.modal('show');
		},

		closeModal: function () {
			this.$modal.modal('hide');
		},

		onHidden: function () {
			this.remove();
		}
	});

	return BaselineCreationView;
});

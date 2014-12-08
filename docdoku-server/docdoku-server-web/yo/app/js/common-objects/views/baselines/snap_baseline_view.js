/*global _,define,App*/
define([
	'backbone',
	'mustache',
	'common-objects/collections/baselines',
	'text!common-objects/templates/baselines/snap_baseline_view.html',
    'common-objects/views/alert'
], function (Backbone, Mustache, Baselines, template, AlertView) {
    'use strict';

	var SnapLatestBaselineView = Backbone.View.extend({
		events: {
			'submit #baseline_creation_form': 'onSubmitForm',
			'hidden #baseline_creation_modal': 'onHidden'
		},

		initialize: function () {
			_.bindAll(this);
			this.isProduct = false;
			if (this.options && this.options.type) {
				this.isProduct = this.options.type === 'RELEASED' || this.options.type === 'LATEST' || this.options.type === 'PRODUCT';
			} else if (!this.collection) {
				this.collection = new Baselines({}, {type: 'document'});
			}
		},

		render: function () {
			var data = {
				i18n: App.config.i18n,
				isProduct: this.isProduct
			};
			if (this.isProduct) {
				data.isReleased = this.options.type === 'RELEASED';
				data.isLatest = this.options.type === 'LATEST';
			}
			this.$el.html(Mustache.render(template, data));
			this.bindDomElements();
			if (this.isProduct) {
				this.$inputBaselineType.val(this.options.type);
			}
            this.$inputBaselineName.customValidity(App.config.i18n.REQUIRED_FIELD);
            return this;
		},

		bindDomElements: function () {
			this.$modal = this.$('#baseline_creation_modal');
            this.$notifications = this.$el.find('.notifications').first();
			this.$inputBaselineName = this.$('#inputBaselineName');
			this.$inputBaselineDescription = this.$('#inputBaselineDescription');
            this.$submitButton = this.$('button.btn-primary').first();
			if (this.isProduct) {
				this.$inputBaselineType = this.$('#inputBaselineType');
			}
		},

		onSubmitForm: function (e) {
            this.$submitButton.attr('disabled', 'disabled');
            var data = {
				name: this.$inputBaselineName.val(),
				description: this.$inputBaselineDescription.val()
			};
            var _this = this;
			var callbacks = {
				success: this.onBaselineCreated,
				error: function(error){
                    _this.onError(data,error);
                    _this.$submitButton.removeAttr('disabled');
                }
			};
			if (this.isProduct) {
				data.type = this.$inputBaselineType.val();
				this.model.createBaseline(data, callbacks);
			} else {
				var baselinesCollection = this.collection;
				baselinesCollection.create(data, callbacks);
			}
			e.preventDefault();
			e.stopPropagation();
			return false;
		},

		onBaselineCreated: function (e) {
            if (typeof(e) === 'string') {
                this.trigger('warning',e);
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

	return SnapLatestBaselineView;
});

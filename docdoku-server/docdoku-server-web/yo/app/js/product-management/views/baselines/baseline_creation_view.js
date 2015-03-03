/*global _,define,App*/
define([
	'backbone',
	'mustache',
	'common-objects/collections/baselines',
	'models/configuration_item',
	'text!templates/baselines/baseline_creation_view.html',
    'common-objects/views/alert'
], function (Backbone, Mustache, Baselines, ConfigurationItem, template, AlertView) {

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

			var data = {
				i18n: App.config.i18n,
                isReleased : this.options.type === 'RELEASED',
                isLatest : this.options.type === 'LATEST',
                model:this.model
			};

			this.$el.html(Mustache.render(template, data));
			this.bindDomElements();
			this.$inputBaselineType.val(this.options.type);

            if(this.$inputConfigurationItem){
                this.$inputConfigurationItem.customValidity(App.config.i18n.REQUIRED_FIELD);

                this.$inputConfigurationItem.typeahead({
                    source: function (query, process) {
                        $.getJSON(App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/products', function (data) {
                            var ids = [];
                            _(data).each(function (d) {
                                ids.push(d.id);
                            });
                            process(ids);
                        });
                    }
                });

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
            this.$inputBaselineType = this.$('#inputBaselineType');
            this.$inputConfigurationItem = this.$('#inputConfigurationItem');
		},

		onSubmitForm: function (e) {

            if(!this.model){
                this.model = new ConfigurationItem({id:this.$inputConfigurationItem.val()});
            }

            this.$submitButton.attr('disabled', 'disabled');

            var data = {
				name: this.$inputBaselineName.val(),
				description: this.$inputBaselineDescription.val()
			};
            if(data.name.trim()){
                var _this = this;
                var callbacks = {
                    success: this.onBaselineCreated,
                    error: function(error){
                        _this.onError(data,error);
                        _this.$submitButton.removeAttr('disabled');
                    }
                };

                data.type = this.$inputBaselineType.val();

                this.model.createBaseline(data, callbacks);

            }else{
                this.$submitButton.removeAttr('disabled');
            }
			e.preventDefault();
			e.stopPropagation();
			return false;
		},

		onBaselineCreated: function (e) {
            if (e.message) {
                this.trigger('warning', e.message);
            }
            if(this.collection){
                e.productBaseline.configurationItemId = this.model.getId();
                this.collection.add(e.productBaseline)
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

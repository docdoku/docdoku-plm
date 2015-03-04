/*global _,define,App*/
define([
	'backbone',
	'mustache',
	'common-objects/collections/baselines',
	'models/configuration_item',
	'text!templates/baselines/baseline_creation_view.html',
    'common-objects/views/alert',
    'views/baselines/baselined_part_list',
    'common-objects/models/product_baseline'
], function (Backbone, Mustache, Baselines, ConfigurationItem, template, AlertView, BaselinePartListView, ProductBaseline) {

    'use strict';

	var BaselineCreationView = Backbone.View.extend({

		events: {
			'change #inputConfigurationItem': 'onProductChange',
			'submit #baseline_creation_form': 'onSubmitForm',
			'hidden #baseline_creation_modal': 'onHidden',
            'change select#inputBaselineType':'changeBaselineType',
            'close-request':'closeModal'
        },

		initialize: function () {
			_.bindAll(this);
		},

		render: function () {

			this.$el.html(Mustache.render(template, {
                i18n: App.config.i18n,
                model:this.model
            }));

			this.bindDomElements();
            this.hideLoader();
            this.bindTypeAhead();

            this.$inputBaselineName.customValidity(App.config.i18n.REQUIRED_FIELD);
            return this;
		},

        onProductChange:function(){
            this.model.set('id',this.$inputConfigurationItem.val());
            this.$inputBaselineType.val("LATEST").trigger('change');
            this.$inputBaselineType.prop("disabled",!this.model.getId());

        },

		bindDomElements: function () {
			this.$modal = this.$('#baseline_creation_modal');
            this.$notifications = this.$el.find('.notifications').first();
			this.$inputBaselineName = this.$('#inputBaselineName');
			this.$inputBaselineDescription = this.$('#inputBaselineDescription');
            this.$submitButton = this.$('button.btn-primary').first();
            this.$inputBaselineType = this.$('#inputBaselineType');
            this.$inputConfigurationItem = this.$('#inputConfigurationItem');
            this.$baselinedPartListArea = this.$('.baselinedPartListArea');
            this.$loader = this.$('.loader');
            this.$resolveBaselineParts = this.$('#resolveBaselineParts');
		},

        bindTypeAhead:function(){
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
        },
        changeBaselineType:function(){
            var type = this.$inputBaselineType.val();
            if(type === 'RELEASED'){
                this.fillReleasedBaselinedParts();
            } else if (type === 'LATEST'){
                this.fillLatestBaselinedParts();
            } else {
                this.fillConfigurationBaselinedParts();
            }
        },

        fillLatestBaselinedParts:function(){
            this.productBaseline = null;
            if(this.baselinePartListView){
                this.baselinePartListView.remove();
                this.baselinePartListView = null;
            }
        },

        fillReleasedBaselinedParts:function(){
            this.showLoader();
            this.model.getReleasedParts().success(this.fillPartsResolutionView).error(this.showResolutionError);
        },

        fillConfigurationBaselinedParts:function(){},

        fillPartsResolutionView:function(partIterations){
            this.hideLoader();
            this.productBaseline = new ProductBaseline({
                baselinedParts:partIterations
            });
            this.baselinePartListView = new BaselinePartListView({
                model: this.productBaseline,
                editMode:true
            }).render();
            this.baselinePartListView.on('part-modal:open',this.closeModal.bind(this))
            this.$baselinedPartListArea.html(this.baselinePartListView.$el);
        },

        showResolutionError:function(xhr,type,message){
            this.$loader.hide();
            this.$resolveBaselineParts.append(message);
        },

        showLoader:function(){
            this.$loader.show();
        },
        hideLoader:function(){
            this.$loader.hide();
        },

		onSubmitForm: function (e) {

            if(!this.model){
                this.model = new ConfigurationItem({id:this.$inputConfigurationItem.val()});
            }

            this.$submitButton.attr('disabled', 'disabled');
            var baselinedParts = this.baselinePartListView ? this.baselinePartListView.getBaselinedParts() : [];
            var data = {
				name: this.$inputBaselineName.val(),
				description: this.$inputBaselineDescription.val(),
                baselinedParts:baselinedParts
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

            this.trigger('info',App.config.i18n.BASELINE_CREATED);

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

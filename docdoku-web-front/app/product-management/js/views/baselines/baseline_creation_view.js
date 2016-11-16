/*global _,define,App*/
define([
	'backbone',
	'mustache',
	'models/configuration_item',
    'common-objects/models/product_baseline',
	'collections/configuration_items',
    'collections/configurations',
    'text!templates/baselines/baseline_creation_view.html',
    'common-objects/views/alert',
    'views/baselines/baseline_choice_list',
    'views/baselines/baselined_part_list',
    'views/baselines/baseline_configuration_list'
], function (Backbone, Mustache, ConfigurationItem, ProductBaseline, ConfigurationItemCollection, ConfigurationCollection, template, AlertView, BaselineChoiceListView, BaselinedPartsView, BaselineConfigurationsView) {

    'use strict';

	var BaselineCreationView = Backbone.View.extend({

		events: {
			'change #inputConfigurationItem': 'onProductChange',
			'submit #baseline_creation_form': 'onSubmitForm',
			'click button[form=baseline_creation_form]': 'interceptSubmit',
			'hidden #baseline_creation_modal': 'onHidden',
            'change select#inputBaselineType':'changeBaselineType',
            'close-modal-request':'closeModal'
        },

		initialize: function () {
			_.bindAll(this);

            this.choiceView = new BaselineChoiceListView({removableItems:false}).render();
            this.baselinePartListView = new BaselinedPartsView({
                editMode:true
            }).render();
            this.productBaseline = new ProductBaseline();
            this.baselinePartListView.model = this.productBaseline;

            this.baselineConfigurationsView = new BaselineConfigurationsView().render();
            this.baselineConfigurationsView.on('configuration:changed',this.updateChoicesView);
		},

		render: function () {

			this.$el.html(Mustache.render(template, {
                i18n: App.config.i18n,
                model:this.model
            }));

			this.bindDomElements();
            this.hideLoader();
            this.fillProductSelect();

            this.$baselineChoicesListArea.html(this.choiceView.$el);
            this.$baselinedPartListArea.html(this.baselinePartListView.$el);
            this.$baselinedConfigurationListArea.html(this.baselineConfigurationsView.$el);

            this.$inputBaselineName.customValidity(App.config.i18n.REQUIRED_FIELD);

            this.changeBaselineType();
            return this;
		},

        onProductChange:function(){
            this.model.set('id',this.$inputConfigurationItem.val());
            this.$inputBaselineType.val('LATEST').trigger('change');
            this.$inputBaselineType.prop('disabled',!this.model.getId());

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
            this.$baselineChoicesListArea = this.$('.baselineChoicesListArea');
            this.$baselinedConfigurationListArea = this.$('.baselinedConfigurationListArea');
            this.$loader = this.$('.loader');
        },

        fillProductSelect:function(){
            if(this.$inputConfigurationItem){
                var _this = this;
                var products = new ConfigurationItemCollection();
                products.fetch().success(function(){
                    products.each(function(product){
                        _this.$inputConfigurationItem.append('<option value="'+product.getId()+'">'+product.getId()+'</option>');
                    });
                    _this.$inputConfigurationItem.trigger('change');
                });
            }
        },

        changeBaselineType:function(){
            var type = this.$inputBaselineType.val();
            this.resetViews();
            this.fetchChoices(type);
        },

        resetViews:function(){
            this.baselinePartListView.clear();
            this.choiceView.clear();
        },

        fetchChoices:function(type){
            if(this.model.getId()){
                if(type === 'RELEASED'){
                    this.showLoader();
                    this.model.getReleasedChoices().success(this.fillChoices).error(this.onRequestsError);
                    this.model.getReleasedParts().success(this.fillPartsResolutionView).error(this.onRequestsError);
                } else if (type === 'LATEST'){
                    this.showLoader();
                    this.model.getLatestChoices().success(this.fillChoices).error(this.onRequestsError);
                }
                this.model.getConfigurations().success(this.fillConfigurations);
            }
        },

        fillPartsResolutionView:function(partIterations){
            this.hideLoader();
            this.productBaseline.setBaselinedParts(partIterations);
            this.baselinePartListView.renderList();
        },

        fillChoices:function(pathChoices){
            this.hideLoader();
            this.choiceView.renderList(pathChoices);
        },

        fillConfigurations:function(configurations){
            this.hideLoader();
            this.baselineConfigurationsView.renderList(configurations);
        },

        onRequestsError:function(xhr,type,message){
            this.$loader.hide();
            this.$notifications.append(new AlertView({
                type:'error',
                message:message
            }).render().$el);
        },

        updateChoicesView:function(configuration){
            this.choiceView.updateFromConfiguration(configuration);
        },

        showLoader:function(){
            this.$loader.show();
        },
        hideLoader:function(){
            this.$loader.hide();
        },

        interceptSubmit:function(){
            this.isValid = !this.$('.tabs').invalidFormTabSwitcher();
        },

		onSubmitForm: function (e) {

            if(this.isValid){

                var optionalUsageLinks = [];
                var substituteLinks = [];

                _.each(this.choiceView.getChoices(),function(choice){
                    if(choice && choice.optional){
                        optionalUsageLinks.push(choice.path);
                    } else if(choice && choice.path){
                        substituteLinks.push(choice.path);
                    }
                });

                if(!this.model){
                    this.model = new ConfigurationItem({id:this.$inputConfigurationItem.val()});
                }

                this.$submitButton.attr('disabled', 'disabled');
                var baselinedParts = this.baselinePartListView.getBaselinedParts();
                var data = {
                    name: this.$inputBaselineName.val(),
                    description: this.$inputBaselineDescription.val(),
                    baselinedParts:baselinedParts,
                    substituteLinks:substituteLinks,
                    optionalUsageLinks:optionalUsageLinks,
                    configurationItemId:this.model.get('id')
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
            }

			e.preventDefault();
			e.stopPropagation();
			return false;
		},

		onBaselineCreated: function (model) {

            if (model.message) {
                this.trigger('warning', model.message);
            }

            this.trigger('info',App.config.i18n.BASELINE_CREATED);

            if(this.collection){
                model.configurationItemId = this.model.getId();
                this.collection.add(model);
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

/*global _,define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/configuration/configuration_creation_view.html',
    'models/configuration',
    'models/configuration_item',
    'collections/configuration_items',
    'views/baselines/baseline_choice_list',
    'common-objects/views/security/acl',
    'common-objects/views/alert'
], function (Backbone, Mustache, template, Configuration, ConfigurationItem, ConfigurationItemCollection,BaselineChoiceListView, ACLView, AlertView) {
    'use strict';
    var ConfigurationCreationView = Backbone.View.extend({

        events: {
            'change select#inputConfigurationItem': 'onProductChange',
            'change select#inputPSFilter':'changePSFilter',
            'click button[form=configuration_creation_form]': 'interceptSubmit',
            'submit #configuration_creation_form': 'onSubmitForm',
            'hidden #configuration_creation_modal': 'onHidden',
            'close-modal-request':'closeModal'
        },

        initialize: function () {
            _.bindAll(this);
            this.choiceView = new BaselineChoiceListView({removableItems:true}).render();
        },

        render: function () {
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n}));
            this.bindDomElements();

            this.$choicesListArea.html(this.choiceView.$el);

            if(this.model.getId()){
                this.onProductChange();
            }else{
                this.fillProductSelect();
            }

            this.workspaceMembershipsView = new ACLView({
                el: this.$('#acl-mapping'),
                editMode: true
            }).render();

            this.$('input[required]').customValidity(App.config.i18n.REQUIRED_FIELD);
            return this;
        },

        onProductChange:function(){
            this.model.set('id',this.$inputConfigurationItem.val());
            this.$inputPSFilter.val('LATEST').trigger('change');
            this.$inputPSFilter.prop('disabled',!this.model.getId());
        },

        changePSFilter:function(){
            var type = this.$inputPSFilter.val();
            this.resetViews();
            this.fetchChoices(type);
        },

        resetViews:function(){
            this.choiceView.clear();
        },

        bindDomElements: function () {
            this.$notifications = this.$el.find('.notifications').first();
            this.$modal = this.$('#configuration_creation_modal');
            this.$choicesListArea = this.$('.choicesListArea');
            this.$inputConfigurationItem = this.$('#inputConfigurationItem');
            this.$inputPSFilter = this.$('#inputPSFilter');
            this.$submitButton = this.$('button[form=configuration_creation_form]');
            this.$loader = this.$('.loader');
            this.$inputName = this.$('#inputName');
            this.$inputDescription = this.$('#inputDescription');
        },

        fillProductSelect:function(){
            if(this.$inputConfigurationItem){
                var _this = this;
                var products = new ConfigurationItemCollection();
                products.fetch().success(function(){
                    _this.hideLoader();
                    products.each(function(product){
                        _this.$inputConfigurationItem.append('<option value="'+product.getId()+'">'+product.getId()+'</option>');
                    });
                    _this.onProductChange();
                }).error(this.onError.bind(this));
            }
        },

        fetchChoices:function(type){
            if(this.model.getId()){
                if(type === 'RELEASED'){
                    this.showLoader();
                    this.model.getReleasedChoices().success(this.fillChoices).error(this.onRequestsError);
                } else if (type === 'LATEST'){
                    this.showLoader();
                    this.model.getLatestChoices().success(this.fillChoices).error(this.onRequestsError);
                }
            }
        },

        fillChoices:function(pathChoices){
            this.hideLoader();
            this.choiceView.renderList(pathChoices);
        },

        onRequestsError:function(xhr,type,message){
            this.hideLoader();
            this.$notifications.append(new AlertView({
                type:'error',
                message:message
            }).render().$el);
        },

        interceptSubmit:function(){
            this.isValid = !this.$('.tabs').invalidFormTabSwitcher();
        },

        onSubmitForm: function (e) {

            var optionalUsageLinks = [];
            var substituteLinks = [];

            _.each(this.choiceView.getChoices(),function(choice){
                if(choice && choice.optional){
                    optionalUsageLinks.push(choice.path);
                } else if(choice && choice.path){
                    substituteLinks.push(choice.path);
                }
            });

            if(!optionalUsageLinks.length && !substituteLinks.length){
                this.$notifications.append(new AlertView({
                    type: 'error',
                    message: App.config.i18n.EMPTY_CHOICES
                }).render().$el);
            }else{

                if(!this.model){
                    this.model = new ConfigurationItem({id:this.$inputConfigurationItem.val()});
                }

                this.$submitButton.attr('disabled', 'disabled');

                var data = {
                    name: this.$inputName.val(),
                    description: this.$inputDescription.val(),
                    substituteLinks:substituteLinks,
                    optionalUsageLinks:optionalUsageLinks,
                    acl: this.workspaceMembershipsView.toList(),
                    configurationItemId:this.model.get('id')
                };

                this.model.createConfiguration(data).success(this.onConfigurationCreated.bind(this)).error(this.onError.bind(this));

            }

            e.preventDefault();
            e.stopPropagation();
            return false;
        },

        onConfigurationCreated: function (model) {

            if (model.message) {
                this.trigger('warning', model.message);
            }

            this.trigger('info', App.config.i18n.CONFIGURATION_CREATED);

            if (this.collection) {
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

        showLoader:function(){
            this.$loader.show();
        },
        hideLoader:function(){
            this.$loader.hide();
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

    return ConfigurationCreationView;

});

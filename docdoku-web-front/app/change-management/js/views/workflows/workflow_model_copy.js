/*global define,App,_*/
define([
    'backbone',
    'mustache',
    'require',
    'text!templates/workflows/workflow_model_copy.html',
    'common-objects/views/alert'
], function (Backbone, Mustache, require, template,AlertView) {
	'use strict';
    var WorkflowModelCopyView = Backbone.View.extend({

        events: {
            'click #save-copy-workflow-btn': 'saveCopyAction',
            'click #cancel-copy-workflow-btn': 'closeModalAction',
            'click a.close': 'closeModalAction',
            'hidden #modal-copy-workflow': 'onHidden',
            'shown #modal-copy-workflow':'onShown',
            'input #workflow-copy-name':'updateName'
        },

        initialize: function () {
            _.bindAll(this);
        },

        render: function () {
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n, workflow: this.model.attributes}));
            this.bindDomElements();
            return this;
        },

        bindDomElements: function () {
            this.inputWorkflowCopyName = this.$('#workflow-copy-name');
            this.submitButton = this.$('#save-copy-workflow-btn');
            this.$modal = this.$('#modal-copy-workflow');
            this.$notifications = this.$('.notifications');
        },

        updateName:function(e){
            if(e.target.value){
                this.submitButton.prop('disabled', false);
            }else{
                this.submitButton.prop('disabled',true);
            }
        },

        saveCopyAction: function () {
            var self = this;
            var reference = this.inputWorkflowCopyName.val();
            var originalName = this.model.get('reference');

            if (originalName !== reference && reference) {

                var copy = this.model.clone();
                delete copy.id;

                copy.save({
                        reference: reference,
                        finalLifeCycleState: copy.get('finalLifeCycleState')
                    },{
                        success: function () {
                            self.model = copy;
                            self.closeModalAction();
                            self.goToWorkflows();
                        },
                        error: function (model, xhr) {

                            var errorMessage = xhr ? xhr.responseText : model;
                            self.$notifications.append(new AlertView({
                                type: 'error',
                                message: errorMessage
                            }).render().$el);

                            self.inputWorkflowCopyName.focus();
                        }
                    }
                );
            }
        },

        openModal: function () {
            this.$modal.modal('show');
        },

        closeModalAction: function () {
            this.$modal.modal('hide');
        },

        onShown:function(){
            var input = this.inputWorkflowCopyName;
            input.focus();
            var tmpStr = input.val();
            input.val('');
            input.val(tmpStr);
        },

        onHidden:function(){
            this.remove();
        },

	    goToWorkflows: function () {
            App.router.navigate(App.config.workspaceId + '/workflows', {trigger: true});
        }

    });

    return WorkflowModelCopyView;

});

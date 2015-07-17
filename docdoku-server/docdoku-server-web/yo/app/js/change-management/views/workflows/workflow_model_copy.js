/*global define,App,_*/
define([
    'backbone',
    'mustache',
    'require',
    'text!templates/workflows/workflow_model_copy.html'
], function (Backbone, Mustache, require, template) {
	'use strict';
    var WorkflowModelCopyView = Backbone.View.extend({

        events: {
            'click #save-copy-workflow-btn': 'saveCopyAction',
            'click #cancel-copy-workflow-btn': 'closeModalAction',
            'click a.close': 'closeModalAction',
            'hidden #modal-copy-workflow': 'onHidden'
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
            this.$modal = this.$('#modal-copy-workflow');
        },

        saveCopyAction: function () {
            var self = this;
            var reference = this.inputWorkflowCopyName.val();

            if (reference !== null && reference !== '') {
                delete this.model.id;
                this.model.save(
                    {
                        reference: reference,
                        finalLifeCycleState: self.model.get('finalLifeCycleState')
                    },
                    {
                        success: function () {
                            self.closeModalAction();
                            self.goToWorkflows();
                        },
                        error: function (model, xhr) {
                            console.error('Error while saving workflow "' + model.attributes.reference + '" : ' + xhr.responseText);
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

        onHidden:function(){
            this.remove();
        },

	    goToWorkflows: function () {
            App.router.navigate(App.config.workspaceId + '/workflows', {trigger: true});
        }

    });

    return WorkflowModelCopyView;

});

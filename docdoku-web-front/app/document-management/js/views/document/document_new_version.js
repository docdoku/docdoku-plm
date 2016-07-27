/*global define,App,_*/
define([
    'backbone',
    'mustache',
    'text!templates/document/document_new_version.html',
    'common-objects/views/workflow/workflow_mapping',
    'common-objects/views/workflow/workflow_list',
    'common-objects/views/security/acl_clone_edit',
    'common-objects/views/alert'
], function (Backbone, Mustache, template, DocumentWorkflowMappingView, DocumentWorkflowListView, ACLView, AlertView) {

    'use strict';

    var DocumentsNewVersionView = Backbone.View.extend({

        events: {
            'click #create-new-version-btn': 'createNewVersionAction',
            'click #cancel-new-version-btn': 'closeModalAction',
            'click a.close': 'closeModalAction',
            'hidden #new-version-modal':'onHidden'
        },

        initialize:function(){
            _.bindAll(this);
        },

        render: function () {

            this.template = Mustache.render(template, {i18n: App.config.i18n, document: this.model.attributes});

            this.$el.html(this.template);

            this.bindDomElements();

            this.workflowsView = new DocumentWorkflowListView();
            this.newVersionWorkflowDiv.html(this.workflowsView.el);

            this.workflowsMappingView = new DocumentWorkflowMappingView({
                el: this.$('#workflows-mapping')
            });

            this.workflowsView.on('workflow:change', this.workflowsMappingView.updateMapping);

            this.aclView = new ACLView({
                el: this.$('#acl-mapping'),
                editMode: true,
                acl: this.model.get('acl')
            }).render();

            this.$('.tabs').tabs();

            return this;
        },

        bindDomElements: function () {
            this.newVersionWorkflowDiv = this.$('#new-version-workflow');
            this.inputNewVersionTitle = this.$('#new-version-title');
            this.textAreaNewVersionDescription = this.$('#new-version-description');
            this.$modal = this.$('#new-version-modal');
            this.$notifications = this.$('.notifications');
        },

        createNewVersionAction: function () {
            if(this.workflowsMappingView.isValid()){
                this.model.createNewVersion(this.inputNewVersionTitle.val(), this.textAreaNewVersionDescription.val(), this.workflowsView.selected(), this.workflowsMappingView.toResolvedList(), this.aclView.toList(), this.closeModalAction, this.onError);
            }else{
                this.$('.tabs').find('li:eq(2) a').tab('show');
            }

        },

        openModal:function(){
            this.$modal.modal('show');
        },

        closeModalAction: function () {
            this.$modal.modal('hide');
        },

        onHidden:function(){
            this.remove();
        },

        onError: function(model) {
            this.$notifications.append(new AlertView({
                type: 'error',
                message: model.responseText
            }).render().$el);
        }

    });

    return DocumentsNewVersionView;

});

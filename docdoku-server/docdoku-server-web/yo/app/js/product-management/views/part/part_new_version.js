/*global define,App,_*/
define([
    'backbone',
    'mustache',
    'text!templates/part/part_new_version.html',
    'common-objects/views/workflow/workflow_mapping',
    'common-objects/views/workflow/workflow_list',
    'common-objects/views/security/acl_clone_edit'
], function (Backbone, Mustache, template, WorkflowMappingView, WorkflowListView, ACLView) {
    'use strict';
    var PartNewVersionView = Backbone.View.extend({

        events: {
            'click #create-new-version-btn': 'createNewVersionAction',
            'click #cancel-new-version-btn': 'closeModalAction',
            'click a.close': 'closeModalAction',
            'hidden #new-version-modal':'onHidden'
        },

        initialize: function () {
            _.bindAll(this);
        },

        render: function () {

            this.template = Mustache.render(template, {i18n: App.config.i18n, model: this.model});

            this.$el.html(this.template);

            this.bindDomElements();

            this.workflowsView = new WorkflowListView();
            this.newVersionWorkflowDiv.html(this.workflowsView.el);

            this.workflowsMappingView = new WorkflowMappingView({
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
            this.textAreaNewVersionDescription = this.$('#new-version-description');
            this.$modal = this.$('#new-version-modal');
        },

        createNewVersionAction: function () {
            this.model.createNewVersion(this.textAreaNewVersionDescription.val(), this.workflowsView.selected(),
                this.workflowsMappingView.toList(), this.aclView.toList());
            this.closeModalAction();
        },

        openModal: function(){
            this.$modal.modal('show');
        },

        closeModalAction: function () {
            this.$modal.modal('hide');
        },

        onHidden: function(){
            this.remove();
        }

    });

    return PartNewVersionView;

});

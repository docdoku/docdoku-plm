/*global _,define,App*/
define([
    'backbone',
    'mustache',
    'collections/milestone_collection',
    'text!templates/milestones/milestone_content.html',
    'views/milestones/milestone_list',
    'views/milestones/milestone_creation',
    'common-objects/views/security/acl_edit',
    'text!common-objects/templates/buttons/delete_button.html',
    'text!common-objects/templates/buttons/ACL_button.html',
    'common-objects/views/alert'
], function (Backbone, Mustache, MilestoneCollection, template, MilestoneListView, MilestoneCreationView, ACLEditView, deleteButton, aclButton, AlertView) {
	'use strict';
	var MilestoneContentView = Backbone.View.extend({
        events: {
            'click button.new-milestone': 'newMilestone',
            'click button.delete': 'deleteMilestone',
            'click button.edit-acl': 'actionEditAcl'
        },

        partials: {
            deleteButton: deleteButton,
            aclButton: aclButton
        },

        initialize: function () {
            _.bindAll(this);
            this.collection = new MilestoneCollection();
        },

        render: function () {
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n}, this.partials));

            this.bindDomElements();

            this.listView = new MilestoneListView({
                el: this.$('#milestone_table'),
                collection: this.collection
            }).render();

            this.listView.on('delete-button:display', this.changeDeleteButtonDisplay);
            this.listView.on('acl-button:display', this.changeAclButtonDisplay);
            this.listView.on('error', this.onError);

            return this;
        },

        bindDomElements: function () {
            this.deleteButton = this.$('.delete');
            this.aclButton = this.$('.edit-acl');
            this.$notifications = this.$('.notifications');
        },

        newMilestone: function () {
            var self = this;
            var milestoneCreationView = new MilestoneCreationView({
                collection: self.collection
            });
            window.document.body.appendChild(milestoneCreationView.render().el);
            milestoneCreationView.openModal();
        },

        deleteMilestone: function () {
            this.listView.deleteSelectedMilestones();
        },

        actionEditAcl: function () {
            var modelChecked = this.listView.getChecked();

            if (modelChecked) {
                var self = this;
                modelChecked.fetch();
                var aclEditView = new ACLEditView({
                    editMode: true,
                    acl: modelChecked.getACL()
                });

                aclEditView.setTitle(modelChecked.getTitle());
                window.document.body.appendChild(aclEditView.render().el);

                aclEditView.openModal();
                aclEditView.on('acl:update', function () {
                    var acl = aclEditView.toList();
                    modelChecked.updateACL({
                        acl: acl || {userEntries: {}, groupEntries: {}},
                        success: function () {
                            modelChecked.set('acl', acl);
                            aclEditView.closeModal();
                            self.listView.redraw();
                        },
                        error: function(model, error){
                            aclEditView.onError(model, error);
                        }
                    });

                });
            }
        },

        changeDeleteButtonDisplay: function (state) {
            if (state) {
                this.deleteButton.show();
            } else {
                this.deleteButton.hide();
            }
        },

        changeAclButtonDisplay: function (state) {
            if (state) {
                this.aclButton.show();
            } else {
                this.aclButton.hide();
            }
        },

        onError: function(model, error) {
            var errorMessage = error ? error.responseText : model;
            this.$notifications.append(new AlertView({
                type: 'error',
                message: errorMessage
            }).render().$el);
        }

    });

    return MilestoneContentView;
});

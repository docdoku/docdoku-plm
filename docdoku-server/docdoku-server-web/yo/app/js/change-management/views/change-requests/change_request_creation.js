/*global _,define,App,window*/
define([
    'backbone',
    'mustache',
    'text!templates/change-requests/change_request_creation.html',
    'models/change_request',
    'common-objects/collections/users',
    'collections/milestone_collection',
    'common-objects/views/linked/linked_documents',
    'common-objects/collections/linked/linked_document_collection',
    'common-objects/views/linked/linked_parts',
    'common-objects/collections/linked/linked_part_collection',
    'common-objects/views/linked/linked_issues',
    'common-objects/collections/linked/linked_change_item_collection'
], function (Backbone, Mustache, template, ChangeRequestModel, UserList, MilestoneList, LinkedDocumentsView, LinkedDocumentCollection, LinkedPartsView, LinkedPartCollection, LinkedIssuesView, LinkedChangeItemCollection) {
    'use strict';
    var ChangeRequestCreationView = Backbone.View.extend({
        events: {
            'click .modal-footer .btn-primary': 'interceptSubmit',
            'submit #request_creation_form': 'onSubmitForm',
            'hidden #request_creation_modal': 'onHidden',
            'close-modal-request':'closeModal'
        },

        initialize: function () {
            this._subViews = [];
            this.model = new ChangeRequestModel();
            _.bindAll(this);
            this.$el.on('remove', this.removeSubviews);                                                                  // Remove cascade
        },

        removeSubviews: function () {
            _(this._subViews).invoke('remove');
        },

        render: function () {
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n}));
            this.bindDomElements();
            new UserList().fetch({success: this.fillUserList});
            new MilestoneList().fetch({success: this.fillMilestoneList});
            this.fillPriorityList();
            this.fillCategoryList();
            this.linkManagement();
            this.$inputRequestName.customValidity(App.config.i18n.REQUIRED_FIELD);

            return this;
        },

        fillMilestoneList: function (list) {
            var self = this;
            if (list) {
                list.each(function (milestone) {
                    self.$inputRequestMilestone.append('<option value="' + milestone.get('id') + '" ' + '>' + milestone.get('title') + '</option>');
                });
            }
        },
        fillUserList: function (list) {
            var self = this;
            if (list) {
                list.each(function (user) {
                    self.$inputRequestAssignee.append('<option value="' + user.get('login') + '" ' + '>' + user.get('name') + '</option>');
                });
            }
        },
        fillPriorityList: function () {
            var self = this;
            _.each(this.model.priorities, function(priority){
                self.$inputRequestPriority.append('<option value="' + priority + '" ' + '>' + priority + '</option>');
            });
        },
        fillCategoryList: function () {
            var self = this;
            _.each(this.model.categories, function(category){
                self.$inputRequestCategory.append('<option value="' + category + '" ' + '>' + category + '</option>');
            });
        },

        linkManagement: function () {
            var that = this;
            var $affectedDocumentsLinkZone = this.$('#documents-affected-links');

            that._affectedDocumentsCollection = new LinkedDocumentCollection();
            that._linkedDocumentsView = new LinkedDocumentsView({
                editMode: true,
                commentEditable:false,
                collection: that._affectedDocumentsCollection
            }).render();

            that._subViews.push(that._linkedDocumentsView);
            $affectedDocumentsLinkZone.html(that._linkedDocumentsView.el);

            var $affectedPartsLinkZone = this.$('#parts-affected-links');

            that._affectedPartsCollection = new LinkedPartCollection();
            that._linkedPartsView = new LinkedPartsView({
                editMode: true,
                collection: that._affectedPartsCollection
            }).render();

            that._subViews.push(that._linkedPartsView);
            $affectedPartsLinkZone.html(that._linkedPartsView.el);

            var $affectedIssuesLinkZone = this.$('#issues-affected-links');

            that._affectedIssuesCollection = new LinkedChangeItemCollection();
            var linkedIssuesView = new LinkedIssuesView({
                editMode: true,
                collection: that._affectedIssuesCollection,
                linkedPartsView: that._linkedPartsView,
                linkedDocumentsView: that._linkedDocumentsView
            }).render();

            that._subViews.push(linkedIssuesView);
            $affectedIssuesLinkZone.html(linkedIssuesView.el);

        },

        bindDomElements: function () {
            this.$modal = this.$('#request_creation_modal');
            this.$inputRequestName = this.$('#inputRequestName');
            this.$inputRequestDescription = this.$('#inputRequestDescription');
            this.$inputRequestMilestone = this.$('#inputRequestMilestone');
            this.$inputRequestPriority = this.$('#inputRequestPriority');
            this.$inputRequestAssignee = this.$('#inputRequestAssignee');
            this.$inputRequestCategory = this.$('#inputRequestCategory');
        },

        interceptSubmit : function(){
            this.isValid = ! this.$('.tabs').invalidFormTabSwitcher();
        },

        onSubmitForm: function (e) {

            if(this.isValid){
                var data = {
                    name: this.$inputRequestName.val(),
                    description: this.$inputRequestDescription.val(),
                    author: App.config.login,
                    assignee: this.$inputRequestAssignee.val(),
                    priority: this.$inputRequestPriority.val(),
                    category: this.$inputRequestCategory.val(),
                    milestoneId: parseInt(this.$inputRequestMilestone.val(), 10)
                };

                this.model.save(data, {
                    success: this.onRequestCreated,
                    error: this.error,
                    wait: true
                });
            }

            e.preventDefault();
            e.stopPropagation();
            return false;
        },

        onRequestCreated: function (model) {
            this.collection.push(model);
            this.updateAffectedDocuments(model);
            this.updateAffectedParts(model);
            this.updateAffectedIssues(model);
            this.closeModal();
        },

        onError: function (model, error) {
            window.alert(App.config.i18n.CREATION_ERROR + ' : ' + error.responseText);
        },

        openModal: function () {
            this.$modal.modal('show');
        },

        closeModal: function () {
            this.$modal.modal('hide');
        },

        onHidden: function () {
            this.remove();
        },

        updateAffectedDocuments: function (model) {
            if (this._affectedDocumentsCollection.length) {
                model.saveAffectedDocuments(this._affectedDocumentsCollection);
            }
        },

        updateAffectedParts: function (model) {
            if (this._affectedPartsCollection.length) {
                model.saveAffectedParts(this._affectedPartsCollection);
            }
        },

        updateAffectedIssues: function (model) {
            if (this._affectedIssuesCollection.length) {
                model.saveAffectedIssues(this._affectedIssuesCollection);
            }
        }
    });

    return ChangeRequestCreationView;
});

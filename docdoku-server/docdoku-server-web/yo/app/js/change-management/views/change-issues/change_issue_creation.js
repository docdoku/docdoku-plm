/*global _,define,App,window*/
define([
    'backbone',
    'mustache',
    'text!templates/change-issues/change_issue_creation.html',
    'models/change_issue',
    'common-objects/collections/users',
    'common-objects/views/linked/linked_documents',
    'common-objects/collections/linked/linked_document_collection',
    'common-objects/views/linked/linked_parts',
    'common-objects/collections/linked/linked_part_collection'
],
function (Backbone, Mustache, template, ChangeIssueModel, UserList, LinkedDocumentsView, LinkedDocumentCollection, LinkedPartsView, LinkedPartCollection) {
    'use strict';
    var ChangeIssueCreationView = Backbone.View.extend({
        events: {
            'click .modal-footer .btn-primary': 'interceptSubmit',
            'submit #issue_creation_form': 'onSubmitForm',
            'hidden #issue_creation_modal': 'onHidden',
            'close-modal-request':'closeModal'
        },

        initialize: function () {
            this._subViews = [];
            this.model = new ChangeIssueModel();
            _.bindAll(this);
            this.$el.on('remove', this.removeSubviews);                                                                 // Remove cascade
        },

        removeSubviews: function () {
            _(this._subViews).invoke('remove');
        },

        render: function () {
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n}));
            this.bindDomElements();
            new UserList().fetch({success: this.fillUserList});
            this.fillPriorityList();
            this.fillCategoryList();
            this.linkManagement();

            this.$inputIssueName.customValidity(App.config.i18n.REQUIRED_FIELD);

            return this;
        },

        fillUserList: function (list) {
            var self = this;
            list.each(function (user) {
                self.$inputIssueAssignee.append('<option value="' + user.get('login') + '" ' + '>' + user.get('name') + '</option>');
            });
        },
        fillPriorityList: function () {
	        var self = this;
	        _.each(this.model.priorities, function(priority){
		        self.$inputIssuePriority.append('<option value="' + priority + '" ' + '>' + priority + '</option>');
	        });
        },
        fillCategoryList: function () {
	        var self = this;
	        _.each(this.model.categories, function(category){
		        self.$inputIssueCategory.append('<option value="' + category + '" ' + '>' + category + '</option>');
	        });
        },

        linkManagement: function () {
            var that = this;
            var $affectedDocumentsLinkZone = this.$('#documents-affected-links');

            that._affectedDocumentsCollection = new LinkedDocumentCollection();
            var linkedDocumentsView = new LinkedDocumentsView({
                editMode: true,
                commentEditable: false,
                collection: that._affectedDocumentsCollection
            }).render();

            that._subViews.push(linkedDocumentsView);
            $affectedDocumentsLinkZone.html(linkedDocumentsView.el);

            var $affectedPartsLinkZone = this.$('#parts-affected-links');

            that._affectedPartsCollection = new LinkedPartCollection();
            var linkedPartsView = new LinkedPartsView({
                editMode: true,
                collection: that._affectedPartsCollection
            }).render();

            that._subViews.push(linkedPartsView);
            $affectedPartsLinkZone.html(linkedPartsView.el);

        },

        bindDomElements: function () {
            this.$modal = this.$('#issue_creation_modal');
            this.$inputIssueName = this.$('#inputIssueName');
            this.$inputIssueDescription = this.$('#inputIssueDescription');
            this.$inputIssuePriority = this.$('#inputIssuePriority');
            this.$inputIssueAssignee = this.$('#inputIssueAssignee');
            this.$inputIssueCategory = this.$('#inputIssueCategory');
            this.$inputIssueInitiator = this.$('#inputIssueInitiator');
        },

        interceptSubmit : function(){
            this.isValid = ! this.$('.tabs').invalidFormTabSwitcher();
        },

        onSubmitForm: function (e) {

            if(this.isValid){
                var data = {
                    name: this.$inputIssueName.val(),
                    description: this.$inputIssueDescription.val(),
                    author: App.config.login,
                    assignee: this.$inputIssueAssignee.val(),
                    priority: this.$inputIssuePriority.val(),
                    category: this.$inputIssueCategory.val(),
                    initiator: this.$inputIssueInitiator.val()
                };

                this.model.save(data, {
                    success: this.onIssueCreated,
                    error: this.onError,
                    wait: true
                });
            }

            e.preventDefault();
            e.stopPropagation();
            return false;
        },

        onIssueCreated: function (model) {
            this.collection.push(model);
            this.updateAffectedDocuments(model);
            this.updateAffectedParts(model);
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
        }
    });

    return ChangeIssueCreationView;
});

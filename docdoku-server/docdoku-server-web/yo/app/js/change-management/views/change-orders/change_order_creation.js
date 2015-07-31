/*global _,define,App,window*/
define([
    'backbone',
    'mustache',
    'text!templates/change-orders/change_order_creation.html',
    'models/change_order',
    'common-objects/collections/users',
    'collections/milestone_collection',
    'common-objects/views/linked/linked_documents',
    'common-objects/collections/linked/linked_document_collection',
    'common-objects/views/linked/linked_parts',
    'common-objects/collections/linked/linked_part_collection',
	'common-objects/views/linked/linked_requests',
	'common-objects/collections/linked/linked_change_item_collection'
], function (Backbone, Mustache, template, ChangeOrderModel, UserList, MilestoneList, LinkedDocumentsView, LinkedDocumentCollection, LinkedPartsView, LinkedPartCollection,LinkedRequestsView,LinkedChangeItemCollection) {
    'use strict';
    var ChangeOrderCreationView = Backbone.View.extend({
        events: {
            'click .modal-footer .btn-primary': 'interceptSubmit',
            'submit #order_creation_form': 'onSubmitForm',
            'hidden #order_creation_modal': 'onHidden',
            'close-modal-request':'closeModal'
        },

        initialize: function () {
            this._subViews = [];
            this.model = new ChangeOrderModel();
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
            this.$inputOrderName.customValidity(App.config.i18n.REQUIRED_FIELD);
            return this;
        },

        fillMilestoneList: function (list) {
            var self = this;
            if (list) {
                list.each(function (milestone) {
                    self.$inputOrderMilestone.append('<option value="' + milestone.get('id') + '"' + '>' + milestone.get('title') + '</option>');
                });
            }
        },
        fillUserList: function (list) {
            var self = this;
            if (list) {
                list.each(function (user) {
                    self.$inputOrderAssignee.append('<option value="' + user.get('login') + '""' + '>' + user.get('name') + '</option>');
                });
            }
        },
        fillPriorityList: function () {
	        var self = this;
	        _.each(this.model.priorities, function(priority){
		        self.$inputOrderPriority.append('<option value="' + priority + '" ' + '>' + priority + '</option>');
	        });
        },
        fillCategoryList: function () {
	        var self = this;
	        _.each(this.model.categories, function(category){
		        self.$inputOrderCategory.append('<option value="' + category + '" ' + '>' + category + '</option>');
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

            var $affectedRequestsLinkZone = this.$('#requests-affected-links');

            that._affectedRequestsCollection = new LinkedChangeItemCollection();
            var linkedRequestsView = new LinkedRequestsView({
                editMode: true,
                collection: that._affectedRequestsCollection,
                linkedPartsView: that._linkedPartsView,
                linkedDocumentsView: that._linkedDocumentsView
            }).render();

            that._subViews.push(linkedRequestsView);
            $affectedRequestsLinkZone.html(linkedRequestsView.el);

        },

        bindDomElements: function () {
            this.$modal = this.$('#order_creation_modal');
            this.$inputOrderName = this.$('#inputOrderName');
            this.$inputOrderDescription = this.$('#inputOrderDescription');
            this.$inputOrderMilestone = this.$('#inputOrderMilestone');
            this.$inputOrderPriority = this.$('#inputOrderPriority');
            this.$inputOrderAssignee = this.$('#inputOrderAssignee');
            this.$inputOrderCategory = this.$('#inputOrderCategory');
        },

        interceptSubmit : function(){
            this.isValid = ! this.$('.tabs').invalidFormTabSwitcher();
        },

        onSubmitForm: function (e) {
            if(this.isValid){
                var data = {
                    name: this.$inputOrderName.val(),
                    description: this.$inputOrderDescription.val(),
                    author: App.config.login,
                    assignee: this.$inputOrderAssignee.val(),
                    priority: this.$inputOrderPriority.val(),
                    category: this.$inputOrderCategory.val(),
                    milestoneId: parseInt(this.$inputOrderMilestone.val(), 10)
                };

                this.model.save(data, {
                    success: this.onOrderCreated,
                    error: this.error,
                    wait: true
                });
            }
            e.preventDefault();
            e.stopPropagation();
            return false;
        },

        onOrderCreated: function (model) {
            this.collection.push(model);
            this.updateAffectedDocuments(model);
            this.updateAffectedParts(model);
            this.updateAffectedRequests(model);
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

        updateAffectedRequests: function (model) {
            if (this._affectedRequestsCollection.length) {
                model.saveAffectedRequests(this._affectedRequestsCollection);
            }
        }
    });

    return ChangeOrderCreationView;
});

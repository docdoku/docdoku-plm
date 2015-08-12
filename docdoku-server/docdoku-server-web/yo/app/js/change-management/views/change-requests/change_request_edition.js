/*global _,define,App,window*/
define([
        'backbone',
        'mustache',
        'text!templates/change-requests/change_request_edition.html',
        'models/change_request',
        'common-objects/collections/users',
        'collections/milestone_collection',
        'common-objects/utils/date',
        'common-objects/models/tag',
        'common-objects/views/tags/tag',
        'common-objects/views/linked/linked_documents',
        'common-objects/collections/linked/linked_document_collection',
        'common-objects/views/linked/linked_parts',
        'common-objects/collections/linked/linked_part_collection',
        'common-objects/views/linked/linked_issues',
        'common-objects/collections/linked/linked_change_item_collection'
    ],
    function (Backbone, Mustache, template, ChangeRequestModel, UserList, MilestoneList, Date, Tag, TagView, LinkedDocumentsView, LinkedDocumentCollection, LinkedPartsView, LinkedPartCollection, LinkedIssuesView, LinkedChangeItemCollection) {
	    'use strict';
        var ChangeRequestEditionView = Backbone.View.extend({
            events: {
                'submit #request_edition_form': 'onSubmitForm',
                'hidden #request_edition_modal': 'onHidden',
                'close-modal-request':'closeModal'
            },


            initialize: function () {
                this.tagsToRemove = [];
                this._subViews = [];
                _.bindAll(this);
                this.$el.on('remove', this.removeSubviews);                                                                  // Remove cascade
            },

            removeSubviews: function () {
                _(this._subViews).invoke('remove');
            },

            render: function () {
                this.removeSubviews();
                this.editMode = this.model.isWritable();
                this.$el.html(Mustache.render(template, {i18n: App.config.i18n, model: this.model}));
                this.bindDomElements();
                this.bindUserPopover();
                Date.dateHelper(this.$('.date-popover'));
                new UserList().fetch({success: this.fillUserList});
                new MilestoneList().fetch({success: this.fillMilestoneList});
                this.fillPriorityList();
                this.fillCategoryList();
                this.initValue();
                this.tagManagement();
                this.linkManagement();
                return this;
            },

            fillMilestoneList: function (list) {
                var self = this;
                if (list) {
                    list.each(function (milestone) {
                        self.$inputRequestMilestone.append('<option value="' + milestone.get('id') + '"' + '>' + milestone.get('title') + '</option>');
                    });
                }
                this.$inputRequestMilestone.val(this.model.getMilestoneId());
            },
            fillUserList: function (list) {
                var self = this;
                list.each(function (user) {
                    self.$inputRequestAssignee.append('<option value="' + user.get('login') + '"' + '>' + user.get('name') + '</option>');
                });
                this.$inputRequestAssignee.val(this.model.getAssignee());
            },
            fillPriorityList: function () {
	            var self = this;
	            _.each(this.model.priorities, function(priority){
		            self.$inputRequestPriority.append('<option value="' + priority + '" ' + '>' + priority + '</option>');
	            });
                this.$inputRequestPriority.val(this.model.getPriority());
            },
            fillCategoryList: function () {
	            var self = this;
	            _.each(this.model.categories, function(category){
		            self.$inputRequestCategory.append('<option value="' + category + '" ' + '>' + category + '</option>');
	            });
                this.$inputRequestCategory.val(this.model.getCategory());
            },

            tagManagement: function () {
                var that = this;

                if (this.model.attributes.tags.length) {

                    var $tagsZone = this.$('.master-tags-list');
                    _.each(that.model.attributes.tags, function (tagLabel) {
                        var tagView;
                        var tagViewParams = {
                            model: new Tag({id: tagLabel, label: tagLabel}),
                            isAdded: that.editMode,
                            clicked: function () {
                                that.tagsToRemove.push(tagLabel);
                                tagView.$el.remove();
                            }
                        };

                        tagView = new TagView(tagViewParams).render();
                        that._subViews.push(tagView);

                        $tagsZone.append(tagView.el);
                    });

                }
            },

            linkManagement: function () {
                var that = this;
                var affectedDocuments = this.model.getAffectedDocuments();
                var $affectedDocumentsLinkZone = this.$('#documents-affected-links');

                that._affectedDocumentsCollection = new LinkedDocumentCollection(affectedDocuments);
                that._linkedDocumentsView = new LinkedDocumentsView({
                    editMode: that.editMode,
                    commentEditable:false,
                    collection: that._affectedDocumentsCollection
                }).render();

                that._subViews.push(that._linkedDocumentsView);
                $affectedDocumentsLinkZone.html(that._linkedDocumentsView.el);

                var affectedParts = this.model.getAffectedParts();
                var $affectedPartsLinkZone = this.$('#parts-affected-links');

                that._affectedPartsCollection = new LinkedPartCollection(affectedParts);
                that._linkedPartsView = new LinkedPartsView({
                    editMode: that.editMode,
                    collection: that._affectedPartsCollection
                }).render();

                that._subViews.push(that._linkedPartsView);
                $affectedPartsLinkZone.html(that._linkedPartsView.el);

                var affectedIssues = this.model.getAddressedChangeIssues();
                var $affectedIssuesLinkZone = this.$('#issues-affected-links');

                that._affectedIssuesCollection = new LinkedChangeItemCollection(affectedIssues);
                var linkedIssuesView = new LinkedIssuesView({
                    editMode: that.editMode,
                    collection: that._affectedIssuesCollection,
                    linkedPartsView: that._linkedPartsView,
                    linkedDocumentsView: that._linkedDocumentsView
                }).render();

                that._subViews.push(linkedIssuesView);
                $affectedIssuesLinkZone.html(linkedIssuesView.el);

            },

            bindDomElements: function () {
                this.$modal = this.$('#request_edition_modal');
                this.$inputRequestName = this.$('#inputRequestName');
                this.$inputRequestDescription = this.$('#inputRequestDescription');
                this.$inputRequestMilestone = this.$('#inputRequestMilestone');
                this.$inputRequestPriority = this.$('#inputRequestPriority');
                this.$inputRequestAssignee = this.$('#inputRequestAssignee');
                this.$inputRequestCategory = this.$('#inputRequestCategory');
                this.$authorLink = this.$('.author-popover');
            },

            bindUserPopover: function () {
                this.$authorLink.userPopover(this.model.getAuthor(), this.model.getId(), 'right');
            },

            initValue: function () {
                this.$inputRequestName.val(this.model.getName());
                this.$inputRequestDescription.val(this.model.getDescription());
            },

            onSubmitForm: function (e) {
                var data = {
                    description: this.$inputRequestDescription.val(),
                    author: App.config.login,
                    assignee: this.$inputRequestAssignee.val(),
                    priority: this.$inputRequestPriority.val(),
                    category: this.$inputRequestCategory.val(),
                    milestoneId: parseInt(this.$inputRequestMilestone.val(), 10)
                };

                this.model.save(data, {
                    success: this.closeModal,
                    error: this.error,
                    wait: true
                });

                this.deleteClickedTags();                                                                                   // Delete tags if needed
                this.updateAffectedDocuments();
                this.updateAffectedParts();
                this.updateAffectedIssues();

                e.preventDefault();
                e.stopPropagation();
                return false;
            },

            onError: function (model, error) {
                window.alert(App.config.i18n.EDITION_ERROR + ' : ' + error.responseText);
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

            deleteClickedTags: function () {
                if (this.tagsToRemove.length) {
                    var that = this;
                    this.model.removeTags(this.tagsToRemove, function () {
                        if (that.model.collection.parent) {
                            if (_.contains(that.tagsToRemove, that.model.collection.parent.id)) {
                                that.model.collection.remove(that.model);
                            }
                        }
                    });
                }
            },

            updateAffectedDocuments: function () {
                this.model.saveAffectedDocuments(this._affectedDocumentsCollection);
            },

            updateAffectedParts: function () {
                this.model.saveAffectedParts(this._affectedPartsCollection);
            },

            updateAffectedIssues: function () {
                this.model.saveAffectedIssues(this._affectedIssuesCollection);
            }
        });

        return ChangeRequestEditionView;
    });

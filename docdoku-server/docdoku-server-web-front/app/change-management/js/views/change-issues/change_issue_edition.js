/*global _,define,App,window*/
define([
        'backbone',
        'mustache',
        'text!templates/change-issues/change_issue_edition.html',
        'common-objects/collections/users',
        'common-objects/utils/date',
        'common-objects/models/tag',
        'common-objects/views/tags/tag',
        'common-objects/views/linked/linked_documents',
        'common-objects/collections/linked/linked_document_collection',
        'common-objects/views/linked/linked_parts',
        'common-objects/collections/linked/linked_part_collection'
    ],
    function (Backbone, Mustache, template, UserList, Date, Tag, TagView, LinkedDocumentsView, LinkedDocumentCollection, LinkedPartsView, LinkedPartCollection) {
        'use strict';
        var ChangeIssueEditionView = Backbone.View.extend({
            events: {
                'submit #issue_edition_form': 'onSubmitForm',
                'hidden #issue_edition_modal': 'onHidden',
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
                this.fillPriorityList();
                this.fillCategoryList();
                this.initValue();
                this.tagManagement();
                this.linkManagement();
                return this;
            },

            fillUserList: function (list) {
                var self = this;
                list.each(function (user) {
                    self.$inputIssueAssignee.append('<option value="' + user.get('login') + '" ' + '>' + user.get('name') + '</option>');
                });
                this.$inputIssueAssignee.val(this.model.getAssignee());
            },
            fillPriorityList: function () {
                var self = this;
                _.each(this.model.priorities, function(priority){
                    self.$inputIssuePriority.append('<option value="' + priority + '" ' + '>' + priority + '</option>');
                });
                this.$inputIssuePriority.val(this.model.getPriority());
            },
            fillCategoryList: function () {
                var self = this;
                _.each(this.model.categories, function(category){
                    self.$inputIssueCategory.append('<option value="' + category + '" ' + '>' + category + '</option>');
                });
                this.$inputIssueCategory.val(this.model.getCategory());
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
                var linkedDocumentsView = new LinkedDocumentsView({
                    editMode: that.editMode,
                    commentEditable:false,
                    collection: that._affectedDocumentsCollection
                }).render();

                that._subViews.push(linkedDocumentsView);
                $affectedDocumentsLinkZone.html(linkedDocumentsView.el);

                var affectedParts = this.model.getAffectedParts();
                var $affectedPartsLinkZone = this.$('#parts-affected-links');

                that._affectedPartsCollection = new LinkedPartCollection(affectedParts);
                var linkedPartsView = new LinkedPartsView({
                    editMode: that.editMode,
                    collection: that._affectedPartsCollection
                }).render();

                that._subViews.push(linkedPartsView);
                $affectedPartsLinkZone.html(linkedPartsView.el);

            },

            bindDomElements: function () {
                this.$modal = this.$('#issue_edition_modal');
                this.$inputIssueName = this.$('#inputIssueName');
                this.$inputIssueInitiator = this.$('#inputIssueInitiator');
                this.$inputIssueDescription = this.$('#inputIssueDescription');
                this.$inputIssuePriority = this.$('#inputIssuePriority');
                this.$inputIssueAssignee = this.$('#inputIssueAssignee');
                this.$inputIssueCategory = this.$('#inputIssueCategory');
                this.$inputIssueInitiator = this.$('#inputIssueInitiator');
                this.$authorLink = this.$('.author-popover');
            },

            bindUserPopover: function () {
                this.$authorLink.userPopover(this.model.getAuthor(), this.model.getId(), 'right');
            },

            initValue: function () {
                this.$inputIssueName.val(this.model.getName());
                this.$inputIssueInitiator.val(this.model.getInitiator());
                this.$inputIssueDescription.val(this.model.getDescription());
            },

            onSubmitForm: function (e) {
                var data = {
                    description: this.$inputIssueDescription.val(),
                    author: App.config.login,
                    assignee: this.$inputIssueAssignee.val(),
                    priority: this.$inputIssuePriority.val(),
                    category: this.$inputIssueCategory.val(),
                    initiator: this.$inputIssueInitiator.val()
                };

                this.model.save(data, {
                    success: this.closeModal,
                    error: this.onError,
                    wait: true
                });

                this.deleteClickedTags();                                                                                   // Delete tags if needed
                this.updateAffectedDocuments();
                this.updateAffectedParts();

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
            }
        });

        return ChangeIssueEditionView;
    });

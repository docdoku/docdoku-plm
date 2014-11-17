/*global _,define,App*/
define([
    'mustache',
    'common-objects/views/components/modal',
    'common-objects/views/file/file_list',
    'common-objects/views/attributes/attributes',
    'common-objects/views/workflow/lifecycle',
    'common-objects/views/linked/linked_documents',
    'common-objects/models/tag',
    'common-objects/views/tags/tag',
    'common-objects/collections/linked/linked_document_collection',
    'text!templates/iteration/document_iteration.html',
    'common-objects/utils/date'
], function (Mustache, ModalView, FileListView, DocumentAttributesView, LifecycleView, LinkedDocumentsView, Tag, TagView, LinkedDocumentCollection, template, date) {
    'use strict';

    var IterationView = ModalView.extend({

        initialize: function () {

            this.iteration = this.model.getLastIteration();
            this.iterations = this.model.getIterations();

            ModalView.prototype.initialize.apply(this, arguments);

            this.events['click a#previous-iteration'] = 'onPreviousIteration';
            this.events['click a#next-iteration'] = 'onNextIteration';
            this.events['submit form'] = 'onSubmitForm';
            this.events['click a.document-path'] = 'onFolderPathClicked';

            this.tagsToRemove = [];

        },

        onPreviousIteration: function () {
            if (this.iterations.hasPreviousIteration(this.iteration)) {
                this.switchIteration(this.iterations.previous(this.iteration));
            }
            return false;
        },

        onNextIteration: function () {
            if (this.iterations.hasNextIteration(this.iteration)) {
                this.switchIteration(this.iterations.next(this.iteration));
            }
            return false;
        },

        switchIteration: function (iteration) {
            this.iteration = iteration;
            var activeTabIndex = this.getActiveTabIndex();
            this.render();
            this.activateTab(activeTabIndex);
        },

        getActiveTabIndex: function () {
            return this.tabs.filter('.active').index();
        },

        activateTab: function (index) {
            this.tabs.eq(index).children().tab('show');
        },

        validation: function () {

            /*checking attributes*/
            var ok = true;
            var attributes = this.attributesView.model;
            attributes.each(function (item) {
                if (!item.isValid()) {
                    ok = false;
                }
            });
            if (!ok) {
                this.getPrimaryButton().attr('disabled', 'disabled');
            } else {
                this.getPrimaryButton().removeAttr('disabled');
            }
        },

        render: function () {
            this.deleteSubViews();

            var editMode = this.model.isCheckoutByConnectedUser() && this.iterations.isLast(this.iteration);

            var data = {
                isCheckoutByConnectedUser: this.model.isCheckoutByConnectedUser(),
                isCheckout: this.model.isCheckout(),
                editMode: editMode,
                master: this.model.toJSON(),
                i18n: App.config.i18n,
                permalink: this.model.getPermalink(),
                hasIterations: this.iterations.length
            };

            data.master.creationDate = date.formatTimestamp(
                App.config.i18n._DATE_FORMAT,
                data.master.creationDate
            );


            var fullPath = data.master.path;
            var re = new RegExp(App.config.workspaceId, '');
            fullPath = fullPath.replace(re, '');
            this.folderPath = fullPath.replace(/\//g, ':');
            if (this.folderPath[0] === ':') {
                this.folderPath = this.folderPath.substr(1, this.folderPath.length);
            }


            if (this.model.hasIterations()) {
                var hasNextIteration = this.iterations.hasNextIteration(this.iteration);
                var hasPreviousIteration = this.iterations.hasPreviousIteration(this.iteration);
                data.iteration = this.iteration.toJSON();
                data.iteration.hasNextIteration = hasNextIteration;
                data.iteration.hasPreviousIteration = hasPreviousIteration;
                data.reference = this.iteration.getId();
                data.iteration.creationDate = date.formatTimestamp(
                    App.config.i18n._DATE_FORMAT,
                    data.iteration.creationDate
                );
            }

            if (this.model.isCheckout()) {
                data.master.checkOutDate = date.formatTimestamp(
                    App.config.i18n._DATE_FORMAT,
                    data.master.checkOutDate
                );
            }

            if(App.config.configSpec!=='latest'){
                data.isForBaseline=true;
            }

            /*Main window*/
            var html = Mustache.render(template, data);
            this.$el.html(html);

            this.tabs = this.$('.nav-tabs li');


            this.attributesView =
                this.addSubView(
                    new DocumentAttributesView({
                        el: '#iteration-additional-attributes-container'
                    })
                );

            this.attributesView.setAttributesLocked(this.model.isAttributesLocked());

            this.attributesView.setEditMode(editMode);
            this.attributesView.render();

            var that = this;

            if (this.model.hasIterations()) {
                this.iteration.getAttributes().each(function (item) {
                    that.attributesView.addAndFillAttribute(item);
                });

                this.fileListView = new FileListView({
                    baseName: this.iteration.getBaseName(),
                    deleteBaseUrl: this.iteration.baseUrl(),
                    uploadBaseUrl: this.iteration.getUploadBaseUrl(),
                    collection: this.iteration.getAttachedFiles(),
                    editMode: editMode
                }).render();

                /* Add the fileListView to the tab */
                this.$('#iteration-files').html(this.fileListView.el);


                this.linkedDocumentsView = new LinkedDocumentsView({
                    editMode: editMode,
                    documentIteration: this.iteration,
                    collection: new LinkedDocumentCollection(this.iteration.getLinkedDocuments())
                }).render();

                /* Add the documentLinksView to the tab */
                this.$('#iteration-links').html(this.linkedDocumentsView.el);
            }

            if (this.model.get('workflow')) {

                this.lifecycleView = new LifecycleView({
                    el: '#tab-iteration-lifecycle'
                }).setAbortedWorkflowsUrl(this.model.getAbortedWorkflowsUrl()).setWorkflow(this.model.get('workflow')).setEntityType('documents').render();

                this.lifecycleView.on('lifecycle:change', function () {
                    that.model.fetch({
                        success: function () {
                            that.lifecycleView.setAbortedWorkflowsUrl(that.model.getAbortedWorkflowsUrl()).setWorkflow(that.model.get('workflow')).setEntityType('documents').render();
                        }
                    });
                });

            } else {
                this.$('a[href=#tab-iteration-lifecycle]').hide();
            }

            this.$('.author-popover').userPopover(this.model.attributes.author.login, this.model.id, 'right');

            if (this.model.isCheckout()) {
                this.$('.checkout-user-popover').userPopover(this.model.getCheckoutUser().login, this.model.id, 'right');
            }

            this.tagsManagement(editMode);

            return this;
        },

        onSubmitForm: function (e) {

            /*saving iteration*/
            this.iteration.save({
                revisionNote: this.$('#inputRevisionNote').val(),
                instanceAttributes: this.attributesView.collection.toJSON(),
                linkedDocuments: this.linkedDocumentsView.collection.toJSON()
            });

            /*There is a parsing problem at saving time*/
            var files = this.iteration.get('attachedFiles');

            /*tracking back files*/
            this.iteration.set({
                attachedFiles: files
            });

            /*
             *saving new files : nothing to do : it's already saved
             *deleting unwanted files
             */
            this.fileListView.deleteFilesToDelete();

            /*
             * Delete tags if needed
             * */

            this.deleteClickedTags();


            this.hide();

            // prevent page reload when pressing enter
            e.preventDefault();
            e.stopPropagation();
            return false;
        },

        cancelAction: function () {

            if (this.model.hasIterations()) {
                //Abort file upload and delete new files
                this.fileListView.deleteNewFiles();
            }
            ModalView.prototype.cancelAction.call(this);
        },

        getPrimaryButton: function () {
            return this.$('div.modal-footer button.btn-primary');
        },

        tagsManagement: function (editMode) {

            var $tagsZone = this.$('.master-tags-list');
            var that = this;

            _.each(this.model.attributes.tags, function (tagLabel) {

                var tagView;

                var tagViewParams = editMode ?
                {
                    model: new Tag({id: tagLabel, label: tagLabel}),
                    isAdded: true,
                    clicked: function () {
                        that.tagsToRemove.push(tagLabel);
                        tagView.$el.remove();
                    }} :
                {
                    model: new Tag({id: tagLabel, label: tagLabel}),
                    isAdded: true,
                    clicked: function () {
                        that.tagsToRemove.push(tagLabel);
                        tagView.$el.remove();
                        that.model.removeTag(tagLabel, function () {
                            if (that.model.collection.parent) {
                                if (_.contains(that.tagsToRemove, that.model.collection.parent.id)) {
                                    that.model.collection.remove(that.model);
                                }
                            }
                        });
                        tagView.$el.remove();
                    }
                };

                tagView = new TagView(tagViewParams).render();

                $tagsZone.append(tagView.el);

            });

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

        onFolderPathClicked: function () {
            var redirectPath = this.folderPath ? 'folders/' + encodeURIComponent(this.folderPath) : 'folders';
            App.router.navigate(App.config.workspaceId + '/' + redirectPath, {trigger: true});
            ModalView.prototype.cancelAction.call(this);
        }

    });
    return IterationView;
});

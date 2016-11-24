/*global _,define,App,$*/
define([
    'backbone',
    'mustache',
    'common-objects/views/components/modal',
    'common-objects/views/file/file_list',
    'text!common-objects/templates/part/part_modal.html',
    'common-objects/views/attributes/attributes',
    'common-objects/views/attributes/template_new_attributes',
    'common-objects/views/part/part_effectivities_view',
    'common-objects/views/part/part_assembly_view',
    'common-objects/views/part/modification_notification_group_list_view',
    'common-objects/views/linked/linked_documents',
    'common-objects/views/part/used_by_view',
    'common-objects/views/alert',
    'common-objects/collections/linked/linked_document_collection',
    'common-objects/collections/linked/linked_document_iteration_collection',
    'common-objects/views/workflow/lifecycle',
    'common-objects/views/part/conversion_status_view',
    'common-objects/utils/date',
    'common-objects/views/tags/tag',
    'common-objects/models/tag'
], function (Backbone, Mustache, ModalView, FileListView, template, AttributesView, TemplateNewAttributesView, PartEffectivitiesView, PartAssemblyView, ModificationNotificationGroupListView, LinkedDocumentsView, UsedByView, AlertView, LinkedDocumentCollection, LinkedDocumentIterationCollection, LifecycleView, ConversionStatusView, date,TagView,Tag) {

    'use strict';

    var PartModalView = ModalView.extend({

        initialize: function () {
            this.iterations = this.model.getIterations();
            this.iteration = this.options.iteration && this.options.iteration < this.iterations.size() ?
                this.iterations.get(this.options.iteration) : this.model.getLastIteration();

            this.productId = this.options.productId;
            this.productConfigSpec = this.options.productConfigSpec;

            ModalView.prototype.initialize.apply(this, arguments);

            this.events['click a#previous-iteration'] = 'onPreviousIteration';
            this.events['click a#next-iteration'] = 'onNextIteration';
            this.events['click .modal-footer button.btn-primary'] = 'interceptSubmit';
            this.events['submit #form-part'] = 'onSubmitForm';
            this.events['click .action-checkin'] = 'actionCheckin';
            this.events['click .action-checkout'] = 'actionCheckout';
            this.events['click .action-undocheckout'] = 'actionUndoCheckout';
            this.events['notification:acknowledged'] = 'updateModificationNotifications';
            this.events['file:uploaded'] = 'updateConversionStatusView';
            this.events['close-modal-request'] = 'closeModal';

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
            return this.$tabs.filter('.active').index();
        },

        activateTab: function (index) {
            this.$tabs.eq(index).children().tab('show');
        },

        activateFileTab: function(){
            this.activateTab(3);
        },

        activateNotificationsTab: function(){
            this.activateTab(this.$tabs.length - 1);
        },

        render: function () {
            var data = {
                part: this.model,
                i18n: App.config.i18n,
                permalink: this.model.getPermalink(),
                hasIterations: this.model.hasIterations()
            };

            this.editMode = this.model.isCheckoutByConnectedUser() && this.iterations.isLast(this.iteration);
            data.editMode = this.editMode;
            data.isCheckout = this.model.isCheckout();
            this.isCheckout = data.isCheckout ;
            this.isReleased = this.model.attributes.status === 'RELEASED';
            data.isReleased = this.isReleased;
            this.isObsolete = this.model.attributes.status === 'OBSOLETE';
            data.isObsolete = this.isObsolete;
            data.isShowingLast = this.iterations.isLast(this.iteration);
            data.isLocked = this.model.isCheckout() && !this.model.isCheckoutByConnectedUser();

            if (this.model.hasIterations()) {
                var hasNextIteration = this.iterations.hasNextIteration(this.iteration);
                var hasPreviousIteration = this.iterations.hasPreviousIteration(this.iteration);
                data.iterations = this.model.getIterations().length;
                data.iteration = this.iteration.toJSON();
                data.iteration.hasNextIteration = hasNextIteration;
                data.iteration.hasPreviousIteration = hasPreviousIteration;
                data.reference = this.iteration.getReference();
                data.iteration.creationDate = date.formatTimestamp(
                    App.config.i18n._DATE_FORMAT,
                    data.iteration.creationDate
                );
                data.iteration.modificationDate = date.formatTimestamp(
                    App.config.i18n._DATE_FORMAT,
                    data.iteration.modificationDate
                );

                if (this.editMode) {
                    data.iteration.revisionDate = data.iteration.creationDate;
                } else {
                    data.iteration.revisionDate = date.formatTimestamp(
                        App.config.i18n._DATE_FORMAT,
                        data.iteration.checkInDate
                    );
                }
            }
            data.hasOneIteration= (this.iterations.length < 1);
            this.$el.html(Mustache.render(template, data));

            this.$authorLink = this.$('.author-popover');
            this.$checkoutUserLink = this.$('.checkout-user-popover');
            this.$releaseUserLink = this.$('.release-user-popover');
            this.$obsoleteUserLink = this.$('.obsolete-user-popover');

            this.$inputIterationNote = this.$('#inputRevisionNote');
            this.$tabs = this.$('.nav-tabs li');

            this.bindUserPopover();
            if (this.iteration) {
                this.initCadFileUploadView();
                this.initAttachedFilesUploadView();
                this.initAttributesView();
                this.initPartEffectivitiesView();
                this.initPartAssemblyView();
                this.initLinkedDocumentsView();
                this.initUsedByView();
                this.initLifeCycleView();

                if (!data.iteration.hasNextIteration) {
                    this.initModificationNotificationGroupListView();
                }
            }

            date.dateHelper(this.$('.date-popover'));
            this.tagsManagement(this.editMode);
            return this;
        },

        bindUserPopover: function () {
            this.$authorLink.userPopover(this.model.getAuthorLogin(), this.model.getNumber(), 'right');
            if (this.model.isCheckout()) {
                this.$checkoutUserLink.userPopover(this.model.getCheckOutUserLogin(), this.model.getNumber(), 'right');
            }
            if (this.model.getReleaseAuthor()) {
                this.$releaseUserLink.userPopover(this.model.getReleaseAuthorLogin(), this.model.getNumber(), 'right');
            }
            if (this.model.isObsolete()) {
                this.$obsoleteUserLink.userPopover(this.model.getObsoleteAuthorLogin(), this.model.getNumber(), 'right');
            }
        },

        initAttributesView: function () {

            var that = this;

            this.attributes = new Backbone.Collection();

            this.attributesView = new AttributesView({
                el: this.$('#attributes-list')
            });

            this.attributesView.setAttributesLocked(this.model.isAttributesLocked());
            this.attributesView.setEditMode(this.editMode);
            this.attributesView.render();

            _.each(this.iteration.getAttributes().models, function (item) {
                that.attributesView.addAndFillAttribute(item);
            });

            this.attributeTemplatesView =  new TemplateNewAttributesView({
                el: this.$('#attribute-templates-list'),
                attributesLocked: false,
                editMode : this.editMode
            });
            this.attributeTemplatesView.render();
            this.attributeTemplatesView.collection.reset(this.iteration.getAttributeTemplates());

        },

        interceptSubmit: function () {
            this.isValid = !this.$('.tabs').invalidFormTabSwitcher();
        },

        onSubmitForm: function (e) {

            // cannot pass a collection of cad file to server.
            var cadFile = this.cadFileView.collection.first();
            if (cadFile) {
                this.iteration.set('nativeCADFile', cadFile.get('fullName'));
            } else {
                this.iteration.set('nativeCADFile', '');
            }

            var that = this;

            this.iteration.save({
                iterationNote: this.$inputIterationNote.val(),
                components: this.partAssemblyView.collection.toJSON(),
                instanceAttributes: this.attributesView.collection.toJSON(),
                instanceAttributeTemplates: this.attributeTemplatesView.collection.toJSON(),
                linkedDocuments: this.linkedDocumentsView.collection.toJSON()
            }, {
                success: function () {
                    if (that.model.collection){
                        that.model.collection.fetch();
                    }
                    that.model.fetch();
                    that.hide();
                    that.model.trigger('change');
                    Backbone.Events.trigger('part:saved');
                    Backbone.Events.trigger('part:iterationChange');
                },
                error: this.onError
            });


            that.deleteClickedTags();
            this.cadFileView.deleteFilesToDelete();
            this.attachedFilesView.deleteFilesToDelete();

            e.preventDefault();
            e.stopPropagation();

            return false;
        },

        initCadFileUploadView: function () {
            this.cadFileView = new FileListView({
                title: App.config.i18n.CAD_FILE,
                baseName: this.iteration.getBaseName('nativecad'),
                deleteBaseUrl: this.iteration.url(),
                uploadBaseUrl: this.iteration.getNativeCadFileUploadBaseUrl(),
                collection: this.iteration._nativeCADFile,
                editMode: this.editMode,
                singleFile: true
            }).render();

            this.$('#iteration-files').html(this.cadFileView.el);
            if(this.editMode){
                this.conversionStatusView = new ConversionStatusView({
                    model:this.iteration
                }).render();
                this.$('.file-list').first().after(this.conversionStatusView.el);
            }

        },

        initAttachedFilesUploadView: function () {
            this.attachedFilesView = new FileListView({
                title: App.config.i18n.ATTACHED_FILES,
                baseName: this.iteration.getBaseName('attachedfiles'),
                deleteBaseUrl: this.iteration.url(),
                uploadBaseUrl: this.iteration.getAttachedFilesUploadBaseUrl(),
                collection: this.iteration.getAttachedFiles(),
                editMode: this.editMode
            }).render();

            this.$('#iteration-files').append(this.attachedFilesView.el);

        },

        updateConversionStatusView:function(){
            this.conversionStatusView.launch();
        },

        initPartEffectivitiesView: function () {
            this.partEffectivitiesView = new PartEffectivitiesView({
                el: '#effectivities-list',
                notifications: this.$el.find('.notifications').first(),
                productId: this.model.id,
                model: this.model
            }).render();
        },

        initPartAssemblyView: function () {
            this.partAssemblyView = new PartAssemblyView({
                el: '#iteration-components',
                collection: new Backbone.Collection(this.iteration.getComponents()),
                editMode: this.editMode,
                model: this.model
            }).render();
        },

        initLinkedDocumentsView: function () {
            if (this.productConfigSpec) {
                var self = this;
                $.ajax({
                    type:'GET',
                    url: App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/products/' + this.productId + '/document-links/' + this.iteration.getReference() + '/' + this.productConfigSpec,
                    contentType:'application/json',

                    success:function(linkedDocuments) {
                        self.iteration.setLinkedDocuments(linkedDocuments);
                        self.displayLinkedDocumentIterationsView();
                    },

                    error: function() {
                        self.displayLinkedDocumentsView();
                    }
                });

            } else {
                this.displayLinkedDocumentsView();
            }
        },

        displayLinkedDocumentsView: function () {
            this.linkedDocumentsView = new LinkedDocumentsView({
                editMode: this.editMode,
                commentEditable:true,
                documentIteration: this.iteration,
                collection: new LinkedDocumentCollection(this.iteration.getLinkedDocuments())
            }).render();

            /* Add the documentLinksView to the tab */
            this.$('#iteration-links').html(this.linkedDocumentsView.el);
        },

        displayLinkedDocumentIterationsView: function () {
            this.linkedDocumentsView = new LinkedDocumentsView({
                editMode: this.editMode,
                commentEditable:true,
                documentIteration: this.iteration,
                collection: new LinkedDocumentIterationCollection(this.iteration.getLinkedDocuments())
            }).render();

            /* Add the documentLinksView to the tab */
            this.$('#iteration-links').html(this.linkedDocumentsView.el);
        },

        initUsedByView: function () {
            this.usedByView = new UsedByView({
                linkedPart: this.model
            }).render();

            /* Add the usedByView to the tab */
            this.$('#iteration-used-by').html(this.usedByView.el);
        },

        initLifeCycleView: function () {
            var that = this;
            if (this.model.get('workflow')) {

                this.lifecycleView = new LifecycleView({
                    el: '#tab-iteration-lifecycle'
                }).setWorkflow(this.model.get('workflow')).setEntityType('parts').render();

                this.lifecycleView.on('lifecycle:change', function () {
                    that.model.fetch({success: function () {
                        that.lifecycleView.setWorkflow(that.model.get('workflow')).setEntityType('parts').render();
                    }});
                });

            } else {
                this.$('a[href=#tab-iteration-lifecycle]').hide();
            }
        },

        initModificationNotificationGroupListView: function () {
            new ModificationNotificationGroupListView({
                el: '#iteration-modification-notifications',
                collection: this.model.getModificationNotifications()
            }).render();
        },

        updateModificationNotifications: function () {
            var unread = 0;
            this.model.getModificationNotifications().each(function(notif) {
                if (!notif.isAcknowledged()) {
                    unread++;
                }
            });
            if (unread === 0) {
                this.model.fetch();
                Backbone.Events.trigger('part:saved');
            }
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
                    }
                }
                    :
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

        actionCheckin: function () {

            // cannot pass a collection of cad file to server.
            var cadFile = this.cadFileView.collection.first();
            if (cadFile) {
                this.iteration.set('nativeCADFile', cadFile.get('fullName'));
            } else {
                this.iteration.set('nativeCADFile', '');
            }

            var that = this;
            this.iteration.save({
                    iterationNote: this.$inputIterationNote.val() || null,
                    components: this.partAssemblyView.collection.toJSON(),
                    instanceAttributes: this.attributesView.collection.toJSON(),
                    instanceAttributeTemplates: this.attributeTemplatesView.collection.toJSON(),
                    linkedDocuments: this.linkedDocumentsView.collection.toJSON()
                }, {
                    success: function () {
                        that.model.checkin().success(function () {
                            that.onSuccess();
                        });
                    },
                    error: this.onError
                }
            );

            this.deleteClickedTags();
            this.cadFileView.deleteFilesToDelete();
            this.attachedFilesView.deleteFilesToDelete();
        },

        actionCheckout: function () {
            var self = this;
            self.model.checkout().success(function () {
                self.onSuccess();
            });

        },

        actionUndoCheckout: function () {
            var self = this;
            self.model.undocheckout().success(function () {
                self.onSuccess();
            });

        },

        onSuccessfulLoad: function () {
            this.model.fetch().success(function () {
                this.iteration = this.model.getLastIteration();
                this.iterations = this.model.getIterations();
                this.render();
                this.activateTab(1);
                Backbone.Events.trigger('part:saved');
                Backbone.Events.trigger('part:iterationChange');
            }.bind(this));

        },

        onError: function (model, error) {
            var errorMessage = error ? error.responseText : model;

            this.$el.find('.notifications').first().append(new AlertView({
                type: 'error',
                message: errorMessage
            }).render().$el);
        },

        closeModal: function () {
            this.hide();
        }

    });

    return PartModalView;

});

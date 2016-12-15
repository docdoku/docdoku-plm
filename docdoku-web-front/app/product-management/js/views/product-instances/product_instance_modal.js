/*global _,$,define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/product-instances/product_instance_modal.html',
    'text!common-objects/templates/path/path.html',
    'common-objects/utils/date',
    'common-objects/collections/attribute_collection',
    'common-objects/views/attributes/attributes',
    'common-objects/views/file/file_list',
    'common-objects/collections/linked/linked_document_collection',
    'common-objects/views/linked/linked_documents',
    'common-objects/collections/file/attached_file_collection',
    'common-objects/views/alert',
    'common-objects/collections/product_baselines',
    'common-objects/views/pathToPathLink/path_to_path_link_item'
], function (Backbone, Mustache, template, pathTemplate, date, AttributeCollection, ProductInstanceAttributeListView, FileListView, LinkedDocumentCollection, LinkedDocumentsView, AttachedFileCollection, AlertView, ProductBaselines, PathToPathLinkItemView) {
    'use strict';
    var ProductInstancesModalView = Backbone.View.extend({
        events: {
            'click .btn-primary': 'interceptSubmit',
            'submit #product_instance_edit_form': 'onSubmitForm',
            'hidden #product_instance_modal': 'onHidden',
            'shown #product_instance_modal': 'onShown',
            'click a#previous-iteration': 'onPreviousIteration',
            'click a#next-iteration': 'onNextIteration',
            'close-modal-request': 'closeModal',
            'click .btn-rebase': 'onRebase'
        },

        template: Mustache.parse(template),

        initialize: function () {
            this.productId = this.model.getConfigurationItemId();
            this.iteration = this.model.getLastIteration();
            this.iterations = this.model.getIterations();
            _.bindAll(this);
        },

        render: function () {

            this.editMode = this.iterations.isLast(this.iteration);
            var data = {
                i18n: App.config.i18n,
                model: this.iteration,
                editMode: this.editMode,
                creationDate: this.model.getFormattedCreationDate()
            };

            if (this.model.hasIterations()) {
                var hasNextIteration = this.iterations.hasNextIteration(this.iteration);
                var hasPreviousIteration = this.iterations.hasPreviousIteration(this.iteration);
                data.iteration = this.iteration.toJSON();
                data.iteration.formattedCreationDate = date.formatTimestamp(App.config.i18n._DATE_FORMAT, data.iteration.creationDate);
                data.iteration.formattedModificationDate = date.formatTimestamp(App.config.i18n._DATE_FORMAT, data.iteration.modificationDate);
                data.iteration.hasNextIteration = hasNextIteration;
                data.iteration.hasPreviousIteration = hasPreviousIteration;
                data.iteration.updateDate = date.formatTimestamp(
                    App.config.i18n._DATE_FORMAT,
                    data.iteration.updateDate
                );
            }

            this.$el.html(Mustache.render(template, data));
            this.bindDomElements();
            this.bindUserPopover();
            this.initAttributesView();
            this.initAttachedFileView();
            this.initLinkedDocumentsView();

            this.initPathDataView();
            this.openModal();
            this.renderChoices();

            var self = this;
            this.collection = new ProductBaselines({}, {productId: this.productId});
            this.collection.fetch({reset: true}).success(function () {
                self.$('.rebase-baseline-select').html('');
                _.each(self.collection.models, function (baseline) {
                    if (self.iteration.getBasedOnId() === baseline.getId()) {
                        self.$('.rebase-baseline-select').append('<option value="' + baseline.getId() + '" selected="selected">' + baseline.getName() + '</option>');
                    } else {
                        self.$('.rebase-baseline-select').append('<option value="' + baseline.getId() + '">' + baseline.getName() + '</option>');
                    }

                });
                // to get the Existing PathToPath, we need to have all the baseline.
                // should be changed, hack to make it work.
                self.getExistingPathToPath();
            });

            date.dateHelper(this.$('.date-popover'));

            return this;
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
            var index = this.getActiveTabIndex();
            this.iteration = iteration;
            this.undelegateEvents();
            this.closeModal();
            this.delegateEvents();
            this.render();
            this.activateTab(index);
        },
        getActiveTabIndex: function () {
            return this.$tabs.filter('.active').index();
        },

        bindDomElements: function () {
            this.$notifications = this.$('.notifications');
            this.$modal = this.$('#product_instance_modal');
            this.$tabs = this.$('.nav-tabs li');
            this.$inputIterationNote = this.$('#inputIterationNote');
            this.$authorLink = this.$('.author-popover');
            this.$substitutes = this.$('.substitutes-list');
            this.$substitutesCount = this.$('.substitutes-count');
            this.$optionals = this.$('.optionals-list');
            this.$optionalsCount = this.$('.optionals-count');
        },

        renderChoices: function () {
            var substitutes = this.iteration.getSubstitutesParts();
            var optionals = this.iteration.getOptionalsParts();
            this.$substitutesCount.text(substitutes.length);
            this.$optionalsCount.text(optionals.length);

            _.each(substitutes, this.drawSubstitutesChoice);
            _.each(optionals, this.drawOptionalsChoice);
        },

        drawSubstitutesChoice: function (data) {
            this.$substitutes.append(Mustache.render(pathTemplate, {
                i18n: App.config.i18n,
                partLinks: data.partLinks
            }));
            this.$substitutes.find('.well i.fa-long-arrow-right').last().remove();
        },

        drawOptionalsChoice: function (data) {
            this.$optionals.append(Mustache.render(pathTemplate, {
                i18n: App.config.i18n,
                partLinks: data.partLinks
            }));
            this.$optionals.find('.well i.fa-long-arrow-right').last().remove();
        },

        initLinkedDocumentsView: function () {
            this.linkedDocumentsView = new LinkedDocumentsView({
                editMode: this.editMode,
                commentEditable: true,
                documentIteration: this.iteration,
                collection: new LinkedDocumentCollection(this.iteration.getlinkedDocuments())
            }).render();

            /* Add the documentLinksView to the tab */
            this.$('#iteration-links').html(this.linkedDocumentsView.el);
        },

        initAttributesView: function () {

            var attributes = new AttributeCollection(this.iteration.getInstanceAttributes());

            this.attributesView = new ProductInstanceAttributeListView({
                collection: attributes
            });

            this.$('#attributes-list').html(this.attributesView.$el);

            this.attributesView.setEditMode(this.editMode);

            this.attributesView.render();
        },

        getExistingPathToPath: function () {
            var self = this;
            _.each(this.iteration.getPathToPathLinks(), function (pathToPathLink) {
                var pathToPathLinkItem = new PathToPathLinkItemView({
                    model: {
                        pathToPath: pathToPathLink,
                        serialNumber: self.model.getSerialNumber(),
                        productId: self.productId
                    }
                }).render();
                self.$('#path-to-path-links').append(pathToPathLinkItem.el);
            });

        },

        initAttachedFileView: function () {

            var filesMapping = _.map(this.iteration.getAttachedFiles(), function (binaryResource) {

                return {
                    fullName: binaryResource.fullName,
                    shortName: _.last(binaryResource.fullName.split('/')),
                    created: true
                };


            });
            var attachedFiles = new AttachedFileCollection(filesMapping);

            var _this = this;
            this.fileListView = new FileListView({
                deleteBaseUrl: this.iteration.url(),
                uploadBaseUrl: _this.iteration.getUploadBaseUrl(),
                collection: attachedFiles,
                editMode: true
            }).render();


            // Add the fileListView to the tab
            this.$('#tab-products-instances-files').append(this.fileListView.el);


        },

        initPathDataView: function () {
            var pathDataList = this.$('#path-data-list');
            var paths = this.iteration.getPathDataPaths();
            var self = this;
            var pathsHtml = [];
            _.each(paths, function (path) {

                var html = $(Mustache.render(pathTemplate, {
                    i18n: App.config.i18n,
                    partLinks: path.partLinks,
                    editMode: self.editMode
                }));

                html.find('button.close').click({fullPath: self.getFullPath(path)}, self.removePathData);
                html.find('i.fa-long-arrow-right').last().remove();
                pathsHtml.push(html);
            });
            pathDataList.html(pathsHtml);
        },

        getFullPath: function (path) {
            var fullPath = '';
            _.each(path.partLinks, function (partLink) {
                fullPath += partLink.fullId + '-';
            });
            return fullPath.substr(0, fullPath.length - 1);
        },

        removePathData: function (event) {
            var self = this;
            var fullPath = event.data.fullPath;
            var pathData = _.findWhere(this.iteration.getPathData(), {path: fullPath});
            $.ajax({
                url: App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/products/' + self.model.getConfigurationItemId() + '/product-instances/' + self.model.getSerialNumber() + '/pathdata/' + pathData.id,
                type: 'DELETE',
                error: function (error, type) {
                    self.onError(type, error);
                },
                success: function () {
                    self.model.fetch().success(function () {
                        self.iteration = self.model.getIterations().get(self.iteration.getIteration());
                        self.initPathDataView();
                    });
                }
            });
        },

        bindUserPopover: function () {
            this.$authorLink.userPopover(this.model.getUpdateAuthor(), this.model.getSerialNumber(), 'right');
        },

        interceptSubmit: function () {
            this.isValid = !this.$('.tabs').invalidFormTabSwitcher();
        },

        updateDataForm: function () {
            this.iteration.setIterationNote(this.$inputIterationNote.val());
            this.iteration.setInstanceAttributes(this.attributesView.collection.toJSON());
            this.iteration.setLinkedDocuments(this.linkedDocumentsView.collection.toJSON());
            var files = this.iteration.get('attachedFiles');

            /*tracking back files*/
            this.iteration.set({
                attachedFiles: files
            });

        },
        onSubmitForm: function (e) {
            var _this = this;
            this.updateDataForm();
            this.iteration.save(JSON.stringify(this.iteration), '', {
                success: function () {
                    _this.model.fetch();
                    _this.closeModal();
                },
                error: _this.onError
            });
            this.fileListView.deleteFilesToDelete();
            e.preventDefault();
            e.stopPropagation();
            return false;
        },

        onRebase: function () {
            var self = this;
            //save the previous iteration before create a new one
            this.updateDataForm();
            this.iteration.save(JSON.stringify(this.iteration), '', {
                success: function () {
                    self.model.fetch();
                },
                error: self.onError
            });
            this.fileListView.deleteFilesToDelete();

            //Do the rebase
            var selectedBaselineId = this.$('.rebase-baseline-select').val();

            var url = App.config.contextPath + '/api/workspaces/' +
                App.config.workspaceId + '/product-instances/' +
                this.productId + '/instances/' +
                this.model.getSerialNumber() + '/rebase';

            $.ajax({
                type: 'PUT',
                data: JSON.stringify({id: selectedBaselineId}),
                contentType: 'application/json',
                url: url,
                success: function () {
                    self.model.fetch().success(function () {
                        self.initialize();
                        self.undelegateEvents();
                        self.closeModal();
                        self.delegateEvents();
                        self.render();
                        self.activateTab(1);
                        self.onRebaseSuccess();
                    });
                },
                error: function (errorMessage, type) {
                    self.onError(type, errorMessage);
                }
            });

        },

        onRebaseSuccess: function () {
            this.$notifications.append(new AlertView({
                type: 'success',
                message: App.config.i18n.REBASE_SUCCESS
            }).render().$el);
        },

        onError: function (model, error) {
            var errorMessage = error ? error.responseText : model;
            this.$notifications.append(new AlertView({
                type: 'error',
                message: errorMessage
            }).render().$el);
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

        onShown: function () {
            this.$modal.addClass('ready');
        },

        activateTab: function (index) {
            this.$tabs.eq(index).children().tab('show');
        },

        activePathToPathLinkTab: function () {
            this.activateTab(6);
        },

        activePathDataTab: function () {
            this.activateTab(7);
        },

        activeFilesTab: function () {
            this.activateTab(4);
        }

    });
    return ProductInstancesModalView;
});

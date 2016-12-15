/*global define,App,_,$*/
define([
        'backbone',
        'mustache',
        'text!templates/product-instance-data-modal.html',
        'models/path_data_master',
        'common-objects/models/path_data_iteration',
        'common-objects/views/attributes/attributes',
        'common-objects/views/file/file_list',
        'common-objects/views/linked/linked_documents',
        'common-objects/collections/linked/linked_document_collection',
        'common-objects/collections/file/attached_file_collection',
        'common-objects/models/attribute',
        'common-objects/views/alert'
    ], function (Backbone, Mustache, template, PathDataMaster, PathDataIteration, AttributesView, FileListView, LinkedDocumentsView, LinkedDocumentCollection, AttachedFileCollection, Attribute, AlertView) {

        'use strict';

        var PathDataModalView = Backbone.View.extend({

            className: 'modal hide product-instance-data-modal in',

            events: {
                'hidden': 'onHidden',
                'submit #form-deliverable-data': 'onSave',
                'click .cancel-button': 'closeModal',
                'click .save-button': 'interceptSubmit',
                'click .new-iteration': 'saveAndCreateNewIteration',
                'click a#previous-iteration': 'onPreviousIteration',
                'click a#next-iteration': 'onNextIteration'
            },

            initialize: function () {
                this.path = this.options.path;// ? this.options.path : '-1';
                this.serialNumber = this.options.serialNumber;
                _.bindAll(this);
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
                this.iteration.setSerialNumber(this.serialNumber);
                this.iteration.setId(this.pathDataId);

                var activeTabIndex = this.getActiveTabIndex();
                this.render();
                this.activateTab(activeTabIndex);
            },

            render: function () {
                this.editMode = false;
                var dataIteration = null;
                if (this.iterations) {
                    this.editMode = this.iteration.getIteration() === this.model.getIterations().size();
                    var hasNextIteration = this.iterations.hasNextIteration(this.iteration);
                    var hasPreviousIteration = this.iterations.hasPreviousIteration(this.iteration);
                    dataIteration = {
                        iteration: this.iteration.getIteration(),
                        iterationNote: this.iteration.getIterationNote(),
                        iterations: this.model.getIterations().size(),
                        hasNextIteration: hasNextIteration,
                        hasPreviousIteration: hasPreviousIteration,
                        i18n: App.config.i18n,
                        editMode: this.editMode
                    };
                }
                this.$el.html(Mustache.render(template, dataIteration || {editMode: true, i18n: App.config.i18n}));
                this.bindDOMElements();
                this.buildTabs();
            },

            bindDOMElements: function () {
                this.$modal = this.$el;
                this.$tabs = this.$('.nav-tabs li');
            },

            getActiveTabIndex: function () {
                return this.$tabs.filter('.active').index();
            },
            activateTab: function (index) {
                this.$tabs.eq(index).children().tab('show');
            },
            initAndOpenModal: function () {

                var url = App.config.contextPath + '/api/workspaces/' +
                    App.config.workspaceId + '/product-instances/' +
                    App.config.productId + '/instances/' +
                    this.serialNumber + '/pathdata/' + this.path;

                var self = this;

                $.ajax({
                    type: 'GET',
                    url: url,
                    success: function (data) {
                        self.isNew = !data.id;
                        data.serialNumber = self.serialNumber;
                        data.path = self.path;
                        self.pathDataId = data.id;
                        self.model = new PathDataMaster(data);

                        if (self.isNew) {
                            self.$('.delete-button').hide();
                            self.$('.title-tab-file').hide();
                            self.$('.title-tab-link').hide();
                            self.$el.html(Mustache.render(template, {i18n: App.config.i18n}));
                            self.$modal = self.$('.modal.product-instance-data-modal');
                            self.$tabs = self.$('.nav-tabs li');
                        } else {
                            self.iteration = self.model.getLastIteration();
                            self.iterations = self.model.getIterations();
                            self.iteration.setSerialNumber(self.serialNumber);
                            self.iteration.setPath(self.path);
                            self.iteration.setId(self.model.id);
                        }
                        self.render();
                        self.openModal();
                    }

                });

                return this;
            },

            buildTabs: function () {
                var partLinks = this.model.getPartLinks();
                var $pathDescription = this.$('.path-description');
                _.each(partLinks, function (partLink) {
                    var text = partLink.name + ' < ' + partLink.number + ' >';
                    if (partLink.referenceDescription) {
                        text += ' (' + partLink.referenceDescription + ')';
                    }
                    $pathDescription.append(text + ' <i class="fa fa-long-arrow-right"> ');
                });

                this.$('.fa.fa-long-arrow-right').last().remove();

                this.buildAttributesTab();
                this.addPartAttributes();

                if (this.iteration) {
                    this.buildLinkedDocumentTab();
                    this.buildFileTab();
                }

            },
            buildAttributesTab: function () {
                var self = this;
                this.attributesView = new AttributesView({
                    el: this.$('#pathDataAttributes')
                });
                this.attributesView.setEditMode(!this.iteration || this.iteration.getIteration() === this.model.getIterations().size());
                this.attributesView.render();

                if (this.isNew) {
                    this.addPartAttributeTemplatesAsAttributes();

                } else {
                    _.each(this.iteration.getInstanceAttributes(), function (item) {
                        self.attributesView.addAndFillAttribute(new Attribute(item));
                    });
                }
            },

            buildLinkedDocumentTab: function () {
                this.linkedDocuments = new LinkedDocumentCollection(this.iteration !== null ? this.iteration.getDocumentLinked() : []);
                this.linkedDocumentsView = new LinkedDocumentsView({
                    editMode: this.editMode,
                    commentEditable: true,
                    collection: this.linkedDocuments
                });
                this.linkedDocumentsView.render();
                this.$('#tab-link').html(this.linkedDocumentsView.$el);
            },

            buildFileTab: function () {

                var filesMapping = _.map(this.iteration !== null ? this.iteration.getAttachedFiles() : [], function (binaryResource) {
                    return {
                        fullName: binaryResource.fullName,
                        shortName: _.last(binaryResource.fullName.split('/')),
                        created: true
                    };
                });

                this.attachedFiles = new AttachedFileCollection(filesMapping);

                this.fileListView = new FileListView({
                    deleteBaseUrl: this.iteration.getDeleteBaseUrl(),
                    uploadBaseUrl: this.iteration.getUploadBaseUrl(),
                    collection: this.attachedFiles,
                    editMode: this.editMode
                });

                this.fileListView.render();

                this.$('#tab-files').html(this.fileListView.$el);
            },
            interceptSubmit: function () {
                this.isValid = !this.$('.tabs').invalidFormTabSwitcher();
            },

            onSave: function (e) {
                if (this.isValid) {
                    if (!this.iteration) {
                        var self = this;
                        this.iterations = this.model.iterations;
                        this.iteration = new PathDataIteration(this.model.attributes);
                        this.iterations.add(this.iteration);
                        this.iteration.setIteration(this.iterations.size());
                        this.iteration.setInstanceAttributes(this.attributesView.collection.toJSON());
                        this.iteration.setIterationNote(this.$('.description-input').val());

                        //POST
                        var creationURL = App.config.contextPath + '/api/workspaces/' +
                            App.config.workspaceId + '/product-instances/' +
                            App.config.productId + '/instances/' + this.serialNumber +
                            '/pathdata/' + this.iteration.getPath() + '/new';

                        $.ajax({
                            type: 'POST',
                            url: creationURL,
                            data: JSON.stringify(this.iteration.toJSON()),
                            contentType: 'application/json',
                            success: function (data) {
                                self.model = new PathDataMaster(data);
                                self.iteration = self.model.getLastIteration();
                                self.iterations = self.model.getIterations();
                                self.iteration.setSerialNumber(self.serialNumber);
                                self.iteration.setPath(self.path);
                                self.iteration.setId(self.model.id);
                                self.trigger('path-data:created');
                                self.closeModal();
                            },
                            error: function (errorMessage) {
                                self.$('#alerts').append(new AlertView({
                                    type: 'error',
                                    title: errorMessage.statusText,
                                    message: errorMessage.responseText
                                }).render().$el);
                            }
                        });

                    } else {
                        this.updateIteration(this.closeModal);
                    }
                }
                e.preventDefault();
                e.stopPropagation();
                return false;
            },

            updateIteration: function (callback) {
                var self = this;
                this.iteration.setId(this.model.getId());
                this.iteration.setInstanceAttributes(this.attributesView.collection.toJSON());
                this.iteration.setIterationNote(this.$('.description-input').val());
                this.iteration.setDocumentLinked(this.linkedDocumentsView.collection.toJSON());
                this.iteration.set({
                    attachedFiles: this.attachedFiles
                });

                //PUT
                var updateURL = App.config.contextPath + '/api/workspaces/' +
                    App.config.workspaceId + '/product-instances/' +
                    App.config.productId + '/instances/' +
                    this.serialNumber + '/pathdata/' +
                    this.iteration.getId() + '/iterations/' + this.iteration.getIteration();
                $.ajax({
                    type: 'PUT',
                    url: updateURL,
                    data: JSON.stringify(this.iteration.toJSON()),
                    contentType: 'application/json',
                    success: function () {
                        callback();
                    },
                    error: function (errorMessage) {
                        self.$('#alerts').append(new AlertView({
                            type: 'error',
                            title: errorMessage.statusText,
                            message: errorMessage.responseText
                        }).render().$el);
                    }
                });

                this.fileListView.deleteFilesToDelete();
            },

            createIteration: function () {
                this.lasIteration = this.model.getLastIteration();
                this.iteration = new PathDataIteration(this.model.attributes);
                this.iterations.add(this.iteration);
                this.iteration.setIteration(this.iterations.size());
                this.iteration.setInstanceAttributes(this.lasIteration.getInstanceAttributes());
                this.iteration.setIterationNote('');
                this.iteration.setDocumentLinked(this.lasIteration.getDocumentLinked());
                this.iteration.set({
                    attachedFiles: this.lasIteration.getAttachedFiles()
                });

                //POST
                var url = App.config.contextPath + '/api/workspaces/' +
                    App.config.workspaceId + '/product-instances/' +
                    App.config.productId + '/instances/' +
                    this.serialNumber + '/pathdata/' + this.iteration.getId();
                var self = this;
                $.ajax({
                    type: 'POST',
                    url: url,
                    data: JSON.stringify(this.iteration.toJSON()),
                    contentType: 'application/json',
                    success: function (data) {
                        self.model = new PathDataMaster(data);
                        self.iteration = self.model.getLastIteration();
                        self.iterations = self.model.getIterations();
                        self.iteration.setSerialNumber(self.serialNumber);
                        self.iteration.setPath(self.path);
                        self.iteration.setId(self.model.id);
                        self.render();
                    },
                    error: function (errorMessage) {
                        self.$('#alerts').append(new AlertView({
                            type: 'error',
                            message: errorMessage
                        }).render().$el);
                    }
                });
            },

            saveAndCreateNewIteration: function () {
                this.updateIteration(this.createIteration);
            },

            addPartAttributes: function () {

                var attributesView = new AttributesView({el: this.$('#partAttributes')});
                attributesView.setEditMode(false);
                attributesView.render();

                _.each(this.model.getPartAttributes(), function (item) {
                    attributesView.addAndFillAttribute(new Attribute(item));
                });
            },

            addPartAttributeTemplatesAsAttributes: function () {
                var self = this;
                _.each(this.model.getPartAttributeTemplates(), function (item) {
                    self.attributesView.addAndFillAttribute(new Attribute(item));
                });
            },

            openModal: function () {
                this.$modal.modal('show');
            },

            closeModal: function () {
                this.$modal.modal('hide');
            },

            onHidden: function () {
                this.remove();
            }

        });

        return PathDataModalView;
    }
);

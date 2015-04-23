/*global define,App,_*/
define([
        'backbone',
        'mustache',
        'text!templates/product-instance-data-modal.html',
        '../models/product_instance_path_data_master',
        '../models/product_instance_path_data_iteration',
        'common-objects/views/attributes/attributes',
        'common-objects/views/file/file_list',
        'common-objects/views/linked/linked_documents',
        'common-objects/collections/linked/linked_document_collection',
        'common-objects/collections/file/attached_file_collection',
        'common-objects/models/attribute',
        'common-objects/views/alert'
    ], function (Backbone, Mustache, template, ProductInstancePathMasterDataModel, ProductInstancePathIterationDataModel, AttributesView, FileListView, LinkedDocumentsView, LinkedDocumentCollection, AttachedFileCollection, Attribute, AlertView) {

        'use strict';

        var ProductInstanceDataModalView = Backbone.View.extend({

            events: {
                'hidden .modal.product-instance-data-modal': 'onHidden',
                'click .cancel-button': 'closeModal',
                'click .save-button': 'onSave',
                'click .new-iteration': 'createIteration',
                'click a#previous-iteration': 'onPreviousIteration',
                'click a#next-iteration': 'onNextIteration'
            },

            initialize: function () {
                this.path = this.options.path ? this.options.path : '-1';
                this.serialNumber = this.options.serialNumber;
                this.$tabs = this.$('.nav-tabs li');
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
                var activeTabIndex = this.getActiveTabIndex();
                this.activateTab(activeTabIndex);
                this.closeModal();
                this.redraw();


            },
            redraw: function () {
                var hasNextIteration = this.iterations.hasNextIteration(this.iteration);
                var hasPreviousIteration = this.iterations.hasPreviousIteration(this.iteration);
                var dataIteration = {
                    iteration: this.iteration.getIteration(),
                    iterationNote: this.iteration.getIterationNote(),
                    iterations: this.model.getIterations().size(),
                    hasNextIteration: hasNextIteration,
                    hasPreviousIteration: hasPreviousIteration,
                    i18n: App.config.i18n
                };
                this.$el.html(Mustache.render(template, dataIteration));
                this.$modal = this.$('.modal.product-instance-data-modal');
                this.$tabs = this.$('.nav-tabs li');
                this.buildTabs();
                this.openModal();
            },
            getActiveTabIndex: function () {
                return this.$tabs.filter('.active').index();
            },
            activateTab: function (index) {
                this.$tabs.eq(index).children().tab('show');
            },

            render: function () {

                var url = App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/products/' + App.config.productId + '/product-instances/' + this.serialNumber + '/pathdata/' + this.path;
                var self = this;

                $.ajax({
                    type: 'GET',
                    url: url,
                    success: function (data) {
                        self.isNew = !data.id;
                        data.serialNumber = self.serialNumber;
                        data.path = self.path;
                        self.model = new ProductInstancePathMasterDataModel(data);

                        if (self.isNew) {
                            self.$('.delete-button').hide();
                            self.$('.title-tab-file').hide();
                            self.$('.title-tab-link').hide();
                            self.$el.html(Mustache.render(template, {i18n: App.config.i18n}));
                            self.$modal = self.$('.modal.product-instance-data-modal');
                            self.$tabs = self.$('.nav-tabs li');
                            self.buildTabs();
                            self.trigger('ready');
                        } else {
                            self.iteration = self.model.getLastIteration();
                            self.iterations = self.model.getIterations();
                            self.iteration.setSerialNumber(self.serialNumber);
                            self.iteration.setPath(self.path);
                            self.iteration.setId(self.model.id);
                            self.redraw();
                        }

                    }

                });

                return this;
            },

            buildTabs: function () {
                var self = this;
                var partsPath = this.model.getPartsPath();

                _.each(partsPath, function (part) {
                    self.$('.path-description').append(part.name);
                    self.$('.path-description').append('<i class="fa fa-chevron-right">');
                });

                self.$('.fa.fa-chevron-right').last().remove();
                this.buildAttributesTab();
                if (this.iteration) {
                    this.buildLinkedDocumentTab();
                    this.buildFileTab();
                }


            },
            buildAttributesTab: function () {
                var self = this;
                this.attributesView = new AttributesView({});
                this.attributesView.render();
                this.$('#tab-attributes').html(this.attributesView.$el);

                if (!this.isNew) {
                    _.each(this.iteration.getInstanceAttributes(), function (item) {
                        self.attributesView.addAndFillAttribute(new Attribute(item));
                    });
                }
            },

            buildLinkedDocumentTab: function () {
                this.linkedDocuments = new LinkedDocumentCollection(this.iteration !== null ? this.iteration.getDocumentLinked() : []);
                this.linkedDocumentsView = new LinkedDocumentsView({
                    editMode: true,
                    commentEditable: true,
                    collection: this.linkedDocuments
                });
                this.linkedDocumentsView.render();
                this.$('#tab-link').html(this.linkedDocumentsView.$el);
            },

            buildFileTab: function () {

                var filesMapping = _.map(this.iteration !== null ? this.iteration.getAttachedFiles() : [], function (fullName) {
                    return {
                        fullName: fullName,
                        shortName: _.last(fullName.split('/')),
                        created: true
                    };
                });

                this.attachedFiles = new AttachedFileCollection(filesMapping);

                this.fileListView = new FileListView({
                    deleteBaseUrl: this.iteration.getDeleteBaseUrl(),
                    uploadBaseUrl: this.iteration.getUploadBaseUrl(),
                    collection: this.attachedFiles,
                    editMode: true
                });

                this.fileListView.render();

                this.$('#tab-files').html(this.fileListView.$el);
            },

            onSave: function () {
                if (!this.iteration) {
                    this.iterations = this.model.iterations;
                    this.iteration = new ProductInstancePathIterationDataModel(this.model.attributes);
                    this.iterations.add(this.iteration);
                    this.iteration.setIteration(this.iterations.size());
                    this.iteration.setInstanceAttributes(this.attributesView.collection.toJSON());
                    this.iteration.setIterationNote(this.$('.description-input').val());

                    //POST
                    var url = App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/products/' + App.config.productId + '/product-instances/' + this.serialNumber + '/pathdata/' + this.iteration.getPath();
                    var self = this;
                    $.ajax({
                        type: 'POST',
                        url: url,
                        data: JSON.stringify(this.iteration),
                        contentType: 'application/json',
                        success: function (data) {
                            self.model = new ProductInstancePathMasterDataModel(data);
                            self.iteration = self.model.getLastIteration();
                            self.iterations = self.model.getIterations();
                            self.iteration.setSerialNumber(self.serialNumber);
                            self.iteration.setPath(self.path);
                            self.iteration.setId(self.model.id);
                            self.closeModal();
                        },
                        error: function (errorMessage) {
                            self.$('#alerts').append(new AlertView({
                                type: 'error',
                                message: errorMessage
                            }).render().$el);
                        }
                    });

                } else {
                    this.iteration.setId(this.model.getId());
                    this.iteration.setInstanceAttributes(this.attributesView.collection.toJSON());
                    this.iteration.setIterationNote(this.$('.description-input').val());
                    this.iteration.setDocumentLinked(this.linkedDocumentsView.collection.toJSON());
                    this.iteration.set({
                        attachedFiles: this.attachedFiles
                    });
                    //PUT
                    var url = App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/products/' + App.config.productId + '/product-instances/' + this.serialNumber + '/pathdata/' + this.iteration.getId() + '/iterations/' + this.iteration.getIteration();
                    var self = this;
                    $.ajax({
                        type: 'PUT',
                        url: url,
                        data: JSON.stringify(this.iteration),
                        contentType: 'application/json',
                        success: function () {
                            self.closeModal();
                        },
                        error: function (errorMessage) {
                            self.$('#alerts').append(new AlertView({
                                type: 'error',
                                message: errorMessage
                            }).render().$el);
                        }
                    });
                }

            },

            createIteration: function () {
                this.lasIteration = this.model.getLastIteration();
                this.iteration = new ProductInstancePathIterationDataModel(this.model.attributes);
                this.iterations.add(this.iteration);
                this.iteration.setIteration(this.iterations.size());
                this.iteration.setInstanceAttributes(this.lasIteration.getInstanceAttributes());
                this.iteration.setIterationNote(" ");
                this.iteration.setDocumentLinked(this.lasIteration.getDocumentLinked());
                this.iteration.set({
                    attachedFiles: this.lasIteration.getAttachedFiles()
                });
                //POST
                var url = App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/products/' + App.config.productId + '/product-instances/' + this.serialNumber + '/pathdata/' + this.iteration.getPath() + '/' + this.iteration.getId();
                var self = this;
                $.ajax({
                    type: 'POST',
                    url: url,
                    data: JSON.stringify(this.iteration),
                    contentType: 'application/json',
                    success: function (data) {
                        self.model = new ProductInstancePathMasterDataModel(data);
                        self.iteration = self.model.getLastIteration();
                        self.iterations = self.model.getIterations();
                        self.iteration.setSerialNumber(self.serialNumber);
                        self.iteration.setPath(self.path);
                        self.iteration.setId(self.model.id);
                        self.redraw();
                    },
                    error: function (errorMessage) {
                        self.$('#alerts').append(new AlertView({
                            type: 'error',
                            message: errorMessage
                        }).render().$el);
                    }
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

        return ProductInstanceDataModalView;
    }
);

/*global define,App,_*/
define([
        'backbone',
        'mustache',
        'text!templates/product-instance-data-modal.html',
        'models/product_instance_data',
        'common-objects/views/attributes/attributes',
        'common-objects/views/file/file_list',
        'common-objects/views/linked/linked_documents',
        'common-objects/collections/linked/linked_document_collection',
        'common-objects/collections/file/attached_file_collection',
        'common-objects/models/attribute',
        'common-objects/views/alert'
    ], function (Backbone, Mustache, template, ProductInstanceDataModel, AttributesView, FileListView, LinkedDocumentsView, LinkedDocumentCollection, AttachedFileCollection, Attribute, AlertView) {

        'use strict';

        var ProductInstanceDataModalView = Backbone.View.extend({

            events: {
                'hidden .modal.product-instance-data-modal': 'onHidden',
                'click .cancel-button' : 'closeModal',
                'click .save-button' : 'onSave',
                'click .delete-button' : 'onDelete'
            },

            initialize: function () {
                this.path = this.options.path ? this.options.path : '-1';
                this.serialNumber = this.options.serialNumber;
                this.model = new ProductInstanceDataModel({
                    path : this.path,
                    serialNumber : this.serialNumber
                });
                _.bindAll(this);
            },

            render: function () {
                this.$el.html(Mustache.render(template, {
                    i18n: App.config.i18n
                }));
                this.$modal = this.$('.modal.product-instance-data-modal');
                var self = this;
                var url = App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/products/' + App.config.productId + '/product-instances/' + this.serialNumber + '/pathdata?path=' + this.path;
                $.ajax({
                    type: 'GET',
                    url : url,
                    success: function(data){
                        self.isNew = !data.id;
                        self.model = new ProductInstanceDataModel(data);
                        if(self.isNew){
                            self.$('.delete-button').hide();
                            self.$('.title-tab-file').hide();
                            self.$('.title-tab-link').hide();
                        }
                        self.buildTabs();
                    },
                    error : function(errorMessage){
                        self.$('#alerts').append(new AlertView({
                            type: 'error',
                            message: errorMessage
                        }).render().$el);
                    }
                });

                return this;
            },

            buildTabs:function(){
                var self = this;

                this.$('.description-input').val(this.model.getDescription());
                var partsPath = this.model.getPartsPath();

                _.each(partsPath, function(part){
                    self.$('.path-description').append(part.name);
                    self.$('.path-description').append('<i class="fa fa-chevron-right">');
                });

                self.$('.fa.fa-chevron-right').last().remove();

                this.buildAttributesTab();
                this.buildLinkedDocumentTab();
                this.buildFileTab();
            },

            buildAttributesTab:function(){
                var self = this;
                this.attributesView = new AttributesView({});
                this.attributesView.render();
                this.$('#tab-attributes').html(this.attributesView.$el);

                if(!this.isNew) {
                    _.each(this.model.getAttributes(), function (item) {
                        self.attributesView.addAndFillAttribute(new Attribute(item));
                    });
                }
            },

            buildLinkedDocumentTab: function(){
                this.linkedDocuments = new LinkedDocumentCollection(this.model.getDocumentLinked());
                this.linkedDocumentsView = new LinkedDocumentsView({
                    editMode: true,
                    commentEditable:true,
                    collection : this.linkedDocuments
                });
                this.linkedDocumentsView.render();
                this.$('#tab-link').html(this.linkedDocumentsView.$el);
            },

            buildFileTab:function(){

                var filesMapping = _.map(this.model.getAttachedFiles(), function (fullName) {
                    return {
                        fullName: fullName,
                        shortName: _.last(fullName.split('/')),
                        created: true
                    };
                });

                var deleteBaseUrl = App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/products/' + App.config.productId + '/product-instances/' + this.serialNumber + '/pathdata/'+this.model.getId();

                this.attachedFiles = new AttachedFileCollection(filesMapping);

                this.fileListView = new FileListView({
                    deleteBaseUrl: deleteBaseUrl,
                    uploadBaseUrl: this.model.getUploadBaseUrl(this.serialNumber),
                    collection: this.attachedFiles,
                    editMode: true
                });

                this.fileListView.render();

                this.$('#tab-files').html(this.fileListView.$el);
            },

            onSave: function(){
                var self = this;
                this.model.setAttributes(this.attributesView.collection.toJSON());
                this.model.setDescription(this.$('.description-input').val());
                if(this.isNew){
                    //POST
                    var url = App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/products/' + App.config.productId + '/product-instances/' + this.serialNumber + '/pathdata';
                    $.ajax({
                        type: 'POST',
                        url : url,
                        data : JSON.stringify(this.model),
                        contentType : 'application/json',
                        success: function(){
                            self.closeModal();
                        },
                        error : function(errorMessage){
                            self.$('#alerts').append(new AlertView({
                                type: 'error',
                                message: errorMessage
                            }).render().$el);
                        }
                    });
                }else{
                    this.model.setDocumentLinked(this.linkedDocuments.toJSON());
                    this.model.setAttachedFiles(this.attachedFiles.toJSON());
                    //PUT
                    var url = App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/products/' + App.config.productId + '/product-instances/' + this.serialNumber + '/pathdata/'+this.model.getId();
                    $.ajax({
                        type: 'PUT',
                        url : url,
                        data : JSON.stringify(this.model),
                        contentType : 'application/json',
                        success: function(){
                            self.closeModal();
                        },
                        error : function(errorMessage){
                            self.$('#alerts').append(new AlertView({
                                type: 'error',
                                message: errorMessage
                            }).render().$el);
                        }
                    });
                    this.fileListView.deleteFilesToDelete();
                }
            },

            onDelete: function(){
                var self = this;
                var url = App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/products/' + App.config.productId + '/product-instances/' + this.serialNumber + '/pathdata/'+this.model.getId();
                $.ajax({
                    type: 'DELETE',
                    url : url,
                    success: function(){
                        self.closeModal();
                    },
                    error : function(errorMessage){
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

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
        'common-objects/models/attribute'
    ], function (Backbone, Mustache, template, ProductInstanceDataModel, AttributesView, FileListView, LinkedDocumentsView, LinkedDocumentCollection, AttachedFileCollection, Attribute) {

        'use strict';

        var ProductInstanceDataModalView = Backbone.View.extend({

            events: {
                'hidden .modal.product-instance-data-modal': 'onHidden',
                'click .cancel-button' : 'closeModal',
                'click .save-button' : 'onSave'
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
                            self.model.setPath(self.path);
                        }
                        self.buildTabs();
                    },
                    error : function(){
                        console.log('fail to get model');
                    }
                });

                return this;
            },

            buildTabs:function(){
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

                this.attachedFiles = new AttachedFileCollection(filesMapping);

                var _this = this;
                this.fileListView = new FileListView({
                    deleteBaseUrl: this.model.getDeleteBaseUrl(),
                    uploadBaseUrl: _this.model.getUploadBaseUrl(),
                    collection: this.attachedFiles,
                    editMode: true
                });
                this.fileListView.render();

                this.$('#tab-files').html(this.fileListView.$el);
            },

            onSave: function(){
                var self = this;
                this.model.setAttributes(this.attributesView.collection.toJSON());
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
                        error : function(){
                            console.log('fail to post model');
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
                        error : function(){
                            console.log('fail to post model');
                        }
                    });
                }
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

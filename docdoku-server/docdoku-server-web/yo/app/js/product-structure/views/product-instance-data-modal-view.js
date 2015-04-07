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
        'common-objects/collections/file/attached_file_collection'
    ], function (Backbone, Mustache, template, ProductInstanceDataModel, AttributesView, FileListView, LinkedDocumentsView, LinkedDocumentCollection, AttachedFileCollection) {

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
                this.$el.html(Mustache.render(template, {i18n: App.config.i18n}));
                this.$modal = this.$('.modal.product-instance-data-modal');
                var self = this;
                var url = App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/products/' + App.config.productId + '/product-instances/' + this.serialNumber + '/pathdata?path=' + this.path;
                $.ajax({
                    type: 'GET',
                    url : url,
                    success: function(data){
                        self.model = new ProductInstanceDataModel(data);
                        self.buildTabs();
                    },
                    error : function(){

                    }
                });

                this.attributesView = this.$('#tab-attributes');
                this.fileListView = this.$('#tab-files');
                this.linkedDocumentsView = this.$('#tab-link');

                return this;
            },

            buildTabs:function(){
                this.buildAttributesTab();
                this.buildLinkedDocumentTab();
                this.buildFileTab();
            },

            buildAttributesTab:function(){
                var attributesView = new AttributesView({});
                attributesView.render();
                this.attributesView.html(attributesView.$el);
            },

            buildLinkedDocumentTab: function(){
                this.linkedDocuments = new LinkedDocumentCollection(this.model.getDocumentLinked());
                var linkedDocumentsView = new LinkedDocumentsView({
                    editMode: true,
                    commentEditable:true,
                    collection : this.linkedDocuments
                });
                linkedDocumentsView.render();
                this.linkedDocumentsView.html(linkedDocumentsView.$el);
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
                var fileListView = new FileListView({
                    deleteBaseUrl: this.model.getDeleteBaseUrl(),
                    uploadBaseUrl: _this.model.getUploadBaseUrl(),
                    collection: this.attachedFiles,
                    editMode: true
                });
                fileListView.render();

                this.fileListView.html(fileListView.$el);
            },

            onSave: function(){

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

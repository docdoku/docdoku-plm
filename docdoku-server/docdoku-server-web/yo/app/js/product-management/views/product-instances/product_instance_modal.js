/*global _,define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/product-instances/product_instance_modal.html',
    'views/baselines/baselined_part_list',
    'common-objects/utils/date',
    'common-objects/collections/attribute_collection',
    'common-objects/views/attributes/attributes',
    'common-objects/views/file/file_list',
    'common-objects/collections/linked/linked_document_collection',
    'common-objects/views/linked/linked_documents',
    'common-objects/collections/file/attached_file_collection',
    'common-objects/views/alert'
], function (Backbone, Mustache, template, BaselinedPartListView,date,AttributeCollection,ProductInstanceAttributeListView,FileListView,LinkedDocumentCollection,LinkedDocumentsView,AttachedFileCollection,AlertView) {
    'use strict';
    var ProductInstancesModalView = Backbone.View.extend({
        events: {
            'click .btn-primary': 'interceptSubmit',
            'submit #product_instance_edit_form': 'onSubmitForm',
            'hidden #product_instance_modal': 'onHidden',
            'shown #product_instance_modal': 'onShown',
            'click a#previous-iteration': 'onPreviousIteration',
            'click a#next-iteration': 'onNextIteration',
            'close-modal-request':'closeModal'
        },

        template: Mustache.parse(template),

        initialize: function () {
            this.productId = this.options.productId;
            this.iteration = this.model.getLastIteration();
            this.iterations = this.model.getIterations();

        },

        render: function () {
            this.editMode = this.iterations.isLast(this.iteration);
            var data = {
                i18n: App.config.i18n,
                model: this.iteration,
                editMode: this.editMode
            };

            if (this.model.hasIterations()) {
                var hasNextIteration = this.iterations.hasNextIteration(this.iteration);
                var hasPreviousIteration = this.iterations.hasPreviousIteration(this.iteration);
                data.iteration = this.iteration.toJSON();
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
            var that = this;
            this.iteration.initBaselinedParts(that,
                {success: that.initBaselinedPartListView
                });
            this.initAttributesView();
           this.initAttachedFileView();
            this.initLinkedDocumentsView();
            this.openModal();
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
            this.iteration = iteration;
            this.undelegateEvents();
            this.closeModal();
            this.delegateEvents();
            this.render();
        },

        bindDomElements: function () {
            this.$notifications = this.$el.find('.notifications').first();
            this.$modal = this.$('#product_instance_modal');
            this.$inputIterationNote = this.$('#inputIterationNote');
            this.$baselinedPartListArea = this.$('.baselinedPartListArea');
            this.$authorLink = this.$('.author-popover');
        },

        initBaselinedPartListView: function (view) {
            view.baselinePartListView = new BaselinedPartListView({model: view.iteration, editMode:false}).render();
            view.$baselinedPartListArea.html(view.baselinePartListView.$el);
            view.baselinePartListView.renderList();
            view.$baselinedPartListArea.html(view.baselinePartListView.$el);

        },

        initLinkedDocumentsView: function () {
            this.linkedDocumentsView = new LinkedDocumentsView({
                editMode: this.editMode,
                commentEditable:true,
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

        initAttachedFileView:   function(){

            var filesMapping = _.map(this.iteration.getAttachedFiles(), function (fullName) {

                return {
                    'fullName': fullName,
                    shortName: _.last(fullName.split('/')),
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

        bindUserPopover: function () {
            this.$authorLink.userPopover(this.model.getUpdateAuthor(), this.model.getSerialNumber(), 'right');
        },

        interceptSubmit: function () {
            this.isValid = !this.$('.tabs').invalidFormTabSwitcher();
        },

        onSubmitForm: function (e) {
            var _this = this;
            this.iteration = this.iteration.clone();
            this.iteration.unset('iteration');
            this.iteration.setIterationNote(this.$inputIterationNote.val());
            this.iteration.setBaselinedParts(this.baselinePartListView.getBaselinedParts());
            this.iteration.setInstanceAttributes(this.attributesView.collection.toJSON());
            this.iteration.setLinkedDocuments(this.linkedDocumentsView.collection.toJSON());
            //linkedDocuments: 
            /*There is a parsing problem at saving time*/
            var files = this.iteration.get('attachedFiles');

            /*tracking back files*/
            this.iteration.set({
                attachedFiles: files
            });
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
        }
    });
    return ProductInstancesModalView;
});

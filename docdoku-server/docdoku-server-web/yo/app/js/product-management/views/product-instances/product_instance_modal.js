/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/product-instances/product_instance_modal.html',
    'views/baselines/baselined_part_list',
    'common-objects/utils/date',
    'common-objects/views/attributes/attributes',
    'common-objects/views/file/file_list',
    'common-objects/collections/linked/linked_document_collection',
    'common-objects/views/linked/linked_documents',
    'common-objects/views/alert'
], function (Backbone, Mustache, template, BaselinedPartListView,date,ProductInstanceAttributeListView,FileListView,LinkedDocumentCollection,LinkedDocumentsView,AlertView) {
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
                collection: new LinkedDocumentCollection(this.iteration.getLinkedDocuments())
            }).render();

            /* Add the documentLinksView to the tab */
            this.$('#iteration-links').html(this.linkedDocumentsView.el);
        },

        initAttributesView: function () {

            this.attributes = new Backbone.Collection();

            this.attributesView = new ProductInstanceAttributeListView({
                collection: this.attributes
            });

            this.$('#attributes-list').html(this.attributesView.$el);

            this.attributesView.setEditMode(this.editMode);

            var that = this;
            _.each(that.iteration.getInstanceAttributes(), function (object) {
                that.attributesView.collection.add({
                    name: object.name,
                    type: object.type,
                    value: object.value,
                    lovName:object.lovName,
                    mandatory: object.mandatory
                });
            });

            this.attributesView.render();
        },

        initAttachedFileView:   function(){
            this.files = new Backbone.Collection();
            var _this = this;
            this.fileListView = new FileListView({
                deleteBaseUrl: this.iteration.url(),
                uploadBaseUrl: _this.iteration.getUploadBaseUrl(),
                collection: this.files,
                editMode: true
            }).render();

            // Add the fileListView to the tab
            this.$('#tab-products-instances-files').append(this.fileListView.el);
            _.each(_this.iteration.getAttachedFiles(), function (object) {
              //  this.fileListView.upload
            });

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

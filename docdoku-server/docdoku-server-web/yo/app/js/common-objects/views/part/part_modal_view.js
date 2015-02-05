/*global _,define,App*/
define([
    'backbone',
    'mustache',
    'common-objects/views/components/modal',
    'common-objects/views/file/file_list',
    'text!common-objects/templates/part/part_modal.html',
    'common-objects/views/attributes/attributes',
    'common-objects/views/part/parts_management_view',
    'common-objects/views/linked/linked_documents',
    'common-objects/collections/linked/linked_document_collection',
    'common-objects/views/workflow/lifecycle',
    'common-objects/views/part/conversion_status_view',
    'common-objects/utils/date'
], function (Backbone,Mustache, ModalView, FileListView, template, AttributesView, PartsManagementView, LinkedDocumentsView, LinkedDocumentCollection, LifecycleView, ConversionStatusView, date) {
    'use strict';
    var PartModalView = ModalView.extend({

        initialize: function () {
            this.iteration = this.model.getLastIteration();
            this.iterations = this.model.getIterations();

            ModalView.prototype.initialize.apply(this, arguments);
            this.events['click a#previous-iteration'] = 'onPreviousIteration';
            this.events['click a#next-iteration'] = 'onNextIteration';
            this.events['submit #form-part'] = 'onSubmitForm';
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

        render: function () {

            var data = {
                part: this.model,
                i18n: App.config.i18n,
                permalink: this.model.getPermalink()
            };

            this.editMode = this.model.isCheckoutByConnectedUser() && this.iterations.isLast(this.iteration);
            data.editMode = this.editMode;
            data.isLockedMode = !this.iteration || (this.model.isCheckout() && this.model.isLastIteration(this.iteration.getIteration()) && !this.model.isCheckoutByConnectedUser());

            if (this.model.hasIterations()) {
                var hasNextIteration = this.iterations.hasNextIteration(this.iteration);
                var hasPreviousIteration = this.iterations.hasPreviousIteration(this.iteration);
                data.iteration = this.iteration.toJSON();
                data.iteration.hasNextIteration = hasNextIteration;
                data.iteration.hasPreviousIteration = hasPreviousIteration;
                data.reference = this.iteration.getReference();
                data.iteration.creationDate = date.formatTimestamp(
                    App.config.i18n._DATE_FORMAT,
                    data.iteration.creationDate
                );
            }

            this.$el.html(Mustache.render(template, data));

            this.$authorLink = this.$('.author-popover');
            this.$checkoutUserLink = this.$('.checkout-user-popover');

            this.$inputIterationNote = this.$('#inputRevisionNote');
            this.$tabs = this.$('.nav-tabs li');

            this.bindUserPopover();
            if (this.iteration) {
                this.initCadFileUploadView();
                this.initAttributesView();

                this.initPartsManagementView();
                this.initLinkedDocumentsView();
                this.initLifeCycleView();
            }

            date.dateHelper(this.$('.date-popover'));
            return this;
        },

        bindUserPopover: function () {
            this.$authorLink.userPopover(this.model.getAuthorLogin(), this.model.getNumber(), 'right');
            if (this.model.isCheckout()) {
                this.$checkoutUserLink.userPopover(this.model.getCheckOutUserLogin(), this.model.getNumber(), 'right');
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

        },

        onSubmitForm: function (e) {

            // cannot pass a collection of cad file to server.
            var cadFile = this.fileListView.collection.first();
            if (cadFile) {
                this.iteration.set('nativeCADFile', cadFile.get('fullName'));
            } else {
                this.iteration.set('nativeCADFile', '');
            }

            this.iteration.save({
                iterationNote: this.$inputIterationNote.val(),
                components: this.partsManagementView.collection.toJSON(),
                instanceAttributes: this.attributesView.collection.toJSON(),
                linkedDocuments: this.linkedDocumentsView.collection.toJSON()
            }, {success: function () {
                Backbone.Events.trigger('part:saved');
            }});


            this.fileListView.deleteFilesToDelete();

            this.hide();

            e.preventDefault();
            e.stopPropagation();

            return false;
        },

        initCadFileUploadView: function () {

            this.fileListView = new FileListView({
                baseName: this.iteration.getBaseName(),
                deleteBaseUrl: this.iteration.url(),
                uploadBaseUrl: this.iteration.getUploadBaseUrl(),
                collection: this.iteration._nativeCADFile,
                editMode: this.editMode,
                singleFile: true
            }).render();

            this.$('#iteration-files').html(this.fileListView.el);

            if(this.editMode){
                this.conversionStatusView = new ConversionStatusView({model:this.iteration}).render();
                this.$('#iteration-files').append(this.conversionStatusView.el);
            }

        },

        initPartsManagementView: function () {
            this.partsManagementView = new PartsManagementView({
                el: '#iteration-components',
                collection: new Backbone.Collection(this.iteration.getComponents()),
                editMode: this.editMode,
                model: this.model
            }).render();
        },

        initLinkedDocumentsView: function () {
            this.linkedDocumentsView = new LinkedDocumentsView({
                editMode: this.editMode,
                documentIteration: this.iteration,
                collection: new LinkedDocumentCollection(this.iteration.getLinkedDocuments())
            }).render();

            /* Add the documentLinksView to the tab */
            this.$('#iteration-links').html(this.linkedDocumentsView.el);
        },

        initLifeCycleView: function () {
            var that = this;
            if (this.model.get('workflow')) {

                this.lifecycleView = new LifecycleView({
                    el: '#tab-iteration-lifecycle'
                }).setAbortedWorkflowsUrl(this.model.getUrl() + '/aborted-workflows').setWorkflow(this.model.get('workflow')).setEntityType('parts').render();

                this.lifecycleView.on('lifecycle:change', function () {
                    that.model.fetch({success: function () {
                        that.lifecycleView.setAbortedWorkflowsUrl(that.model.getUrl() + '/aborted-workflows').setWorkflow(that.model.get('workflow')).setEntityType('parts').render();
                    }});
                });

            } else {
                this.$('a[href=#tab-iteration-lifecycle]').hide();
            }
        }

    });

    return PartModalView;

});

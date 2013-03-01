define([
    "common-objects/views/components/modal",
    "common-objects/views/file/file_list",
    "common-objects/views/attributes/attributes",
    "views/document/document_lifecycle",
    "common-objects/views/linked_document/linked_documents",
    "models/tag",
    "views/document/document_tag",
    "common-objects/collections/linked_document_collection",
    "text!templates/iteration/document_iteration.html",
    "i18n!localization/nls/document-management-strings",
    "common-objects/utils/date"
], function (ModalView, FileListView, DocumentAttributesView, LifecycleDocumentView, LinkedDocumentsView, Tag, TagView, LinkedDocumentCollection, template, i18n, date) {

    var IterationView = ModalView.extend({

        template:Mustache.compile(template),

        initialize: function() {

            this.iteration = this.model.getLastIteration();
            this.iterations = this.model.getIterations();

            ModalView.prototype.initialize.apply(this, arguments);

            this.events["click a#previous-iteration"] = "onPreviousIteration";
            this.events["click a#next-iteration"] = "onNextIteration";

            this.tagsToRemove = [];

        },

        onPreviousIteration: function() {
            if (this.iterations.hasPreviousIteration(this.iteration)) {
                this.switchIteration(this.iterations.previous(this.iteration));
            }
            return false;
        },

        onNextIteration: function() {
            if (this.iterations.hasNextIteration(this.iteration)) {
                this.switchIteration(this.iterations.next(this.iteration));
            }
            return false;
        },

        switchIteration: function(iteration) {
            this.iteration = iteration;
            var activeTabIndex = this.getActiveTabIndex();
            this.render();
            this.activateTab(activeTabIndex);
        },

        getActiveTabIndex: function() {
            return this.tabs.filter('.active').index();
        },

        activateTab:function(index) {
            this.tabs.eq(index).children().tab('show');
        },

        validation: function() {

            /*checking attributes*/
            var ok = true;
            var attributes = this.attributesView.model;
            attributes.each(function (item) {
                if (!item.isValid()) {
                    ok = false;
                }
            });
            if (!ok) {
                this.getPrimaryButton().attr("disabled", "disabled");
            } else {
                this.getPrimaryButton().removeAttr("disabled");
            }
        },

        render: function() {
            this.deleteSubViews();

            var editMode = this.model.isCheckoutByConnectedUser() && this.iterations.isLast(this.iteration);

            var data = {
                isCheckoutByConnectedUser: this.model.isCheckoutByConnectedUser(),
                isCheckout: this.model.isCheckout(),
                editMode: editMode,
                master: this.model.toJSON(),
                i18n: i18n,
                permalink : this.model.getPermalink()
            };

            data.master.creationDate = date.formatTimestamp(
                i18n._DATE_FORMAT,
                data.master.creationDate
            );

            if (this.model.hasIterations()) {
                var hasNextIteration = this.iterations.hasNextIteration(this.iteration);
                var hasPreviousIteration = this.iterations.hasPreviousIteration(this.iteration);
                data.iteration = this.iteration.toJSON();
                data.iteration.hasNextIteration = hasNextIteration;
                data.iteration.hasPreviousIteration = hasPreviousIteration;
                data.reference = this.iteration.getReference();
                data.iteration.creationDate = date.formatTimestamp(
                    i18n._DATE_FORMAT,
                    data.iteration.creationDate
                );
            }

            if (this.model.isCheckout()) {
                data.master.checkOutDate = date.formatTimestamp(
                    i18n._DATE_FORMAT,
                    data.master.checkOutDate
                );
            }

            /*Main window*/
            var html = this.template(data);
            this.$el.html(html);

            this.tabs = this.$('.nav-tabs li');


            this.customAttributesView =
                this.addSubView(
                    new DocumentAttributesView({
                        el:"#iteration-additional-attributes-container"
                    })
            );

            this.customAttributesView.setEditMode(editMode);
            this.customAttributesView.render();

            var that = this;

            if (this.model.hasIterations()) {
                this.iteration.getAttributes().each(function (item) {
                    that.customAttributesView.addAndFillAttribute(item);
                });

                this.fileListView = new FileListView({
                    baseName: this.iteration.getWorkspace() + "/documents/" + this.iteration.getDocumentMasterId() + "/" + this.iteration.getDocumentMasterVersion() + "/" + this.iteration.getIteration(),
                    deleteBaseUrl: this.iteration.url(),
                    uploadBaseUrl: this.iteration.getUploadBaseUrl(),
                    collection:this.iteration.getAttachedFiles(),
                    editMode: editMode
                }).render();

                /* Add the fileListView to the tab */
                this.$("#iteration-files").html(this.fileListView.el);


                this.linkedDocumentsView = new LinkedDocumentsView({
                    editMode: editMode,
                    documentIteration: this.iteration,
                    collection: new LinkedDocumentCollection(this.iteration.getLinkedDocuments())
                }).render();

                /* Add the documentLinksView to the tab */
                this.$("#iteration-links").html(this.linkedDocumentsView.el);
            }

            if(this.model.get("workflow")){
                this.lifecycleView =  new LifecycleDocumentView({
                    el:"#tab-iteration-lifecycle"
                }).setWorkflow(this.model.get("workflow")).render();
                this.$("#iteration-lifecycle").html(this.lifecycleView.el);
            }else{
                this.$("a[href=#tab-iteration-lifecycle]").hide();
            }

            this.$(".author-popover").userPopover(this.model.attributes.author.login, this.model.id, "right");

            if (this.model.isCheckout()) {
                this.$(".checkout-user-popover").userPopover(this.model.getCheckoutUser().login, this.model.id, "right");
            }

            this.tagsManagement(editMode);

            return this;
        },

        primaryAction: function() {

            /*saving iteration*/
            this.iteration.save({
                revisionNote: this.$('#inputRevisionNote').val(),
                instanceAttributes: this.customAttributesView.collection.toJSON(),
                linkedDocuments: this.linkedDocumentsView.collection.toJSON()
            });

            /*There is a parsing problem at saving time*/
            var files = this.iteration.get("attachedFiles");

            /*tracking back files*/
            this.iteration.set({
                attachedFiles:files
            });

            /*
             *saving new files : nothing to do : it's already saved
             *deleting unwanted files
             */
            this.fileListView.deleteFilesToDelete();

            /*
            * Delete tags if needed
            * */

            this.deleteClickedTags();


            this.hide();

        },

        cancelAction: function() {

            if (this.model.hasIterations()) {
                //Abort file upload and delete new files
                this.fileListView.deleteNewFiles();
            }
            ModalView.prototype.cancelAction.call(this);
        },

        getPrimaryButton: function() {
            var button = this.$("div.modal-footer button.btn-primary");
            return button;
        },

        tagsManagement: function (editMode) {

            var $tagsZone = this.$(".master-tags-list");
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
                    }} :
                {
                    model: new Tag({id: tagLabel, label: tagLabel}),
                    isAdded: false,
                    clicked: null
                };

                tagView = new TagView(tagViewParams).render();

                $tagsZone.append(tagView.el);

            });

        },

        deleteClickedTags:function(){
            var that = this ;
            if(this.tagsToRemove.length){
                that.model.removeTags(this.tagsToRemove, function(){
                    if(that.model.collection.parent) {
                        if(_.contains(that.tagsToRemove, that.model.collection.parent.id)){
                            that.model.collection.remove(that.model);
                        }
                    }
                });
            }
        }

    });
    return IterationView;
});
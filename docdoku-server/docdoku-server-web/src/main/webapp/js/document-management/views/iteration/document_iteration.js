define([
    "views/components/modal",
    "views/file_list",
    "views/document/document_attributes",
    "text!templates/iteration/document_iteration.html",
    "i18n!localization/nls/document-management-strings",
    "common/date"
], function (ModalView, FileListView, DocumentAttributesView, template, i18n, date) {

    var IterationView = ModalView.extend({

        template:Mustache.compile(template),

        initialize: function() {

            /*we are fetching the last iteration*/
            this.iteration = this.model.getLastIteration();

            ModalView.prototype.initialize.apply(this, arguments);

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

            var editMode = this.model.isCheckoutByConnectedUser();

            var data = {
                editMode: editMode,
                master: this.model.toJSON(),
                i18n: i18n
            }

            data.master.creationDate = date.formatTimestamp(
                i18n._DATE_FORMAT,
                data.master.creationDate
            );

            if (this.model.hasIterations()) {
                data.iteration = this.iteration.toJSON();
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
                    deleteBaseUrl: this.iteration.url(),
                    uploadBaseUrl: this.iteration.getUploadBaseUrl(),
                    collection:this.iteration.getAttachedFiles(),
                    editMode: editMode
                }).render();
    
                /* Add the fileListView to the tab */
                $("#iteration-files").append(this.fileListView.el);
            }

            return this;
        },

        primaryAction: function() {

            /*saving iteration*/
            this.iteration.save({
                revisionNote: this.$('#inputRevisionNote').val(),
                instanceAttributes: this.customAttributesView.collection.toJSON()
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
            var filesToDelete = this.fileListView.filesToDelete;

            /*we need to reverse read because model.destroy() remove elements from collection*/
            while (filesToDelete.length != 0) {
                var file = filesToDelete.pop();
                file.destroy({
                    error:function () {
                        alert("file " + file + " could not be deleted");
                    }
                });
            }

            this.hide();

        },

        cancelAction: function() {

            if (this.model.hasIterations()) {

                //Abort file upload if there is one
                if(!_.isUndefined(this.fileListView.xhr))
                    this.fileListView.xhr.abort();

                /*deleting unwanted files that have been added by upload*/
                var filesToDelete = this.fileListView.newItems;

                /*we need to reverse read because model.destroy() remove elements from collection*/
                while (filesToDelete.length != 0) {
                    var file = filesToDelete.pop();
                    file.destroy({
                        error:function () {
                            alert("file " + file + " could not be deleted");
                        }
                    });
                }
            }
            ModalView.prototype.cancelAction.call(this);
        },

        getPrimaryButton: function() {
            var button = this.$el.find("div.modal-footer button.btn-primary");
            return button;
        }

    });
    return IterationView;
});
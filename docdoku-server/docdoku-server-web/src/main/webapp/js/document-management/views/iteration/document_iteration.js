define([
    "views/components/modal",
    "views/iteration/file_editor",
    "views/components/editable_list_view",
    "views/document/document_attributes",
    "text!templates/iteration/document_iteration.html",
    "i18n!localization/nls/document-management-strings",
    "common/date"
], function (ModalView, FileEditor, EditableListView, DocumentAttributesView, template, i18n, date) {

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

            var data = {
                editMode:  this.model.isCheckout(),
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

            this.customAttributesView.setEditMode(this.model.isCheckout());
            this.customAttributesView.render();

            var that = this;

            if (this.model.hasIterations()) {
                this.iteration.getAttributes().each(function (item) {
                    that.customAttributesView.addAndFillAttribute(item);
                });

                /* Editor : generate a View for each file and handle the upload progress bar*/
                this.fileEditor = new FileEditor({
                    documentIteration:this.iteration
                });
    
                /* ListView */
                this.filesView = new EditableListView({
                    model:this.iteration.getAttachedFiles(), /*domain objects set directly in view.model*/
                    editable:true, /*we will have to look at view.options.editable*/
                    listName:"Attached files for " + this.iteration,
                    editor : this.fileEditor
                }).render();
                this.fileEditor.setWidget(this.filesView);
    
                /* Add the ListView to the tab */
                $("#iteration-files").append(this.filesView.$el);
    
                this.cutomizeRendering();
    
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

            var filesToDelete = this.filesView.selection;

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

                /*deleting unwanted files that have been added by upload*/
                var filesToDelete = this.filesView.newItems;

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

        /*
         * Here are some jquery adjustments to render the list specially
         */

        cutomizeRendering: function() {

            this.filesView.on("list:selected", function (selectedObject, index, line) {
                line.addClass("stroke");
                line.find("a").addClass("stroke");
            });

            this.filesView.on("list:unselected", function (selectedObject, index, line) {
                line.find(".stroke").removeClass("stroke");
                line.removeClass("stroke")
            });

            this.fileEditor.render();
        },

        getPrimaryButton: function() {
            var button = this.$el.find("div.modal-footer button.btn-primary");
            kumo.assertNotEmpty(button, "can't find primary button");
            return button;
        }

    });
    return IterationView;
});
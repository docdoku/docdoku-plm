define(
    [
        "common-objects/views/components/modal",
        "text!templates/part_template_creation_view.html",
        "i18n!localization/nls/document-management-strings",
        "models/part_template",
        "common-objects/views/file/file_list",
        "common-objects/views/attributes/template_new_attributes"
    ],
    function (ModalView, template, i18n, PartTemplate, FileListView, TemplateNewAttributesView) {

    var PartTemplateEditView = ModalView.extend({


        template: Mustache.compile(template),

        initialize: function () {
            ModalView.prototype.initialize.apply(this, arguments);
            this.events["submit #part_template_creation_form"]="onSubmitForm";
        },

        rendered: function () {

            this.$(".tabs").tabs();

            this.bindDomElements();

            this.attributesView = this.addSubView(
                new TemplateNewAttributesView({
                    el: "#tab-attributes",
                    attributesLocked: this.model.isAttributesLocked()
                })
            ).render();


            this.fileListView = new FileListView({
                baseName: this.model.getBaseName(),
                deleteBaseUrl: this.model.url(),
                uploadBaseUrl: this.model.getUploadBaseUrl(),
                collection: this.model._attachedFile,
                editMode: true,
                singleFile: true
            }).render();

            this.$("#tab-files").append(this.fileListView.el);

            this.attributesView.collection.reset(this.model.get("attributeTemplates"));

            this.$("a#mask-help").popover({
                title: i18n.MASK,
                placement: "left",
                html: true,
                content: i18n.MASK_HELP
            });

        },

        bindDomElements:function(){
            this.$partTemplateReference = this.$("#part-template-reference");
            this.$partTemplateType = this.$("#part-template-type");
            this.$partTemplateMask = this.$("#part-template-mask");
            this.$partTemplateIdGenerated = this.$("#part-template-id-generated");
        },

        onSubmitForm: function(e) {

            // cannot pass a collection of cad file to server.
            var attachedFile = this.fileListView.collection.first();
            if(attachedFile){
                this.model.set("attachedFile", attachedFile.get("fullName"));
            }else{
                this.model.set("attachedFile","");
            }

            this.model.save({
                id: this.$partTemplateReference.val(),
                partType:this.$partTemplateType.val(),
                mask: this.$partTemplateMask.val(),
                idGenerated:  this.$partTemplateIdGenerated.is(":checked"),
                attributeTemplates:this.attributesView.collection.toJSON(),
                attributesLocked:this.attributesView.isAttributesLocked()
            }, {
                wait: true,
                success: this.onPartTemplateCreated,
                error: this.onError
            });

            this.fileListView.deleteFilesToDelete();

            e.preventDefault();
            e.stopPropagation();
            return false ;
        },

        cancelAction: function() {
            this.fileListView.deleteNewFiles();
        },

        onPartTemplateCreated: function() {
            this.model.fetch();
            this.hide();
        },

        onError: function(model, error) {
            alert(i18n.CREATION_ERROR + " : " + error.responseText);
        }

    });

    return PartTemplateEditView;

});
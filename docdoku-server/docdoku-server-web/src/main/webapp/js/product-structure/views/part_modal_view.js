define(
    [
    "common-objects/views/components/modal",
    "common-objects/views/file/file_list",
    'text!templates/part_modal.html',
    'i18n!localization/nls/product-structure-strings',
    "common-objects/views/attributes/attributes"
    ],
    function(ModalView, FileListView, template, i18n, PartAttributesView ) {

    var PartModalView = ModalView.extend({

        template: Mustache.compile(template),

        initialize:function(){
            this.iteration = this.model.getLastIteration();
            ModalView.prototype.initialize.apply(this, arguments);
            /*
             this.events["click a#next-iteration"] = "onNextIteration";
             this.events["click a#previous-iteration"] = "onPreviousIteration";
             */
            this.events["submit #form-part"] = "onSubmitForm";
        },

        render: function() {
            this.$el.html(this.template({
                part: this.model,
                i18n: i18n
            }));

            this.editMode = this.model.isCheckoutByConnectedUser() ;

            this.$authorLink = this.$('.author-popover');
            this.$checkoutUserLink = this.$('.checkout-user-popover');
            this.bindUserPopover();
            this.initAttributesView();
            this.initCadFileUploadView();
            return this;
        },

        bindUserPopover: function() {
            this.$authorLink.userPopover(this.model.getAuthorLogin(), this.model.getNumber(), "right");
            if(this.model.isCheckout()){
                this.$checkoutUserLink.userPopover(this.model.getCheckOutUserLogin(), this.model.getNumber(),"right");
            }
        },

        initAttributesView:function(){

            var that = this ;

            this.attributes = new Backbone.Collection();

            this.partAttributesView = new PartAttributesView({
                el:this.$("#attributes-list")
            });

            this.partAttributesView.setEditMode(this.editMode);
            this.partAttributesView.render();

            _.each(this.model.getLastIteration().getAttributes().models ,function(item){
                that.partAttributesView.addAndFillAttribute(item);
            });

        },

        onSubmitForm:function(e){

            // cannot pass a collection of cad file to server.
            var cadFile = this.iteration.get("nativeCADFile").first();
            if(cadFile){
                this.iteration.set("nativeCADFile", cadFile.get("fullName"));
            }

            this.iteration.save({
                instanceAttributes: this.partAttributesView.collection.toJSON()
            });

            this.fileListView.deleteFilesToDelete();

            this.hide();

            e.preventDefault();
            e.stopPropagation();
            return false ;
        },

        initCadFileUploadView:function(){

            this.fileListView = new FileListView({
                baseName: this.iteration.getBaseName(),
                deleteBaseUrl: this.iteration.url(),
                uploadBaseUrl: this.iteration.getUploadBaseUrl(),
                collection: this.iteration.get("nativeCADFile"),
                editMode:this.editMode
            }).render();

            this.$("#iteration-files").html(this.fileListView.el);

        }

    });

    return PartModalView;

});
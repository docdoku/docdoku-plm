define(["common-objects/views/components/modal",
    "common-objects/views/file/file_list",
    'text!templates/part_modal.html',
    'i18n!localization/nls/product-structure-strings',
    "common-objects/views/attributes/attributes"],
    function(ModalView, FileListView, template, i18n, PartAttributesView) {

    var PartModalView = ModalView.extend({

        template: Mustache.compile(template),

        events: {
            "submit #form-part":"onSubmitForm",
            "hidden #part-modal": "onHidden"
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
            return this;
        },

        onHidden: function() {
            this.remove();
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

            this.model.save({
                instanceAttributes: this.attributesView.collection.toJSON()
            });

            e.preventDefault();
            e.stopPropagation();
            return false ;
        }

    });

    return PartModalView;

});
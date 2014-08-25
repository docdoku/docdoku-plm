define([
    "text!templates/product-instances/product_instances_list_item.html",
    "i18n!localization/nls/product-instances-strings"
], function (
    template,
    i18n
    ) {
    var ProductInstancesListItemView = Backbone.View.extend({

        template: Mustache.compile(template),

        events:{
            "click input[type=checkbox]":"selectionChanged",
            "click td.reference":"openEditView"
        },

        tagName:"tr",

        initialize: function () {
            this._isChecked = false ;
        },

        render:function(){
            this.$el.html(this.template({model:this.model, i18n:i18n}));
            this.$checkbox = this.$("input[type=checkbox]");
            this.model.on("change",this.render,this);
            this.bindUserPopover();
            this.trigger("rendered",this);
            return this;
        },

        selectionChanged:function(){
            this._isChecked = this.$checkbox.prop("checked");
            this.trigger("selectionChanged",this);
        },

        isChecked:function(){
            return this._isChecked;
        },

        check:function(){
            this.$checkbox.prop("checked", true);
            this._isChecked = true;
        },

        bindUserPopover:function(){
            this.$(".author-popover").userPopover(this.model.getUpdateAuthor(),this.model.getSerialNumber(),"left");
        },

        unCheck:function(){
            this.$checkbox.prop("checked", false);
            this._isChecked = false;
        },

        openEditView:function(){
            var that = this;
            require(["views/product-instances/product_instance_modal"],function(ProductInstanceModalView){
                var view = new ProductInstanceModalView({model:that.model});
                view.render().openModal();
                $("body").append(view.el);
            });
        }
    });

    return ProductInstancesListItemView;
});

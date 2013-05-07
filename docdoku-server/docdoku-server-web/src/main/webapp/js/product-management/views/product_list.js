define([
    "text!templates/product_list.html",
    "i18n!localization/nls/product-management-strings",
    "views/product_list_item"
], function (
    template,
    i18n,
    ProductListItemView
    ) {
    var ProductListView = Backbone.View.extend({

        template: Mustache.compile(template),

        events:{
            "click .toggle-checkboxes":"toggleSelection"
        },

        initialize: function () {
            _.bindAll(this);
            this.listenTo(this.collection, "reset", this.resetList);
            this.listenTo(this.collection, 'add', this.addNewProduct);
            this.listenTo(this.collection, 'remove', this.removeProduct);
            this.listItemViews = [];
        },

        render:function(){
            this.$el.html(this.template({i18n:i18n}));
            this.bindDomElements();
            this.collection.fetch({reset:true});
            return this;
        },

        bindDomElements:function(){
            this.$items = this.$(".items");
            this.$checkbox = this.$(".toggle-checkboxes");
        },

        resetList:function(){
            var that = this;
            this.$items.empty();
            this.collection.each(function(model){
                that.addProduct(model);
            });
        },

        pushProduct:function(product){
            this.collection.push(product);
        },

        addNewProduct:function(model){
            this.addProduct(model,true);
        },

        addProduct:function(model,effect){
            var view = this.addProductView(model);
            this.$el.removeClass("hide");
            if(effect){
                view.$el.highlightEffect();
            }
        },

        removeProduct:function(model){
            this.removeProductView(model);
            if(!this.collection.size()){
                this.$el.hide();
            }
        },

        removeProductView:function(model){

            var viewToRemove = _(this.listItemViews).select(function(view){
                return view.model == model;
            })[0];

            if(viewToRemove){
                this.listItemViews = _(this.listItemViews).without(viewToRemove);
                viewToRemove.remove();
            }

            if( this.listItemViews.length == 0){
                this.$el.addClass("hide");
            }

        },

        addProductView:function(model){
            var view = new ProductListItemView({model:model}).render();
            this.listItemViews.push(view);
            this.$items.append(view.$el);
            view.on("selectionChanged",this.onSelectionChanged);
            return view;
        },

        toggleSelection:function(){
            if(this.$checkbox.is(":checked")){
                _(this.listItemViews).each(function(view){
                    view.check();
                });
            }else{
                _(this.listItemViews).each(function(view){
                    view.unCheck();
                });
            }
            this.onSelectionChanged();
        },

        onSelectionChanged:function(view){

            var checkedViews = _(this.listItemViews).select(function(view){
                return view.isChecked();
            });

            if (checkedViews.length <= 0){
                this.onNoProductSelected();
            }else if(checkedViews.length == 1){
                this.onOneProductSelected();
            }else {
                this.onSeveralProductsSelected();
            }

        },

        onNoProductSelected:function(){
            this.trigger("delete-button:display",false);
        },

        onOneProductSelected:function(){
            this.trigger("delete-button:display",true);
        },

        onSeveralProductsSelected:function(){
            this.trigger("delete-button:display",true);
        },

        deleteSelectedProducts:function(){
            if(confirm("Delete Products")){
                _(this.listItemViews).each(function(view){
                    if(view.isChecked()){
                        view.model.destroy();
                    }
                });
            }
        }

    });

    return ProductListView;

});

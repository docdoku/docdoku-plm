define([
    "text!templates/product_list.html",
    "i18n!localization/nls/product-management-strings",
    "views/product_list_item",
    "i18n!localization/nls/datatable-strings"
], function (
    template,
    i18n,
    ProductListItemView,
    i18nDt
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
            this.listItemViews = [];
        },

        render:function(){
            this.collection.fetch({reset:true});
            return this;
        },

        bindDomElements:function(){
            this.$items = this.$(".items");
            this.$checkbox = this.$(".toggle-checkboxes");
        },

        resetList:function(){
            if(this.oTable){
                this.oTable.fnDestroy();
            }
            this.$el.html(this.template({i18n:i18n}));
            this.bindDomElements();
            this.listItemViews=[];
            var that = this;
            this.collection.each(function(model){
                that.addProduct(model);
            });
            this.dataTable();
        },

        pushProduct:function(product){
            this.collection.push(product);
        },

        addNewProduct:function(model){
            this.addProduct(model,true);
            this.redraw();
        },

        addProduct:function(model,effect){
            var view = this.addProductView(model);
            if(effect){
                view.$el.highlightEffect();
            }
        },

        removeProduct:function(model){
            this.removeProductView(model);
            this.redraw();
        },

        removeProductView:function(model){

            var viewToRemove = _(this.listItemViews).select(function(view){
                return view.model == model;
            })[0];

            if(viewToRemove){
                this.listItemViews = _(this.listItemViews).without(viewToRemove);
                var row = viewToRemove.$el.get(0);
                this.oTable.fnDeleteRow(this.oTable.fnGetPosition(row));
                viewToRemove.remove();
            }

        },

        addProductView:function(model){
            var view = new ProductListItemView({model:model}).render();
            this.listItemViews.push(view);
            this.$items.append(view.$el);
            view.on("selectionChanged",this.onSelectionChanged);
            view.on("rendered",this.redraw);
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

        onSelectionChanged:function(){

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
            this.trigger("create-baseline-button:display",false);
        },

        onOneProductSelected:function(){
            this.trigger("delete-button:display",true);
            this.trigger("create-baseline-button:display",true);
        },

        onSeveralProductsSelected:function(){
            this.trigger("delete-button:display",true);
            this.trigger("create-baseline-button:display",false);
        },

        getSelectedProduct:function(){
            var model = null;
            _(this.listItemViews).each(function(view){
                if(view.isChecked()){
                    model = view.model;
                }
            });
            return model;
        },

        deleteSelectedProducts:function(){
            var that = this;
            if(confirm(i18n["DELETE_SELECTION_?"])){
                _(this.listItemViews).each(function(view){
                    if(view.isChecked()){
                        view.model.destroy({success:function(){
                            that.removeProduct(view.model);
                            that.onSelectionChanged();
                        },error:function(model,err){
                            alert(err.responseText);
                            that.onSelectionChanged();
                        }});
                    }
                });
            }
        },
        redraw:function(){
            this.dataTable();
        },
        dataTable:function(){
            var oldSort = [[0,"asc"]];
            if(this.oTable){
                oldSort = this.oTable.fnSettings().aaSorting;
                this.oTable.fnDestroy();
            }
            this.oTable = this.$el.dataTable({
                aaSorting:oldSort,
                bDestroy:true,
                iDisplayLength:-1,
                oLanguage:{
                    sSearch: "<i class='icon-search'></i>",
                    sEmptyTable:i18nDt.NO_DATA,
                    sZeroRecords:i18nDt.NO_FILTERED_DATA
                },
                sDom : 'ft',
                aoColumnDefs: [
                    { "bSortable": false, "aTargets": [ 0,3 ] }
                ]
            });
            this.$el.parent().find(".dataTables_filter input").attr("placeholder",i18nDt.FILTER);
        }

    });

    return ProductListView;

});

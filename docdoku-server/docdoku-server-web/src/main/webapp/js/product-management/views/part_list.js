define([
    "text!templates/part_list.html",
    "i18n!localization/nls/product-management-strings",
    "views/part_list_item",
    "i18n!localization/nls/datatable-strings"
], function (
    template,
    i18n,
    PartListItemView,
    i18nDt
    ) {
    var PartListView = Backbone.View.extend({

        template: Mustache.compile(template),

        events:{
            "click .toggle-checkboxes":"toggleSelection"
        },

        initialize: function () {
            _.bindAll(this);
            this.listenTo(this.collection, "reset", this.resetList);
            this.listenTo(this.collection, 'add', this.addNewPart);
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
            this.$el.html(this.template({i18n:i18n}));
            this.bindDomElements();
            var that = this;
            this.listItemViews = [];
            this.collection.each(function(model){
                that.addPart(model);
            });
            this.$el.dataTable().fnDestroy();
            this.$el.dataTable({
                bDestroy:true,
                iDisplayLength:-1,
                oLanguage:{
                    sSearch: "<i class='icon-search'></i>"
                },
                sDom : 'ft',
                aoColumnDefs: [
                    { "bSortable": false, "aTargets": [ 0,11,12 ] }
                ]
            });
            this.$el.parent().find(".dataTables_filter input").attr("placeholder", i18nDt.FILTER);
        },

        pushPart:function(part){
            this.collection.push(part);
        },

        addNewPart:function(model){
            this.addPart(model,true);
        },

        addPart:function(model, effect){
            var view = this.addPartView(model);
            this.$el.removeClass("hide");
            if(effect){
                view.$el.highlightEffect();
            }
        },

        removePart:function(model){
            this.removePartView(model);
        },

        removePartView:function(model){

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

        addPartView:function(model){
            var view = new PartListItemView({model:model}).render();
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

            var checkedViews = _(this.listItemViews).select(function(itemView){
                return itemView.isChecked();
            });

            if (checkedViews.length <= 0){
                this.onNoPartSelected();
            }else if(checkedViews.length == 1){
                this.onOnePartSelected();

                if (checkedViews[0].model.isCheckout()) {
                    if (checkedViews[0].model.isCheckoutByConnectedUser()) {
                        this.trigger("checkout-group:update",{canCheckout:false, canUndoAndCheckin:true});
                    } else {
                        this.trigger("checkout-group:update",{canCheckout:false, canUndoAndCheckin:false});
                    }
                } else {
                    this.trigger("checkout-group:update",{canCheckout:true, canUndoAndCheckin:false});
                }

            }else {
                this.onSeveralPartsSelected();
            }

        },

        onNoPartSelected:function(){
            this.trigger("delete-button:display",false);
            this.trigger("checkout-group:display",false);
            this.trigger("acl-edit-button:display",false);
        },

        onOnePartSelected:function(){
            this.trigger("delete-button:display",true);
            this.trigger("checkout-group:display",true);
            var partSelected = this.getSelectedPart();
            this.trigger("acl-edit-button:display", partSelected ? (APP_CONFIG.workspaceAdmin || partSelected.getAuthorLogin() == APP_CONFIG.login) : false);
        },

        onSeveralPartsSelected:function(){
            this.trigger("delete-button:display",true);
            this.trigger("checkout-group:display",false);
            this.trigger("acl-edit-button:display",false);
        },

        deleteSelectedParts:function(){
            var that = this ;
            if(confirm(i18n["DELETE_SELECTION_?"])){
                _(this.listItemViews).each(function(view){
                    if(view.isChecked()){
                        view.model.destroy({success:function(){
                            that.removePart(view.model);
                        },error:function(model,err){
                            alert(err.responseText)
                        }});
                    }
                });
                this.onNoPartSelected();
            }
        },

        getSelectedPart:function(){
            var checkedView = _(this.listItemViews).select(function(itemView){
                return itemView.isChecked();
            })[0];

            if(checkedView){
                return checkedView.model;
            }

        }

    });

    return PartListView;

});

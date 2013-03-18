define([
    "text!templates/part_template_list.html",
    "i18n!localization/nls/product-management-strings",
    "views/part_template_list_item"
], function (
    template,
    i18n,
    PartTemplateListItemView
    ) {
    var PartTemplateListView = Backbone.View.extend({

        template: Mustache.compile(template),

        events:{
            "click .toggle-checkboxes":"toggleSelection"
        },

        initialize: function () {
            _.bindAll(this);
            this.listenTo(this.collection, "reset", this.resetList);
            this.listenTo(this.collection, 'add', this.addPartTemplate);
            this.listItemViews = [];
        },

        render:function(){
            this.$el.html(this.template({i18n:i18n}));
            this.bindDomElements();
            this.collection.fetch();
            return this;
        },

        bindDomElements:function(){
            this.$items = this.$(".items");
            this.$checkbox = this.$(".toggle-checkboxes");
        },

        resetList:function(){
            var that = this;
            this.listItemViews = [];
            this.$items.empty();
            this.collection.each(function(model){
                that.addPartTemplate(model);
            });
        },

        pushPartTemplate:function(partTemplate){
            this.collection.push(partTemplate);
        },

        addPartTemplate:function(model){
            this.addPartTemplateView(model)
        },

        removePartTemplate:function(model){
            this.removePartTemplateView(model);
        },

        removePartTemplateView:function(model){

            var viewToRemove = _(this.listItemViews).select(function(view){
                return view.model == model;
            })[0];

            if(viewToRemove){
                this.listItemViews = _(this.listItemViews).without(viewToRemove);
                viewToRemove.remove();
            }

        },

        addPartTemplateView:function(model){
            var view = new PartTemplateListItemView({model:model}).render();
            this.listItemViews.push(view);
            this.$items.append(view.$el);
            view.on("selectionChanged",this.onSelectionChanged);
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
                this.onNoPartTemplateSelected();
            }else if(checkedViews.length == 1){
                this.onOnePartTemplateSelected();
            }else {
                this.onSeveralPartTemplatesSelected();
            }

        },

        onNoPartTemplateSelected:function(){
            this.trigger("delete-button:display",false);
        },

        onOnePartTemplateSelected:function(){
            this.trigger("delete-button:display",true);
        },

        onSeveralPartTemplatesSelected:function(){
            this.trigger("delete-button:display",true);
        },

        deleteSelectedPartTemplates:function(){
            var that = this ;
            if(confirm(i18n["DELETE_SELECTION_?"])){
                _(this.listItemViews).each(function(view){
                    if(view.isChecked()){
                        view.model.destroy({success:function(){
                            that.removePartTemplate(view.model);
                        },error:function(model,err){
                            alert(err.responseText)
                        }});
                    }
                });
                this.onNoPartTemplateSelected();
            }
        },

        getSelectedPartTemplate:function(){
            var checkedView = _(this.listItemViews).select(function(itemView){
                return itemView.isChecked();
            })[0];

            if(checkedView){
                return checkedView.model;
            }

        }

    });

    return PartTemplateListView;

});

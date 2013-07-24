define([
    "text!templates/part_template_list.html",
    "i18n!localization/nls/product-management-strings",
    "views/part_template_list_item",
    "i18n!localization/nls/datatable-strings"
], function (
    template,
    i18n,
    PartTemplateListItemView,
    i18nDt
    ) {
    var PartTemplateListView = Backbone.View.extend({

        template: Mustache.compile(template),

        events:{
            "click .toggle-checkboxes":"toggleSelection"
        },

        initialize: function () {
            _.bindAll(this);
            this.listenTo(this.collection, "reset", this.resetList);
            this.listenTo(this.collection, 'add', this.addNewPartTemplate);
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
                that.addPartTemplate(model);
            });
            this.dataTable();
        },

        pushPartTemplate:function(partTemplate){
            this.collection.push(partTemplate);
        },

        addNewPartTemplate:function(model){
            this.addPartTemplate(model,true);
            this.redraw();
        },

        addPartTemplate:function(model,effect){
            var view = this.addPartTemplateView(model);
            if(effect){
                view.$el.highlightEffect();
            }
        },

        removePartTemplate:function(model){
            this.removePartTemplateView(model);
            this.redraw();
        },

        removePartTemplateView:function(model){

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

        addPartTemplateView:function(model){
            var view = new PartTemplateListItemView({model:model}).render();
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
                            alert(err.responseText);
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
                    { "bSortable": false, "aTargets": [ 0 ] },
                    { "sType":i18nDt.DATE_SORT, "aTargets": [5] }
                ]
            });
            this.$el.parent().find(".dataTables_filter input").attr("placeholder",i18nDt.FILTER);
        }

    });

    return PartTemplateListView;

});

define([
    "text!templates/baseline/baselines_list.html",
    "i18n!localization/nls/baseline-strings",
    "views/baseline/baselines_list_item",
    "i18n!localization/nls/datatable-strings"
], function (
    template,
    i18n,
    BaselinesListItemView,
    i18nDt
    ) {
    var BaselinesListView = Backbone.View.extend({
        template: Mustache.compile(template),
        events:{
            "click .toggle-checkboxes":"toggleSelection"
        },

        initialize: function () {
            _.bindAll(this);
            this.listenTo(this.collection, "reset", this.resetList);
            this.listenTo(this.collection, 'add', this.addNewBaseline);
            this.listItemViews = [];
        },

        render:function(){
            this.collection.fetch({reset:true});
            return this;
        },

        bindDomElements:function(){
            this.$table = this.$("#baseline_table");
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
                that.addBaseline(model);
            });
            this.dataTable();
        },

        addNewBaseline:function(model){
            this.addBaseline(model,true);
            this.redraw();
        },

        addBaseline:function(model,effect){
            var view = this.addBaselineView(model);
            if(effect){
                view.$el.highlightEffect();
            }
        },

        removeBaseline:function(model){
            this.removeBaselineView(model);
            this.redraw();
        },

        removeBaselineView:function(model){

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

        addBaselineView:function(model){
            var view = new BaselinesListItemView({model:model}).render();
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
                this.onNoBaselineSelected();
            }else if(checkedViews.length == 1){
                this.onOneBaselineSelected();
            }else {
                this.onSeveralBaselinesSelected();
            }

        },

        onNoBaselineSelected:function(){
            this.trigger("delete-button:display",false);
            this.trigger("duplicate-button:display",false);
        },

        onOneBaselineSelected:function(){
            this.trigger("delete-button:display",true);
            this.trigger("duplicate-button:display",true);
        },

        onSeveralBaselinesSelected:function(){
            this.trigger("delete-button:display",true);
            this.trigger("duplicate-button:display",false);
        },

        getSelectedBaseline:function(){
            var model = null;
            _(this.listItemViews).each(function(view){
                if(view.isChecked()){
                    model = view.model;
                }
            });
            return model;
        },

        deleteSelectedBaselines:function(){
            var that = this;
            if(confirm(i18n["DELETE_SELECTION_?"])){
                _(this.listItemViews).each(function(view){
                    if(view.isChecked()){
                        view.model.destroy({success:function(){
                            that.removeBaseline(view.model);
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
            var oldSort = [[1,"asc"]];
            if(this.oTable){
                oldSort = this.oTable.fnSettings().aaSorting;
                this.oTable.fnDestroy();
            }
            this.oTable = this.$table.dataTable({
                aaSorting:oldSort,
                bDestroy:true,
                iDisplayLength:-1,
                oLanguage:{
                    sSearch: "<i class='fa fa-search'></i>",
                    sEmptyTable:i18nDt.NO_DATA,
                    sZeroRecords:i18nDt.NO_FILTERED_DATA
                },
                sDom : 'ft',
                aoColumnDefs: [
                    { "bSortable": false, "aTargets": [ 0 ] }
                ]
            });
            this.$el.find(".dataTables_filter input").attr("placeholder",i18nDt.FILTER);
        }

    });

    return BaselinesListView;
});

define([
    "text!templates/change_item_list.html",
    "i18n!localization/nls/change-management-strings",
    "views/change-issues/change_issue_list_item",
    "i18n!localization/nls/datatable-strings"
], function (
    template,
    i18n,
    ChangeIssueListItemView,
    i18nDt
    ) {
    var ChangeIssueListView = Backbone.View.extend({

        template: Mustache.compile(template),

        events:{
            "click .toggle-checkboxes":"toggleSelection"
        },

        removeSubviews: function () {
            _(this.listItemViews).invoke("remove");                                                                    // Invoke remove for each views in listItemViews
        },

        initialize: function () {
            _.bindAll(this);
            this.listenTo(this.collection, "reset", this.resetList);
            this.listenTo(this.collection, 'add', this.addNewIssue);
            this.listItemViews = [];
            this.$el.on("remove",this.removeSubviews);
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
            this.collection.each(function(model){that.addIssue(model);});
            this.dataTable();
        },

        addNewIssue:function(model){
            this.addIssue(model,true);
            this.redraw();
        },

        addIssue:function(model,effect){
            var view = new ChangeIssueListItemView({model:model}).render();
            this.listItemViews.push(view);
            this.$items.append(view.$el);
            view.on("selectionChanged",this.onSelectionChanged);
            view.on("rendered",this.redraw);
            if(effect){
                view.$el.highlightEffect();
            }
        },

        removeIssue:function(model){
            var viewToRemove = _(this.listItemViews).select(function(view){
                return view.model == model;
            })[0];

            if(viewToRemove){
                this.listItemViews = _(this.listItemViews).without(viewToRemove);
                var row = viewToRemove.$el.get(0);
                this.oTable.fnDeleteRow(this.oTable.fnGetPosition(row));
                viewToRemove.remove();
            }
            this.redraw();
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
            var checkedViewsLength = _(this.listItemViews).select(function(view){
                return view.isChecked();
            }).length;

            if (checkedViewsLength <= 0){                                                                               // No Issue Selected
                this.trigger("delete-button:display",false);
                this.trigger("acl-button:display",false);
            }else if(checkedViewsLength == 1){                                                                          // One Issue Selected
                this.trigger("delete-button:display",true);
                this.trigger("acl-button:display",true);
            }else {                                                                                                     // Several Issue Selected
                this.trigger("delete-button:display",true);
                this.trigger("acl-button:display",false);
            }
        },

        getChecked:function(){
            var model = null;
            _(this.listItemViews).each(function(view){
                if(view.isChecked()){
                    model = view.model;
                }
            });
            return model;
        },

        eachChecked: function (callback){
            _(this.listItemViews).each(function(view){
                if(view.isChecked()){
                    callback(view);
                }
            });
        },

        deleteSelectedIssues:function(){
            var that = this;
            if(confirm("Delete Issues")){
                _(this.listItemViews).each(function(view){
                    if(view.isChecked()){
                        view.model.destroy({success:function(){
                            that.removeIssue(view.model);
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
            this.eachChecked(function(view){
                view.unCheck();
            });

        },

        dataTable:function(){
            var oldSort = [[1,"asc"]];
            if(this.oTable){
                oldSort = this.oTable.fnSettings().aaSorting;
                this.oTable.fnDestroy();
            }
            this.oTable = this.$el.dataTable({
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
                    { "bSortable": false, "aTargets": [ 0,5 ] },
                    { "sType":i18nDt.DATE_SORT, "aTargets": [5] }
                ]
            });
            this.$el.parent().find(".dataTables_filter input").attr("placeholder",i18nDt.FILTER);
        }

    });

    return ChangeIssueListView;
});
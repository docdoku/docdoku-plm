define([
	"common-objects/collections/workflow_models",
	"views/checkbox_list",
	"views/workflow_list_item",
	"text!templates/workflow_list.html",
    "i18n!localization/nls/datatable-strings"
], function (
	WorkflowList,
	CheckboxListView,
	WorkflowListItemView,
	template,
    i18nDt
) {
	var WorkflowListView = CheckboxListView.extend({
		template: Mustache.compile(template),
        collection: function () {
			return new WorkflowList();
		},
		itemViewFactory: function (model) {
			return new WorkflowListItemView({
				model: model
			});
		},
        rendered:function(){
            var that = this;
            this.on("ready",function(){
                that.$el.dataTable({
                    bDestroy:true,
                    iDisplayLength:-1,
                    oLanguage:{
                        sSearch: "<i class='icon-search'></i>"
                    },
                    sDom : 'ft',
                    aoColumnDefs: [
                        { "bSortable": false, "aTargets": [ 0 ] }
                    ]
                });
                that.$el.parent().find(".dataTables_filter input").attr("placeholder", i18nDt.FILTER);
            });
        }
	});
	return WorkflowListView;
});

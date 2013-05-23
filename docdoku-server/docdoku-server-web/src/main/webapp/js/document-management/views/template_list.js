define([
	"views/checkbox_list",
	"views/template_list_item",
	"text!templates/template_list.html",
    "i18n!localization/nls/datatable-strings"
], function (
	CheckboxListView,
	TemplateListItemView,
	template,
    i18nDt
) {
	var TemplateListView = CheckboxListView.extend({
		template: Mustache.compile(template),
		itemViewFactory: function (model) {
			return new TemplateListItemView({
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
                        sSearch: "<i class='icon-search'></i>",
                        sEmptyTable:i18nDt.NO_DATA,
                        sZeroRecords:i18nDt.NO_FILTERED_DATA
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
	return TemplateListView;
});

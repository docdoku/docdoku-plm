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
            model.on("change",this.redraw);
			return new TemplateListItemView({
				model: model
			});
		},
        rendered:function(){
            this.once("_ready",this.dataTable);
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
                    { "sType":i18nDt.DATE_SORT, "aTargets": [4] }
                ]
            });
            this.$el.parent().find(".dataTables_filter input").attr("placeholder", i18nDt.FILTER);
        }
	});
	return TemplateListView;
});

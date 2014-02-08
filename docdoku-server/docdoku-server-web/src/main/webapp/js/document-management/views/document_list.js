define([
	"views/checkbox_list",
	"views/document_list_item",
	"text!templates/document_list.html",
    "i18n!localization/nls/datatable-strings"
], function (
	CheckboxListView,
	DocumentListItemView,
	template,
    i18nDt
) {
	var DocumentListView = CheckboxListView.extend({

		template: Mustache.compile(template),

		itemViewFactory: function (model) {
            model.on("change",this.redraw);
            return new DocumentListItemView({
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
            var oldSort = [];
            if(this.oTable){
                oldSort = this.oTable.fnSettings().aaSorting;
                try{
                    this.oTable.fnDestroy();
                }catch(e){
                    console.error(e)
                }

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
                    { "bSortable": false, "aTargets": [ 0,1,12,13,14,15 ] },
                    { "sType":i18nDt.DATE_SORT, "aTargets": [8,10] }
                ]
            });

            this.$el.parent().find(".dataTables_filter input").attr("placeholder", i18nDt.FILTER);
        }

	});
	return DocumentListView;
});

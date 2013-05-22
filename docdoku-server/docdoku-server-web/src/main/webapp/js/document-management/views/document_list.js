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
            var that = this ;
            this.on("ready",function(){
                that.dataTable();
            });

        },
        redraw:function(){
            this.dataTable();
        },
        dataTable:function(){
            if(this.oTable){
                this.oTable.fnDestroy();
            }
            this.oTable = this.$el.dataTable({
                bDestroy:true,
                iDisplayLength:-1,
                oLanguage:{
                    sSearch: "<i class='icon-search'></i>"
                },
                sDom : 'ft',
                aoColumnDefs: [
                    { "bSortable": false, "aTargets": [ 0,1,12,13,14,15 ] }
                ]
            });
            this.$el.parent().find(".dataTables_filter input").attr("placeholder", i18nDt.FILTER);
        }

	});
	return DocumentListView;
});

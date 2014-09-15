/*global define*/
define([
    "common-objects/views/documents/checkbox_list",
    "views/document_list_item",
    "text!templates/document_list.html"
], function (CheckboxListView, DocumentListItemView, template) {
    var DocumentListView = CheckboxListView.extend({

        template: template,

        itemViewFactory: function (model) {
            model.on("change", this.redraw);
            return new DocumentListItemView({
                model: model
            });
        },
        rendered: function () {
            this.once("_ready", this.dataTable);
        },
        redraw: function () {
            this.dataTable();
        },
        dataTable: function () {
            var oldSort = [];
            if (this.oTable) {
                oldSort = this.oTable.fnSettings().aaSorting;
                try {
                    this.oTable.fnDestroy();
                } catch (e) {
                    console.error(e);
                }

            }

            this.oTable = this.$el.dataTable({
                aaSorting: oldSort,
                bDestroy: true,
                iDisplayLength: -1,
                oLanguage: {
                    sSearch: "<i class='fa fa-search'></i>",
                    sEmptyTable: APP_CONFIG.i18n.NO_DATA,
                    sZeroRecords: APP_CONFIG.i18n.NO_FILTERED_DATA
                },
                sDom: 'ft',
                aoColumnDefs: [
                    { "bSortable": false, "aTargets": [ 0, 1, 12, 13, 14, 15 ] },
                    { "sType": APP_CONFIG.i18n.DATE_SORT, "aTargets": [8, 10] }
                ]
            });


            this.$el.parent().find(".dataTables_filter input").attr("placeholder", APP_CONFIG.i18n.FILTER);
        }

    });
    return DocumentListView;
});

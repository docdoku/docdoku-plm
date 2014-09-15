/*global define*/
define([
    "common-objects/views/documents/checkbox_list",
    "views/template_list_item",
    "text!templates/template_list.html"
], function (CheckboxListView, TemplateListItemView, template) {
    var TemplateListView = CheckboxListView.extend({

        template: template,

        itemViewFactory: function (model) {
            model.on("change", this.redraw);
            return new TemplateListItemView({
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
            var oldSort = [
                [0, "asc"]
            ];
            if (this.oTable) {
                oldSort = this.oTable.fnSettings().aaSorting;
                this.oTable.fnDestroy();
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
                    { "bSortable": false, "aTargets": [ 0 ] },
                    { "sType": APP_CONFIG.i18n.DATE_SORT, "aTargets": [4] }
                ]
            });
            this.$el.parent().find(".dataTables_filter input").attr("placeholder", APP_CONFIG.i18n.FILTER);
        }
    });
    return TemplateListView;
});

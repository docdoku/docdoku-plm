/*global define,App,_*/
define([
    'common-objects/views/documents/checkbox_list',
    'views/document_list_item',
    'text!templates/document_list.html'
], function (CheckboxListView, DocumentListItemView, template) {
    'use strict';
    var DocumentListView = CheckboxListView.extend({

        template: template,

        itemViewFactory: function (model) {
            model.on('change', this.redraw);
            return new DocumentListItemView({
                model: model
            });
        },
        rendered: function () {
            this.once('_ready', function() {
                this.dataTable();
                this.trigger('selectionChange'); // TODO rename event name accordingly to patterns ('entity:verb')
            });

        },
        redraw: function () {
            this.dataTable();
        },
        dataTable: function () {
            var oldSort = [];
            if (this.oTable) {
                oldSort = this.oTable.fnSettings().aaSorting;
                this.oTable.fnDestroy();
            }

            this.oTable = this.$el.dataTable({
                aaSorting: oldSort,
                bDestroy: true,
                iDisplayLength: -1,
                oLanguage: {
                    sSearch: '<i class="fa fa-search"></i>',
                    sEmptyTable: App.config.i18n.NO_DATA,
                    sZeroRecords: App.config.i18n.NO_FILTERED_DATA
                },
                sDom: 'ft',
                aoColumnDefs: [
                    { 'bSortable': false, 'aTargets': [ 0, 1, 11, 12, 13, 14, 15 ] },
                    { 'sType': App.config.i18n.DATE_SORT, 'aTargets': [8] },
                    { 'sType': 'strip_html', 'aTargets': [2] }
                ]
            });

            this.$el.parent().find('.dataTables_filter input').attr('placeholder', App.config.i18n.FILTER);
        },

        checkCheckboxes: function (selectedDocuments) {
            var that = this;
            _.each(selectedDocuments, function(selectedView) {
                _.each(that.subViews, function(view) {
                    if(selectedView.model.getId() === view.model.getId()) {
                        view.check();
                    }
                });
            });
        }

    });
    return DocumentListView;
});

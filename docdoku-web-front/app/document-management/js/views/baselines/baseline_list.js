/*global _,define,bootbox,App*/
define([
    'backbone',
    'mustache',
    'text!templates/baselines/baseline_list.html',
    'views/baselines/baseline_list_item'
], function (Backbone, Mustache, template, BaselineListItemView) {
    'use strict';

    var BaselineListView = Backbone.View.extend({

        events: {
            'click .toggle-checkboxes': 'toggleSelection'
        },

        removeSubviews: function () {
            _(this.listItemViews).invoke('remove');                                                                     // Invoke remove for each views in listItemViews
            this.listItemViews = [];
        },

        initialize: function () {
            _.bindAll(this);
            this.listenTo(this.collection, 'reset', this.resetList);
            this.listenTo(this.collection, 'add', this.addNewDocumentBaseline);
            this.listItemViews = [];
            this.$el.on('remove', this.removeSubviews);
        },

        render: function () {
            this.collection.fetch({reset: true});
            return this;
        },

        bindDomElements: function () {
            this.$table = this.$('#document_baseline_table');
            this.$items = this.$('.items');
            this.$checkbox = this.$('.toggle-checkboxes');
        },

        resetList: function () {
            if (this.oTable) {
                this.oTable.fnDestroy();
            }
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n}));
            this.bindDomElements();
            this.listItemViews = [];
            var that = this;
            this.collection.each(function (model) {
                that.addDocumentBaseline(model);
            });
            this.dataTable();
        },

        addNewDocumentBaseline: function (model) {
            this.addDocumentBaseline(model, true);
            this.redraw();
        },

        addDocumentBaseline: function (model, effect) {
            var view = this.addDocumentBaselineView(model);
            if (effect) {
                view.$el.highlightEffect();
            }
        },

        removeDocumentBaseline: function (model) {
            this.removeDocumentBaselineView(model);
            this.redraw();
        },

        removeDocumentBaselineView: function (model) {
            var viewToRemove = _(this.listItemViews).select(function (view) {
                return view.model === model;
            })[0];

            if (viewToRemove) {
                this.listItemViews = _(this.listItemViews).without(viewToRemove);
                var row = viewToRemove.$el.get(0);
                this.oTable.fnDeleteRow(this.oTable.fnGetPosition(row));
                viewToRemove.remove();
            }
        },

        addDocumentBaselineView: function (model) {
            var view = new BaselineListItemView({model: model}).render();
            this.listItemViews.push(view);
            this.$items.append(view.$el);
            view.on('selectionChanged', this.onSelectionChanged);
            view.on('rendered', this.redraw);
            return view;
        },

        toggleSelection: function () {
            if (this.$checkbox.is(':checked')) {
                _(this.listItemViews).each(function (view) {
                    view.check();
                });
            } else {
                _(this.listItemViews).each(function (view) {
                    view.unCheck();
                });
            }
            this.onSelectionChanged();
        },

        onSelectionChanged: function () {
            var checkedViews = _(this.listItemViews).select(function (view) {
                return view.isChecked();
            });

            if (checkedViews.length <= 0) {
                this.onNoDocumentBaselineSelected();
            } else if (checkedViews.length === 1) {
                this.onOneDocumentBaselineSelected();
            } else {
                this.onSeveralDocumentBaselinesSelected();
            }

        },

        onNoDocumentBaselineSelected: function () {
            this.trigger('delete-button:display', false);
        },

        onOneDocumentBaselineSelected: function () {
            this.trigger('delete-button:display', true);
        },

        onSeveralDocumentBaselinesSelected: function () {
            this.trigger('delete-button:display', true);
        },

        getSelectedBaseline:function(){
            var selectedView =  _.select(this.listItemViews,function(view){
                return view.isChecked();
            })[0];
            return selectedView ? selectedView.model : null;
        },

        deleteSelectedBaselines: function () {
            var _this = this;
            bootbox.confirm(App.config.i18n.CONFIRM_DELETE_DOCUMENTS_COLLECTIONS, function(result){
                if(result){
                    _(_this.listItemViews).each(function (view) {
                        if (view.isChecked()) {
                            view.model.destroy({
                                dataType: 'text', // server doesn't send a json hash in the response body
                                success: function () {
                                    _this.removeDocumentBaseline(view.model);
                                    _this.onSelectionChanged();
                                },
                                error: function (model, err) {
                                    _this.trigger('error',model,err);
                                    _this.onSelectionChanged();
                                }
                            });
                        }
                    });
                }
            });
        },
        redraw: function () {
            this.dataTable();
        },
        dataTable: function () {
            var oldSort = [
                [1, 'asc']
            ];
            if (this.oTable) {
                oldSort = this.oTable.fnSettings().aaSorting;
                this.oTable.fnDestroy();
            }
            this.oTable = this.$table.dataTable({
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
                    { 'bSortable': false, 'aTargets': [ 0,6 ] },
                    { 'sType': App.config.i18n.DATE_SORT, 'aTargets': [4] }
                ]
            });
            this.$el.find('.dataTables_filter input').attr('placeholder', App.config.i18n.FILTER);
        }

    });

    return BaselineListView;
});

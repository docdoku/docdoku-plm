/*global _,define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/product-instances/product_instances_list.html',
    'views/product-instances/product_instances_list_item'
], function (Backbone, Mustache, template, ProductInstancesListItemView) {
	'use strict';
    var ProductInstancesListView = Backbone.View.extend({

        events: {
            'click .toggle-checkboxes': 'toggleSelection'
        },

        initialize: function () {
            _.bindAll(this);
            this.listenTo(this.collection, 'reset', this.resetList);
            this.listenTo(this.collection, 'add', this.addNewProductInstances);
            this.listItemViews = [];
        },

        render: function () {
            this.collection.fetch({reset: true});
            return this;
        },

        bindDomElements: function () {
            this.$table = this.$('#product_instances_table');
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
                that.addProductInstances(model);
            });
            this.dataTable();
        },

        addNewProductInstances: function (model) {
            this.addProductInstances(model, true);
            this.redraw();
        },

        addProductInstances: function (model, effect) {
            var view = this.addProductInstancesView(model);
            if (effect) {
                view.$el.highlightEffect();
            }
        },

        removeProductInstance: function (model) {
            this.removeProductInstancesView(model);
            this.redraw();
        },

        removeProductInstancesView: function (model) {

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

        addProductInstancesView: function (model) {
            var view = new ProductInstancesListItemView({model: model}).render();
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
                this.onNoProductInstanceSelected();
            } else if (checkedViews.length === 1) {
                this.onOneProductInstanceSelected();
            } else {
                this.onSeveralProductInstancesSelected();
            }

        },

        onNoProductInstanceSelected: function () {
            this.trigger('delete-button:display', false);
            this.trigger('duplicate-button:display', false);
        },

        onOneProductInstanceSelected: function () {
            this.trigger('delete-button:display', true);
            this.trigger('duplicate-button:display', true);
        },

        onSeveralProductInstancesSelected: function () {
            this.trigger('delete-button:display', true);
            this.trigger('duplicate-button:display', false);
        },

        getSelectedProductInstance: function () {
            var model = null;
            _(this.listItemViews).each(function (view) {
                if (view.isChecked()) {
                    model = view.model;
                }
            });
            return model;
        },

        deleteSelectedProductInstances: function () {
            var that = this;
            if (confirm(App.config.i18n.DELETE_SELECTION_QUESTION)) {
                _(this.listItemViews).each(function (view) {
                    if (view.isChecked()) {
                        view.model.id = view.model.getSerialNumber();
                        view.model.destroy({
                            dataType: 'text', // server doesn't send a json hash in the response body
                            success: function () {
                                that.removeProductInstance(view.model);
                                that.onSelectionChanged();
                            },
                            error: function (model, err) {
                                alert(err.responseText);
                                that.onSelectionChanged();
                            },
                            wait: true});
                    }
                });
            }
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
                    { 'bSortable': false, 'aTargets': [ 0 ] }
                ]
            });
            this.$el.find('.dataTables_filter input').attr('placeholder', App.config.i18n.FILTER);
        }

    });

    return ProductInstancesListView;
});

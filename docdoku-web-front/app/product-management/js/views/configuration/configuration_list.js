/*global _,define,bootbox,App*/
define([
    'backbone',
    'mustache',
    'text!templates/configuration/configuration_list.html',
    'views/configuration/configuration_list_item'
], function (Backbone, Mustache, template, ConfigurationListItemView) {
    'use strict';
    var ConfigurationListView = Backbone.View.extend({

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
            this.listenTo(this.collection, 'add', this.addNewConfiguration);
            this.listItemViews = [];
            this.$el.on('remove', this.removeSubviews);
        },

        render: function () {
            var _this = this;
            this.oTable = null;
            this.collection.fetch({
                reset: true,
                error: function (err) {
                    _this.trigger('error', err);
                }
            });
            return this;
        },

        bindDomElements: function () {
            this.$items = this.$('.items');
            this.$checkbox = this.$('.toggle-checkboxes');
        },

        resetList: function () {
            var _this = this;
            this.removeSubviews();

            this.$el.html(Mustache.render(template, {i18n: App.config.i18n}));
            this.bindDomElements();

            this.collection.each(function (model) {
                _this.addConfiguration(model);
            });
            this.dataTable();
        },

        addNewConfiguration: function (model) {
            this.addConfiguration(model, true);
            this.redraw();
        },

        addConfiguration: function (model, effect) {
            var view = this.addConfigurationView(model);
            if (effect) {
                view.$el.highlightEffect();
            }
        },

        removeConfiguration: function (model) {
            this.removeConfigurationView(model);
            this.redraw();
        },

        removeConfigurationView: function (model) {
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

        addConfigurationView: function (model) {
            var view = new ConfigurationListItemView({model: model}).render();
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
                this.onNoConfigurationSelected();
            } else if (checkedViews.length === 1) {
                this.onOneConfigurationSelected();
            } else {
                this.onSeveralConfigurationsSelected();
            }

        },

        onNoConfigurationSelected: function () {
            this.trigger('delete-button:display', false);
            this.trigger('acl-button:display', false);
        },

        onOneConfigurationSelected: function () {
            this.trigger('delete-button:display', true);
            this.trigger('acl-button:display', true);
        },

        onSeveralConfigurationsSelected: function () {
            this.trigger('delete-button:display', true);
            this.trigger('acl-button:display', false);
        },

        getSelectedConfiguration: function () {
            var model = null;
            _(this.listItemViews).each(function (view) {
                if (view.isChecked()) {
                    model = view.model;
                }
            });
            return model;
        },

        deleteSelectedConfigurations: function () {
            var _this = this;
            bootbox.confirm(App.config.i18n.CONFIRM_DELETE_CONFIGURATION,
                App.config.i18n.CANCEL,
                App.config.i18n.DELETE,
                function (result) {
                    if (result) {
                        _(_this.listItemViews).each(function (view) {
                            if (view.isChecked()) {
                                view.model.destroy({
                                    dataType: 'text', // server doesn't send a json hash in the response body
                                    success: function () {
                                        _this.removeConfiguration(view.model);
                                        _this.onSelectionChanged();
                                    },
                                    error: function (model, err) {
                                        _this.trigger('error', model, err);
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
                [0, 'asc']
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
                    sSearch: '<i class="fa fa-search"></i>',
                    sEmptyTable: App.config.i18n.NO_DATA,
                    sZeroRecords: App.config.i18n.NO_FILTERED_DATA
                },
                sDom: 'ft',
                aoColumnDefs: [
                    {'bSortable': false, 'aTargets': [0, 5]},
                    {'sType': App.config.i18n.DATE_SORT, 'aTargets': [3]}
                ]
            });
            this.$el.parent().find('.dataTables_filter input').attr('placeholder', App.config.i18n.FILTER);
        }

    });

    return ConfigurationListView;
});

/*global _,define,bootbox,App*/
define([
    'backbone',
    'mustache',
    'text!templates/change_item_list.html',
    'views/change-issues/change_issue_list_item'
], function (Backbone, Mustache, template, ChangeIssueListItemView) {
	'use strict';
    var ChangeIssueListView = Backbone.View.extend({
        events: {
            'click .toggle-checkboxes': 'toggleSelection'
        },

        removeSubviews: function () {
            _(this.listItemViews).invoke('remove');                                                                    // Invoke remove for each views in listItemViews
        },

        initialize: function () {
            _.bindAll(this);
            this.listenTo(this.collection, 'reset', this.resetList);
            this.listenTo(this.collection, 'add', this.addNewIssue);
            this.listItemViews = [];
            this.$el.on('remove', this.removeSubviews);
        },

        render: function () {
            this.collection.fetch({reset: true});
            return this;
        },

        bindDomElements: function () {
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
                that.addIssue(model);
            });
            this.dataTable();

        },

        addNewIssue: function (model) {
            this.addIssue(model, true);
            this.redraw();
        },

        addIssue: function (model, effect) {
            var view = new ChangeIssueListItemView({model: model}).render();
            this.listItemViews.push(view);
            this.$items.append(view.$el);
            view.on('selectionChanged', this.onSelectionChanged);
            view.on('rendered', this.redraw);
            if (effect) {
                view.$el.highlightEffect();
            }
        },

        removeIssue: function (model) {
            var viewToRemove = _(this.listItemViews).select(function (view) {
                return view.model === model;
            })[0];

            if (viewToRemove) {
                this.listItemViews = _(this.listItemViews).without(viewToRemove);
                var row = viewToRemove.$el.get(0);
                this.oTable.fnDeleteRow(this.oTable.fnGetPosition(row));
                viewToRemove.remove();
            }
            this.redraw();
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
            var checkedViewsLength = _(this.listItemViews).select(function (view) {
                return view.isChecked();
            }).length;

            if (checkedViewsLength <= 0) {                                                                               // No Issue Selected
                this.trigger('delete-button:display', false);
                this.trigger('acl-button:display', false);
            } else if (checkedViewsLength === 1) {                                                                          // One Issue Selected
                this.trigger('delete-button:display', true);
                this.trigger('acl-button:display', true);
            } else {                                                                                                     // Several Issue Selected
                this.trigger('delete-button:display', true);
                this.trigger('acl-button:display', false);
            }
        },

        getChecked: function () {
            var model = null;
            _(this.listItemViews).each(function (view) {
                if (view.isChecked()) {
                    model = view.model;
                }
            });
            return model;
        },

        eachChecked: function (callback) {
            _(this.listItemViews).each(function (view) {
                if (view.isChecked()) {
                    callback(view);
                }
            });
        },

        deleteSelectedIssues: function () {
            var _this = this;
            bootbox.confirm(App.config.i18n.CONFIRM_DELETE_ISSUE, function(result){
                if(result){
                    _(_this.listItemViews).each(function (view) {
                        if (view.isChecked()) {
                            view.model.destroy({
                                wait:true,
                                dataType: 'text', // server doesn't send a json hash in the response body
                                success: function () {
                                    _this.removeIssue(view.model);
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
            this.eachChecked(function (view) {
                view.unCheck();
            });

        },

        dataTable: function () {
            var oldSort = [
                [1, 'asc']
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
                    { 'bSortable': false, 'aTargets': [ 0, 5 ] },
                    { 'sType': App.config.i18n.DATE_SORT, 'aTargets': [6] },
                    { 'sType': 'strip_html', 'aTargets': [1] }
                ]
            });
            this.$el.parent().find('.dataTables_filter input').attr('placeholder', App.config.i18n.FILTER);
        }

    });

    return ChangeIssueListView;
});

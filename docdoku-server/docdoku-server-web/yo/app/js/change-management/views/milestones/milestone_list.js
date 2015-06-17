/*global _,define,App,bootbox*/
define([
    'backbone',
    'mustache',
    'text!templates/milestones/milestone_list.html',
    'views/milestones/milestone_list_item'
], function (Backbone, Mustache, template, MilestoneListItemView) {
	'use strict';
	var MilestoneListView = Backbone.View.extend({

        events: {
            'click .toggle-checkboxes': 'toggleSelection'
        },

        initialize: function () {
            _.bindAll(this);
            this.listenTo(this.collection, 'reset', this.resetList);
            this.listenTo(this.collection, 'add', this.addNewMilestone);
            this.listItemViews = [];
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
                that.addMilestone(model);
            });
            this.dataTable();
        },

        addNewMilestone: function (model) {
            this.addMilestone(model, true);
            this.redraw();
        },

        addMilestone: function (model, effect) {
            var view = this.addMilestoneView(model);
            if (effect) {
                view.$el.highlightEffect();
            }
        },

        removeMilestone: function (model) {
            this.removeMilestoneView(model);
            this.redraw();
        },

        removeMilestoneView: function (model) {
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

        addMilestoneView: function (model) {
            var view = new MilestoneListItemView({model: model}).render();
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
            var checkedViewsLength = _(this.listItemViews).select(function (view) {
                return view.isChecked();
            }).length;

            if (checkedViewsLength <= 0) {                                                                               // No Milestone Selected
                this.trigger('delete-button:display', false);
                this.trigger('acl-button:display', false);
            } else if (checkedViewsLength === 1) {                                                                          // One Milestone Selected
                this.trigger('delete-button:display', true);
                this.trigger('acl-button:display', true);
            } else {                                                                                                     // Several Milestone Selectes
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

        deleteSelectedMilestones: function () {
            var that = this;
            bootbox.confirm(App.config.i18n.CONFIRM_DELETE_ISSUE, function(result){
                if(result){
                    _(that.listItemViews).each(function (view) {
                        if (view.isChecked()) {
                            view.model.destroy({
                                wait:true,
                                dataType: 'text', // server doesn't send a json hash in the response body
                                success: function () {
                                    that.removeMilestone(view.model);
                                    that.onSelectionChanged();
                                },
                                error: function (model, err) {
                                    that.trigger('error', model, err);
                                    that.onSelectionChanged();
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
                    { 'sType': 'strip_html', 'aTargets': [1] }
                ]
            });
            this.$el.parent().find('.dataTables_filter input').attr('placeholder', App.config.i18n.FILTER);
        }

    });

    return MilestoneListView;
});

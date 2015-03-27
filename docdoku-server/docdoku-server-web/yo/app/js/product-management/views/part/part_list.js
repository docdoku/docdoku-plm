/*global _,define,App,bootbox*/
define([
    'backbone',
    'mustache',
    'text!templates/part/part_list.html',
    'views/part/part_list_item'
], function (Backbone, Mustache, template, PartListItemView) {
    'use strict';
    var PartListView = Backbone.View.extend({

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
            this.listenTo(this.collection, 'add', this.addNewPart);
            this.listItemViews = [];
            this.$el.on('remove', this.removeSubviews);
        },

        render: function () {
            var that = this;
            this.collection.fetch({reset: true}).error(function (err) {
                that.trigger('error', null, err);
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
                _this.addPart(model);
            });
            this.dataTable();
        },

        addNewPart: function (model) {
            this.addPart(model, true);
            this.redraw();
        },

        addPart: function (model, effect) {
            var view = this.addPartView(model);
            if (effect) {
                view.$el.highlightEffect();
            }
        },

        removePart: function (model) {
            this.removePartView(model);
            this.redraw();
        },

        removePartView: function (model) {

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

        addPartView: function (model) {
            var view = new PartListItemView({model: model}).render();
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

            var checkedViews = _(this.listItemViews).select(function (itemView) {
                return itemView.isChecked();
            });

            if (checkedViews.length <= 0) {
                this.onNoPartSelected();
            } else if (checkedViews.length === 1) {
                this.onOnePartSelected();

                if (checkedViews[0].model.isCheckout()) {
                    if (checkedViews[0].model.isCheckoutByConnectedUser()) {
                        var canUndo = checkedViews[0].model.getLastIteration().get('iteration') > 1;
                        this.trigger('checkout-group:update', {canCheckout: false, canUndo: canUndo, canCheckin: true});
                    } else {
                        this.trigger('checkout-group:update', {canCheckout: false, canUndo: false, canCheckin: false});
                    }
                } else {
                    this.trigger('checkout-group:update', {canCheckout: true, canUndo: false, canCheckin: false});
                }

            } else {
                this.onSeveralPartsSelected();
                this.canCheckinCheckoutOrUndoCheckout();


            }

        },

        onNoPartSelected: function () {
            this.trigger('delete-button:display', false);
            this.trigger('checkout-group:display', false);
            this.trigger('acl-edit-button:display', false);
            this.trigger('new-version-button:display', false);
            this.trigger('release-button:display', false);
            this.trigger('new-product-button:display', false);
            this.trigger('obsolete-button:display', false);
        },

        onOnePartSelected: function () {
            var partSelected = this.getSelectedPart();
            this.trigger('delete-button:display', true);
            this.trigger('checkout-group:display', !partSelected.isReleased() && !partSelected.isObsolete());
            this.trigger('acl-edit-button:display', partSelected ? (App.config.workspaceAdmin || partSelected.getAuthorLogin() === App.config.login) : false);
            this.trigger('new-version-button:display', !partSelected.isCheckout());
            this.trigger('release-button:display', (!partSelected.isCheckout() && !partSelected.isReleased() && !partSelected.isObsolete()));
            this.trigger('new-product-button:display', true);
            this.trigger('obsolete-button:display', partSelected.isReleased());
        },

        onSeveralPartsSelected: function () {
            this.trigger('delete-button:display', true);
            this.trigger('acl-edit-button:display', false);
            this.trigger('new-version-button:display', false);
            this.trigger('release-button:display', this.isSelectedPartsReleasable());
            this.trigger('new-product-button:display', false);
            this.trigger('obsolete-button:display', false);
        },

        deleteSelectedParts: function () {
            var _this = this;
            bootbox.confirm(App.config.i18n.CONFIRM_DELETE_PART, function (result) {
                if (result) {
                    _(_this.listItemViews).each(function (view) {
                        if (view.isChecked()) {
                            view.model.destroy({
                                wait: true,
                                dataType: 'text', // server doesn't send a json hash in the response body
                                success: function () {
                                    _this.removePart(view.model);
                                    _this.onSelectionChanged();
                                }, error: function (model, err) {
                                    _this.trigger('error', model, err);
                                    _this.onSelectionChanged();
                                }});
                        }
                    });
                }
            });
        },

        releaseSelectedParts: function () {
            var that = this;
            bootbox.confirm(App.config.i18n.RELEASE_SELECTION_QUESTION, function (result) {
                if (result) {
                    _(that.listItemViews).each(function (view) {
                        if (view.isChecked()) {
                            view.model.release();
                        }
                    });
                }
            });
        },

        getSelectedPart: function () {
            var checkedView = _(this.listItemViews).select(function (itemView) {
                return itemView.isChecked();
            })[0];

            if (checkedView) {
                return checkedView.model;
            }
            return null;
        },

        getSelectedParts: function () {
            var checkedViews = [];
            _(this.listItemViews).select(function (itemView) {
                if (itemView.isChecked()) {
                    checkedViews.push(itemView.model);
                }

            });
            return checkedViews;
        },
        eachChecked: function (callback) {
            _(this.listItemViews).each(function (view) {
                if (view.isChecked()) {
                    callback(view);
                }
            });
        },
        isSelectedPartsReleasable: function () {
            var isPartReleasable = true;
            _(this.listItemViews).each(function (view) {
                if (view.isChecked() && (view.model.isCheckout() || view.model.isReleased())) {
                    isPartReleasable = false;
                }
            });
            return isPartReleasable;
        },
        areSelectedPartsCheckoutable: function () {
            var isPartCheckout = true;
            _(this.getSelectedParts()).each(function (view) {
                if (view.isReleased() || view.isCheckout()) {
                    isPartCheckout = false;
                }
            });
            return isPartCheckout;
        },
        areSelectedPartsCheckouted: function () {
            var isPartCheckouted = true;
            _(this.getSelectedParts()).each(function (view) {
                if (!view.isCheckout()) {
                    isPartCheckouted = false;
                }
            });
            return isPartCheckouted;
        },
        areSelectedPartsAllNotCheckouted: function () {
            var isPartNotCheckouted = true;
            _(this.getSelectedParts()).each(function (view) {
                if (view.isCheckout() || view.isReleased()) {
                    isPartNotCheckouted = false;
                }
            });
            return isPartNotCheckouted;
        },
        areSelectedPartsCheckoutedByConnectedUser: function () {
            var isPartCheckoutedByConnectedUser = true;
            _(this.getSelectedParts()).each(function (view) {
                if (!view.isCheckoutByConnectedUser()) {
                    isPartCheckoutedByConnectedUser = false;
                }
            });
            return isPartCheckoutedByConnectedUser;
        },
        haveMoreThanOneIteration: function () {
            var hasMoreThanOneIteration = true;
            _(this.getSelectedParts()).each(function (view) {
                if (view.getLastIteration().get('iteration') <= 1) {
                    hasMoreThanOneIteration = false;
                }
            });
            return hasMoreThanOneIteration;
        },

        canCheckinCheckoutOrUndoCheckout: function () {

            if (this.areSelectedPartsCheckouted()) {
                if (this.areSelectedPartsCheckoutedByConnectedUser()) {
                    this.trigger('checkout-group:display', true);
                    this.trigger('checkout-group:update', {canCheckout: false, canUndo: this.haveMoreThanOneIteration(), canCheckin: true});
                } else {
                    this.trigger('checkout-group:display', true);
                    this.trigger('checkout-group:update', {canCheckout: false, canUndo: false, canCheckin: false});
                }
            } else if(this.areSelectedPartsAllNotCheckouted()) {
                this.trigger('checkout-group:display', true);
                this.trigger('checkout-group:update', {canCheckout: true, canUndo: false, canCheckin: false});
            }

            else if ( this.areSelectedPartsCheckoutable()){
                this.trigger('checkout-group:display', true);
                this.trigger('checkout-group:update', {canCheckout: true, canUndo: false, canCheckin: false});
            }
            else{
                this.trigger('checkout-group:display', false);
            }


        },

        redraw: function () {
            this.dataTable();
        },

        dataTable: function () {
            var oldSort = [
                [0, 'asc']
            ];
            if (this.oTable) {
                if (this.oTable.fnSettings()) {
                    oldSort = this.oTable.fnSettings().aaSorting;
                    this.oTable.fnDestroy();
                }
                this.bindDomElements();
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
                    { 'bSortable': false, 'aTargets': [ 0, 1, 11, 12, 13 ] },
                    { 'sType': App.config.i18n.DATE_SORT, 'aTargets': [8] },
                    { 'sType': 'strip_html', 'aTargets': [2] }
                ]
            });
            this.$el.parent().find('.dataTables_filter input').attr('placeholder', App.config.i18n.FILTER);
        }

    });
    return PartListView;
});

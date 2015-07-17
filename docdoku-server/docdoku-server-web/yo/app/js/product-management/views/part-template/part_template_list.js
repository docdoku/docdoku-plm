/*global _,define,App,bootbox*/
define([
    'backbone',
    'mustache',
    'text!templates/part-template/part_template_list.html',
    'views/part-template/part_template_list_item',
    'common-objects/views/security/acl_edit'
], function (Backbone, Mustache, template, PartTemplateListItemView, ACLEditView) {
    'use strict';
    var PartTemplateListView = Backbone.View.extend({

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
            this.listenTo(this.collection, 'add', this.addNewPartTemplate);
            this.listItemViews = [];
            this.$el.on('remove', this.removeSubviews);
        },

        render: function () {
            var _this = this;
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
                _this.addPartTemplate(model);
            });
            this.dataTable();
        },

        addNewPartTemplate: function (model) {
            this.addPartTemplate(model, true);
            this.redraw();
        },

        addPartTemplate: function (model, effect) {
            var view = this.addPartTemplateView(model);
            if (effect) {
                view.$el.highlightEffect();
            }
        },

        removePartTemplate: function (model) {
            this.removePartTemplateView(model);
            this.redraw();
        },

        removePartTemplateView: function (model) {

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

        addPartTemplateView: function (model) {
            var view = new PartTemplateListItemView({model: model}).render();
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
                this.onNoPartTemplateSelected();
            } else if (checkedViews.length === 1) {
                this.onOnePartTemplateSelected();
            } else {
                this.onSeveralPartTemplatesSelected();
            }

        },

        onNoPartTemplateSelected: function () {
            this.trigger('delete-button:display', false);
            this.trigger('acl-button:display', false);
        },
        onOnePartTemplateSelected: function () {
            this.trigger('delete-button:display', true);
            this.trigger('acl-button:display', true);
        },
        onSeveralPartTemplatesSelected: function () {
            this.trigger('delete-button:display', true);
            this.trigger('acl-button:display', false);
        },

        deleteSelectedPartTemplates: function () {
            var _this = this;
            bootbox.confirm(App.config.i18n.CONFIRM_DELETE_PART_TEMPLATE, function (result) {
                if (result) {
                    _(_this.listItemViews).each(function (view) {
                        if (view.isChecked()) {
                            view.model.destroy({
                                dataType: 'text', // server doesn't send a json hash in the response body
                                success: function () {
                                    _this.removePartTemplate(view.model);
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

        editSelectedPartTemplateACL: function () {
            var templateSelected;
            var _this = this;
            _(_this.listItemViews).each(function (view) {
                if (view.isChecked()) {
                    templateSelected = view.model;
                }
            });

            var aclEditView = new ACLEditView({
                editMode: true,
                acl: templateSelected.get('acl')
            });

            aclEditView.setTitle(templateSelected.getId());
            window.document.body.appendChild(aclEditView.render().el);

            aclEditView.openModal();
            aclEditView.on('acl:update', function () {

                var acl = aclEditView.toList();

                templateSelected.updateACL({
                    acl: acl || {userEntries: {}, groupEntries: {}},
                    success: function () {
                        templateSelected.set('acl', acl);
                        aclEditView.closeModal();
                    },
                    error: function(error){
                        aclEditView.onError(error);
                    }
                });
            });

            return false;
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
                    {'bSortable': false, 'aTargets': [0, 6, 7]},
                    {'sType': App.config.i18n.DATE_SORT, 'aTargets': [5]}
                ]
            });
            this.$el.parent().find('.dataTables_filter input').attr('placeholder', App.config.i18n.FILTER);
        }

    });

    return PartTemplateListView;

});

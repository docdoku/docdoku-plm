/*global _,define,App,window,$*/
define([
    'backbone',
    'mustache',
    'async',
    'views/bom_item_view',
    'text!templates/bom_content.html',
    'collections/part_collection',
    'common-objects/views/prompt',
    'common-objects/views/security/acl_edit'
], function (Backbone, Mustache, Async, BomItemView, template, PartList, PromptView, ACLEditView) {
    'use strict';
    var BomContentView = Backbone.View.extend({

        el: '#bom_table_container',

        events: {
            'change th > input': 'onHeaderSelectionChanged',
            'change td > input': 'onItemSelectionChanged'
        },

        initialize: function () {
            _.bindAll(this);
            this.itemViews = [];
        },

        render: function () {
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n}));
            this.table = this.$('table');
            this.tbody = this.$('tbody');
            return this;
        },

        onHeaderSelectionChanged: function (e) {
            this.setCheckAll(e.target.checked);
            this.notifySelectionChanged();
        },

        onItemSelectionChanged: function () {
            this.notifySelectionChanged();
        },

        notifySelectionChanged: function () {
            this.trigger('itemSelectionChanged', this.checkedViews());
        },

        update: function (component) {
            this.itemViews = [];
            if (component && !component.isVirtual()) {
                this.partsCollection = new PartList();
                this.partsCollection.setFilterUrl(component.getUrlForBom());
                this.listenTo(this.partsCollection, 'reset', this.addAllBomItem);
                this.partsCollection.fetch({reset: true});
            }
        },

        showRoot: function (rootComponent) {
            this.itemViews = [];
            if (rootComponent && !rootComponent.isVirtual()) {
                this.partsCollection = new PartList();
                this.partsCollection.setFilterUrl(rootComponent.getRootUrlForBom());
                this.listenTo(this.partsCollection, 'reset', this.addAllBomItem);
                this.partsCollection.fetch({reset: true});
            }
        },

        addAllBomItem: function (parts) {
            this.render();
            parts.each(this.addBomItem, this);
            this.notifySelectionChanged();
            this.dataTable();
        },

        setCheckAll: function(status) {
            _.invoke(this.itemViews, 'setSelectionState', status);
        },

        checkedViews: function () {
            return _.filter(this.itemViews, function (itemView) {
                return itemView.isChecked();
            });
        },

        addBomItem: function (part) {
            var bomItemView = new BomItemView({model: part}).render();
            this.listenTo(bomItemView.model, 'change', this.notifySelectionChanged);
            this.itemViews.push(bomItemView);
            this.tbody.append(bomItemView.el);
        },

        actionCheckout: function () {
            var listViews = this.checkedViews();

            var ajaxes = [];
            _(listViews).each(function (view) {
                ajaxes.push(view.model.checkout());
            });
            $.when.apply($, ajaxes).then(this.onSuccess);


            return false;
        },

        actionUndocheckout: function () {
            var listViews = this.checkedViews();

            var ajaxes = [];
            _(listViews).each(function (view) {
                ajaxes.push(view.model.undocheckout());
            });
            $.when.apply($, ajaxes).then(this.onSuccess);


            return false;
        },

        actionCheckin: function () {
            var self = this;
            var listViews = this.checkedViews();
            var promptView = new PromptView();
            promptView.setPromptOptions(App.config.i18n.REVISION_NOTE, App.config.i18n.REVISION_NOTE_PROMPT_LABEL, App.config.i18n.REVISION_NOTE_PROMPT_OK, App.config.i18n.REVISION_NOTE_PROMPT_CANCEL);
            window.document.body.appendChild(promptView.render().el);
            promptView.openModal();

            self.listenTo(promptView, 'prompt-ok', function (args) {
                var iterationNote = args[0];
                if (_.isEqual(iterationNote, '')) {
                    iterationNote = null;
                }

                Async.each(listViews, function (view, callback) {
                        view.model.getLastIteration().save({
                            iterationNote: iterationNote
                        }).success(function () {
                            view.model.checkin().success(callback);
                        });
                    },
                    function (err) {
                        if (!err) {
                            self.onSuccess();
                        }
                    });

            });

            this.listenTo(promptView, 'prompt-cancel', function () {
                var ajaxes = [];
                _(listViews).each(function (view) {
                    ajaxes.push(view.model.checkin());
                });
                $.when.apply($, ajaxes).then(this.onSuccess);
            });
            return false;
        },

        onSuccess: function () {
            Backbone.Events.trigger('part:saved');
        },

        actionUpdateACL: function () {
            var _this = this;

            var selectedPart = this.checkedViews()[0].model;

            var aclEditView = new ACLEditView({
                editMode: true,
                acl: selectedPart.get('acl')
            });

            aclEditView.setTitle(selectedPart.getPartKey());

            window.document.body.appendChild(aclEditView.render().el);

            aclEditView.openModal();

            aclEditView.on('acl:update', function () {

                var acl = aclEditView.toList();

                selectedPart.updateACL({
                    acl: acl || {userEntries: {}, groupEntries: {}},
                    success: function () {
                        selectedPart.set('acl', acl);
                        aclEditView.closeModal();
                    },
                    error: _this.onError
                });

            });
        },

        dataTable: function () {
            var oldSort = [
                [0, 'asc']
            ];
            if (this.oTable) {
                if (this.oTable.fnSettings()) {
                    oldSort = this.oTable.fnSettings().aaSorting;
                }
            }
            this.oTable = this.table.dataTable({
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
                    {'bSortable': false, 'aTargets': [0, 1, 11, 12, 13]},
                    {'sType': App.config.i18n.DATE_SORT, 'aTargets': [8]},
                    {'sType': 'strip_html', 'aTargets': [2]}
                ]
            });
            this.$el.parent().find('.dataTables_filter input').attr('placeholder', App.config.i18n.FILTER);
        },

        onError: function (model, error) {
            var errorMessage = error ? error.responseText : model;
            window.alert(errorMessage);
        }

    });

    return BomContentView;
});

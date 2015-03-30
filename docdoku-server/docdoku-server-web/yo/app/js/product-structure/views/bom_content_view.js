/*global _,define,App,window*/
define([
	'backbone',
	'mustache',
	'views/bom_item_view',
	'text!templates/bom_content.html',
	'collections/part_collection',
    'common-objects/views/prompt',
    'common-objects/views/security/acl_edit'
],function (Backbone, Mustache, BomItemView, template, PartList, PromptView, ACLEditView) {
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
            this.bindEvent();
            return this;
        },

        bindEvent: function () {
            // Try to remove this
            Backbone.Events.on('part:saved', this.resetCollection);
        },

        onHeaderSelectionChanged: function (e) {
            _.invoke(this.itemViews, 'setSelectionState', e.target.checked);
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
            this.partsCollection = new PartList();
            this.partsCollection.setFilterUrl(component.getUrlForBom());
            this.listenTo(this.partsCollection, 'reset', this.addAllBomItem);
            this.partsCollection.fetch({reset: true});
        },

        showRoot: function (rootComponent) {
            this.itemViews = [];
            this.partsCollection = new PartList();
            this.partsCollection.setFilterUrl(rootComponent.getRootUrlForBom());
            this.listenTo(this.partsCollection, 'reset', this.addAllBomItem);
            this.partsCollection.fetch({reset: true});
        },

        addAllBomItem: function (parts) {
            this.render();
            parts.each(this.addBomItem, this);
            this.notifySelectionChanged();
            this.dataTable();
        },

        addBomItem: function (part) {
            var bomItemView = new BomItemView({model: part}).render();
            this.listenTo(bomItemView.model, 'change', this.notifySelectionChanged);
            this.itemViews.push(bomItemView);
            this.tbody.append(bomItemView.el);
        },

        checkedViews: function () {
            return _.filter(this.itemViews, function (itemView) {
                return itemView.isChecked();
            });
        },

        actionCheckout: function () {
            var self = this;

            _.each(this.checkedViews(), function (view) {
                view.model.checkout().then(self.onSuccess);
            });

            return false;
        },

        actionUndocheckout: function () {
            var self = this;

            _.each(this.checkedViews(), function (view) {
                view.model.undocheckout().then(self.onSuccess);
            });

            return false;
        },

        actionCheckin: function () {
            var self = this;

            _.each(this.checkedViews(), function (view) {
                if (!view.model.getLastIteration().get('iterationNote')) {
                    var promptView = new PromptView();
                    promptView.setPromptOptions(App.config.i18n.ITERATION_NOTE, App.config.i18n.ITERATION_NOTE_PROMPT_LABEL, App.config.i18n.ITERATION_NOTE_PROMPT_OK, App.config.i18n.ITERATION_NOTE_PROMPT_CANCEL);
                    window.document.body.appendChild(promptView.render().el);
                    promptView.openModal();

                    self.listenTo(promptView, 'prompt-ok', function (args) {
                        var iterationNote = args[0];
                        if (_.isEqual(iterationNote, '')) {
                            iterationNote = null;
                        }
                        view.model.getLastIteration().save({
                            iterationNote: iterationNote
                        }).success(function () {
                            view.model.checkin().success(view.model.fetch().success(self.onSuccess));
                        });

                    });

                    self.listenTo(promptView, 'prompt-cancel', function () {
                        view.model.checkin().success(view.model.fetch().success(self.onSuccess));
                    });

                } else {
                    view.model.checkin().success(view.model.fetch().success(self.onSuccess));
                }
            });

            return false;
        },

        onSuccess: function () {
            Backbone.Events.trigger('part:saved');
        },

        resetCollection: function () {
            this.partsCollection.fetch();
        },

        actionUpdateACL:function(){
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
                    { 'bSortable': false, 'aTargets': [ 0, 1, 11, 12, 13 ] },
                    { 'sType': App.config.i18n.DATE_SORT, 'aTargets': [8] },
                    { 'sType': 'strip_html', 'aTargets': [2] }
                ]
            });
            this.$el.parent().find('.dataTables_filter input').attr('placeholder', App.config.i18n.FILTER);
        },

        onError:function(model, error){
            var errorMessage = error ? error.responseText : model;
            window.alert(errorMessage);
        }

    });

    return BomContentView;
});

/*global define*/
define([ "backbone", "mustache", "views/bom_item_view", "text!templates/bom_content.html", "collections/part_collection"],
    function (Backbone, Mustache, BomItemView, template, PartList) {
        var BomContentView = Backbone.View.extend({

            el: "#bom_table_container",

            events: {
                "change th > input": "onHeaderSelectionChanged",
                "change td > input": "onItemSelectionChanged"
            },

            initialize: function () {
                _.bindAll(this);
                this.itemViews = [];
            },

            render: function () {
                this.$el.html(Mustache.render(template, {i18n: APP_CONFIG.i18n}));
                this.table = this.$('table');
                this.tbody = this.$('tbody');
                return this;
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
                this.listenTo(this.partsCollection, "reset", this.addAllBomItem);
                this.partsCollection.fetch({reset: true});
            },

            showRoot: function (rootComponent) {
                this.itemViews = [];
                this.partsCollection = new PartList();
                this.partsCollection.setFilterUrl(rootComponent.getRootUrlForBom());
                this.listenTo(this.partsCollection, "reset", this.addAllBomItem);
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
                this.listenTo(bomItemView.model, "change", this.notifySelectionChanged);
                this.itemViews.push(bomItemView);
                this.tbody.append(bomItemView.el);
            },

            checkedViews: function () {
                return _.filter(this.itemViews, function (itemView) {
                    return itemView.isChecked();
                });
            },

            actionCheckout: function () {
                _.each(this.checkedViews(), function (view) {
                    view.model.checkout();
                });
                return false;
            },

            actionUndocheckout: function () {
                _.each(this.checkedViews(), function (view) {
                    view.model.undocheckout();
                });
                return false;
            },

            actionCheckin: function () {
                _.each(this.checkedViews(), function (view) {
                    view.model.checkin();
                });
                return false;
            },

            dataTable: function () {
                var oldSort = [
                    [0, "asc"]
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
                        sSearch: "<i class='fa fa-search'></i>",
                        sEmptyTable: APP_CONFIG.i18n.NO_DATA,
                        sZeroRecords: APP_CONFIG.i18n.NO_FILTERED_DATA
                    },
                    sDom: 'ft',
                    aoColumnDefs: [
                        { "bSortable": false, "aTargets": [ 0 ] },
                        { "sType": APP_CONFIG.i18n.DATE_SORT, "aTargets": [7, 8] }
                    ]
                });
                this.$el.parent().find(".dataTables_filter input").attr("placeholder", APP_CONFIG.i18n.FILTER);
            }

        });

        return BomContentView;
    });

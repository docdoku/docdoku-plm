define(["views/bom_item_view", "text!templates/bom_content.html", "i18n!localization/nls/product-structure-strings","collections/part_collection","i18n!localization/nls/datatable-strings"], function (BomItemView, template, i18n, PartList, i18nDt) {
    var BomContentView = Backbone.View.extend({

        el: "#bom_table_container",

        events: {
            "change th > input": "onHeaderSelectionChanged",
            "change td > input": "onItemSelectionChanged"
        },

        template: Mustache.compile(template),

        initialize: function() {
            _.bindAll(this);
            this.itemViews = [];
        },

        render: function() {
            this.$el.html(this.template({i18n : i18n}));
            this.table = this.$('table');
            this.tbody = this.$('tbody');
            return this;
        },

        onHeaderSelectionChanged: function(e) {
            _.invoke(this.itemViews, 'setSelectionState', e.target.checked);
            this.notifySelectionChanged();
        },

        onItemSelectionChanged: function() {
            this.notifySelectionChanged();
        },

        notifySelectionChanged: function() {
            this.trigger('itemSelectionChanged', this.checkedViews());
        },

        update: function(component) {
            this.itemViews = [];
            this.partsCollection = new PartList();
            this.partsCollection.setFilterUrl(component.getUrlForBom());
            this.listenTo(this.partsCollection, "reset", this.addAllBomItem);
            this.partsCollection.fetch({reset:true});
        },

        showRoot:function(rootComponent){
            this.itemViews = [];
            this.partsCollection = new PartList();
            this.partsCollection.setFilterUrl(rootComponent.getRootUrlForBom());
            this.listenTo(this.partsCollection, "reset", this.addAllBomItem);
            this.partsCollection.fetch({reset:true});
        },

        addAllBomItem: function(parts) {
            this.render();
            parts.each(this.addBomItem, this);
            this.notifySelectionChanged();

            this.table.dataTable({
                bDestroy:true,
                iDisplayLength:-1,
                oLanguage:{
                    sSearch: "<i class='icon-search'></i>"
                },
                sDom : 'ft',
                aoColumnDefs: [
                    { "bSortable": false, "aTargets": [ 0 ] }
                ]
            });
            this.$el.parent().find(".dataTables_filter input").attr("placeholder",i18nDt.FILTER);
        },

        addBomItem: function(part) {
            var bomItemView = new BomItemView({model: part}).render();
            this.listenTo(bomItemView.model, "change", this.notifySelectionChanged);
            this.itemViews.push(bomItemView);
            this.tbody.append(bomItemView.el);
        },

        checkedViews: function() {
            return _.filter(this.itemViews, function(itemView) {
                return itemView.isChecked();
            });
        },

        actionCheckout: function() {
            _.each(this.checkedViews(), function(view) {
                view.model.checkout();
            });
            return false;
        },

        actionUndocheckout: function() {
            _.each(this.checkedViews(), function(view) {
                view.model.undocheckout();
            });
            return false;
        },

        actionCheckin: function() {
            _.each(this.checkedViews(), function(view) {
                view.model.checkin();
            });
            return false;
        }

    });

    return BomContentView;
});

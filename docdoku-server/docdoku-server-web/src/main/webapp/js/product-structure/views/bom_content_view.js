define(["views/bom_item_view", "text!templates/bom_content.html", "i18n!localization/nls/product-structure-strings"], function (BomItemView, template, i18n) {
    var BomContentView = Backbone.View.extend({

        el: $("#bom_table_container"),

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
            this.componentSelectedInTreeView = component;
            this.tbody.empty();
            this.itemViews = [];
            if (component.isAssembly()) {
                if (component.children.isEmpty()) {
                    this.listenTo(component.children, 'reset', this.addAllBomItem);
                    component.children.fetch();
                } else {
                    this.addAllBomItem(component.children);
                }
            } else {
                this.addBomItem(component);
            }
        },

        addAllBomItem: function(components) {
            components.each(this.addBomItem, this);
            this.notifySelectionChanged();
        },

        addBomItem: function(componentItem) {
            var bomItemView = new BomItemView({model: componentItem}).render();
            this.itemViews.push(bomItemView);
            this.tbody.append(bomItemView.el);
        },

        checkedViews: function() {
            return _.filter(this.itemViews, function(itemView) {
                return itemView.isChecked();
            });
        },

        actionCheckout: function() {
            var self = this;
            _.each(this.checkedViews(), function(view) {
                view.model.checkout(self.refreshContent);
            });
            return false;
        },

        actionUndocheckout: function() {
            var self = this;
            _.each(this.checkedViews(), function(view) {
                view.model.undocheckout(self.refreshContent);
            });
            return false;
        },

        actionCheckin: function() {
            var self = this;
            _.each(this.checkedViews(), function(view) {
                view.model.checkin(self.refreshContent);
            });
            return false;
        },

        refreshContent: function() {
            this.tbody.empty();
            this.itemViews = [];
            var self = this;
            if (this.componentSelectedInTreeView.isAssembly()) {
                //to refresh the bom content, we need to re fetch all the items
                //we use silent to prevent adding/refreshing components in the tree view which listen for 'reset' event
                this.componentSelectedInTreeView.children.fetch({
                    success: function(collection) {
                        self.addAllBomItem(collection);
                    },
                    silent: true
                });
            }
        }

    });

    return BomContentView;
});

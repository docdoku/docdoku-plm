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
            this.tbody.empty();
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
        }

    });

    return BomContentView;
});

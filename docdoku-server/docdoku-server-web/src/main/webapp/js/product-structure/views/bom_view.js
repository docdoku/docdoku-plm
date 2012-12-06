define(["views/bom_item_view"], function (BomItemView) {

    var BomView = Backbone.View.extend({

        el: $("#bom_table_container"),

        initialize: function() {
            this.tbody = this.$('tbody');
        },

        update: function(component) {
            this.tbody.empty();
            if (component.isAssembly()) {
                if (component.children.isEmpty()) {
                    component.children.on('reset', this.addAllBomItem, this).fetch();
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
            this.tbody.append(bomItemView.el);
        }

    });

    return BomView;
});

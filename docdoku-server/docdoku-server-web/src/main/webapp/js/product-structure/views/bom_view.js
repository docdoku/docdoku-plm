define(["views/bom_item_view", "text!templates/bom.html"], function (BomItemView, template, i18n) {

    var BomView = Backbone.View.extend({

        el: $("#bom_table_container"),

        template: Mustache.compile(template),

        render: function() {
            this.$el.html(this.template({i18n : i18n}));
            this.tbody = this.$('tbody');
            return this;
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
            this.tbody.append(bomItemView.el);
        }

    });

    return BomView;
});

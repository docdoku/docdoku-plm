define(['text!templates/bom_item.html'], function(template) {

    var BomItemView = Backbone.View.extend({

        tagName: 'tr',

        events: {
            "click .component_number": "onComponentClicked"
        },

        template: Mustache.compile(template),

        render: function() {
            this.$el.html(this.template({
                number: this.model.getNumber(),
                amount: this.model.getAmount(),
                version: this.model.getVersion(),
                name: this.model.getName(),
                iteration: this.model.getIteration()
            }));
            return this;
        },

        onComponentClicked: function() {
            var self = this;
            require(['views/component_modal_view'], function(ComponentModalView) {
                var componentModalView = new ComponentModalView({
                    model: self.model
                });
                $('body').append(componentModalView.render().el);
                componentModalView.show();
            });
        }

    });

    return BomItemView;

});

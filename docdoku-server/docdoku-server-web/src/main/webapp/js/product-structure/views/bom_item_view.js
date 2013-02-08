define(['text!templates/bom_item.html'], function(template) {

    var BomItemView = Backbone.View.extend({

        tagName: 'tr',

        events: {
            "click .component_number": "onComponentClicked"
        },

        template: Mustache.compile(template),

        render: function() {
            this.$el.html(this.template(this.model));
            this.$input = this.$("input");
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
        },

        isChecked: function() {
            return this.$input[0].checked;
        },

        setSelectionState: function(state) {
            this.$input[0].checked = state;
        }

    });

    return BomItemView;

});

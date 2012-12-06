define(function () {

    var BomItemView = Backbone.View.extend({

        tagName: 'tr',

        template: _.template("<td><%= number %></td>" +
            "<td><%= name %></td>" +
            "<td><%= version %></td>" +
            "<td><%= iteration %></td>"+
            "<td><%= amount %></td>"),

        render: function() {
            this.$el.html(this.template({
                number: this.model.attributes.number,
                amount: this.model.getAmount(),
                version: this.model.attributes.version,
                iteration: this.model.attributes.iteration
            }));
            return this;
        }

    });

    return BomItemView;

});

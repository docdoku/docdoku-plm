window.BomItemView = Backbone.View.extend({


    tagName:'tr',

    template: _.template("<td><%= number %></td>" +
                         "<td><%= name %></td>" +
                         "<td><%= version %></td>" +
                         "<td><%= iteration %></td>"+
                         "<td><%= instances.length %></td>"),
    render: function(){


        $("#bom_table tbody").append(this.$el.html(this.template(this.model.toJSON())));
    }
});
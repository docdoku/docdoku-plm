window.PartItemView = Backbone.View.extend({

    tagName:'li',

    template: _.template("<a href='#'><label class='checkbox'><input type='checkbox' value=''><%= number %></label></a>"),

    initialize:function(){

    },
    render: function(){

        this.$el.html(this.template(this.model.toJSON()));

        if(this.model.isNode()){
            console.log('printing node');
            this.$el.find('label').addClass("isNode");
        }

        return this;
    }
});
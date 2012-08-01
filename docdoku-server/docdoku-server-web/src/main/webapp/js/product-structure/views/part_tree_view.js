window.PartTreeView = Backbone.View.extend({
        tagName:'li',
        template: _.template("<a href='#'><label class='checkbox'><input type='checkbox' value=''><%= number %></label></a>"),

        //template:'<span class="node-label"></span><ul class="nav nav-list node-tree"></ul>',

        initialize: function() {
            this.model.bind('change', this.render, this);
        },

        render: function() {
            console.log(this.model.toJSON());
            $(this.el).html(this.template(this.model.toJSON()));

            if(this.model.toJSON().isNode){
                $(this.el).addClass('node');
            }

            return this;
        }
});
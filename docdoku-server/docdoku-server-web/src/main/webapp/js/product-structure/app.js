window.AppView = Backbone.View.extend({

    el: $("#workspace"),

    initialize: function() {
        Parts.bind('add', this.addOnePart, this);
        Parts.bind('reset', this.addAll, this);
        Parts.fetch();
    },

    addAll: function() {
        Parts.each(this.addOnePart);
    },

    addOnePart: function(part){
        var view = new PartTreeView({model: part});
        this.$("#product_nav_list").append(view.render().el);
    }

});

window.App = new AppView;
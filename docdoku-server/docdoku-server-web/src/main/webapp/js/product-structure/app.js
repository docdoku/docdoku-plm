window.AppView = Backbone.View.extend({

    el: $("#workspace"),

    initialize: function() {
        var allParts = new PartCollection;
        var partNodeView = new PartNodeView({collection:allParts, parentView: $("#product_nav_list")});
        allParts.fetch();

    }

});

window.App = new AppView;
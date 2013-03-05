define([
    "text!templates/nav_list_action_bar.html",
    "collections/result_path_collection"
], function (
    template,
    ResultPathCollection
    ) {

    var SearchView = Backbone.View.extend({

        el: '#nav_list_action_bar',

        template:Mustache.compile(template),

        events: {
            "submit form#nav_list_search" : "search"
        },

        initialize: function() {
            this.collection = new ResultPathCollection();
        },

        render:function(){
            this.$el.html(this.template());
            return this;
        },

        search: function(e){

            var searchString = e.target.children[0].value.trim();

            if(searchString.length > 0) {
                this.collection.searchString = searchString;
                this.collection.fetch();
            } else {
                this.collection.reset();
            }

            return false;
        }

    });

    return SearchView;

});
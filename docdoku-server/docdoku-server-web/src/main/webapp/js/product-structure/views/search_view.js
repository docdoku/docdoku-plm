define([
    "collections/result_path_collection"
], function (
    ResultPathCollection
    ) {

    var SearchView = Backbone.View.extend({

        el: 'div#search_control_container',

        events: {
            "submit form#nav_list_search" : "search"
        },

        initialize: function() {
            this.searchInput = this.$el.find('input');
            this.collection = new ResultPathCollection();
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
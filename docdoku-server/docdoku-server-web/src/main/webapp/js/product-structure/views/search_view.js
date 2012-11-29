define([
    "collections/result_path_collection"
], function (
    ResultPathCollection
    ) {

    var LayersListView = Backbone.View.extend({

        el: 'div#search_control_container',

        events: {
            "click a#nav_list_search_button" : "search"
        },

        initialize: function() {
            this.searchInput = this.$el.find('input');
            this.collection = new ResultPathCollection();
            this.collection.bind('reset', this.onAllResultPathAdded, this);
        },

            onAllResultPathAdded: function() {
                this.collection.each(this.onResultPathAdded,this);
            },

            onResultPathAdded: function(resultPath) {
                console.log(resultPath.path);
            },

        search: function(){
            var searchString = this.searchInput.val().trim();
            if(searchString.length > 0){
                console.log(searchString);
                this.collection.searchString = searchString;
                this.collection.fetch();
            }else
                this.collection.reset();
        }

    });

    return LayersListView;

});
define(function(){

    NotificationCollection = Backbone.Collection.extend({

        model: Notification,

        getId:function(){
          return this.get("id");
        },
            
        getContent: function() {
            return this.get("content");
        },

        getTitle: function() {
            return this.get("title");
        },

        getDate: function() {
            return this.get("date");
        },

        getActions: function(){
            return this.get("actions");
        }

    });

    return NotificationCollection;

});

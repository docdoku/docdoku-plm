define([],function(){

    var Baseline = Backbone.Model.extend({

        getId:function(){
            return this.get("id");
        },
        getName:function(){
            return this.get("name");
        },
        getDescription:function(){
            return this.get("description");
        },
        getCreationDate:function(){
            return this.get("creationDate");
        }

    });

    return Baseline;
});
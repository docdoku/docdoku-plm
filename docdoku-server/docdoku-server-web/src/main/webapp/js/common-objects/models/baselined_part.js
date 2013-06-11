define([],function(){

    var BaselinedPart = Backbone.Model.extend({

        getNumber:function(){
            return this.get("number");
        },

        getVersion:function(){
            return this.get("version");
        },

        getLastIteration:function(){
            return this.get("lastIteration");
        },

        getIteration:function(){
            return this.get("iteration");
        },

        setIteration:function(iteration){
            this.set("iteration",iteration);
        },

        setExcluded:function(excluded){
            this.set("excluded",excluded);
        },
        isExcluded:function(){
            return this.get("excluded");
        }

    });

    return BaselinedPart;
})
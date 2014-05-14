define([],function(){

    var Baseline = Backbone.Model.extend({

        getId:function(){
            return this.get("id");
        },
        getName:function(){
            return this.get("name");
        },
        getType:function(){
            return this.get("type");
        },
        isReleased:function(){
            return this.get("type")=="RELEASED";
        },
        getDescription:function(){
            return this.get("description");
        },
        getCreationDate:function(){
            return this.get("creationDate");
        },
        getBaselinedParts:function(){
            return this.get("baselinedParts");
        },
        setBaselinedParts:function(baselinedParts){
            this.set("baselinedParts",baselinedParts);
        },
        duplicate:function(args){
            $.ajax({
                type: "POST",
                url: this.url()+"/duplicate",
                data: JSON.stringify(args.data),
                contentType: "application/json; charset=utf-8",
                success: args.success,
                error: args.error
            });
        }

    });

    return Baseline;
});
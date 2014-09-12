'use strict';
define([],function(){

    var Baseline = Backbone.Model.extend({
	    init: function(){

	    },

        getId:function(){
            return this.get('id');
        },
        getName:function(){
            return this.get('name');
        },
        getDescription:function(){
            return this.get('description');
        },
        getCreationDate:function(){
            return this.get('creationDate');
        },
        duplicate:function(args){
            var _this = this;
            $.ajax({
                type: 'POST',
                url: this.url()+'/duplicate',
                data: JSON.stringify(args.data),
                contentType: 'application/json; charset=utf-8',
                success: function(data){
                    var duplicatedBaseline = new Baseline(data);
                    duplicatedBaseline.setConfigurationItemId(_this.getConfigurationItemId());
                    _this.collection.add(duplicatedBaseline);
                    args.success(duplicatedBaseline);
                },
                error: args.error
            });
        }
    });

    return Baseline;
});
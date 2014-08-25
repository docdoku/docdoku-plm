'use strict';
define([],function(){
    var BaselinedPart = Backbone.Model.extend({

        getNumber:function(){return this.get('number');},

        getVersion:function(){return this.get('version');},
        setVersion:function(version){this.set('version',version);},

        getIteration:function(){return this.get('iteration');},
        setIteration:function(iteration){this.set('iteration',iteration);},

        getAvailableIterations:function(){return this.get('availableIterations');},

        isExcluded:function(){return this.get('excluded');},
        setExcluded:function(excluded){this.set('excluded',excluded);}
    });

    return BaselinedPart;
});

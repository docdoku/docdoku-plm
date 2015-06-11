/*global define,App,$*/
define([
    'backbone',
    'mustache',
    'text!common-objects/templates/typedLink/typed-link-item.html',
    'common-objects/views/alert'
], function (Backbone, Mustache, template, AlertView){
    'use strict';

    var TypedLinkItemView = Backbone.View.extend({

        className:'well',

        events:{
        },

        initialize: function(){
            this.model = this.options.model;
        },


        render: function () {

            var data = {
                i18n: App.config.i18n,
                isCreationMode : this.model.isCreationMode,
                canSuppress : ['wip','latest','latest-released'].indexOf(App.config.configSpec)!==-1,
                source : this.model.source,
                target : this.model.target,
                availableType: this.model.availableType,
                description : this.model.pathToPath.description,
                type : this.model.pathToPath.type
            };

            this.$el.html(Mustache.render(template, data));

            return this;
        }



    });

    return TypedLinkItemView;
});

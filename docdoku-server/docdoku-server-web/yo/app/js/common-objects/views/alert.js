/*global define*/
define([
    'common-objects/views/base',
    'mustache',
    'text!common-objects/templates/alert.html'
], function (BaseView, Mustache, template) {
    'use strict';
    var AlertView = Backbone.View.extend({
        event:{
            'click .close': 'onClose'
        },

        render: function(){
            this.$el.html(Mustache.render(template,{
                model : {
                    type : this.options.type,
                    title : this.options.title,
                    message: this.options.message
                }
            }));
            this.bindDomElements();
            return this;
        },

        onClose : function(){
            this.remove();
        }

    });
    return AlertView;
});

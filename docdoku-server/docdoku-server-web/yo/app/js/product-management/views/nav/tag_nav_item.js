/*global define,App,bootbox*/
define([
    'backbone',
    'mustache',
    'text!templates/nav/tag_nav_item.html'
], function (Backbone, Mustache, template) {
	'use strict';
    var TagNavItemView = Backbone.View.extend({
        events:{
            'click .delete': 'actionDelete',
            'mouseleave .header': 'hideActions'
        },
        render: function () {
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n, workspaceId: App.config.workspaceId, model:this.model}));
            return this;
        },
        hideActions:function() {
            this.$('.header .btn-group').first().removeClass('open');
        },
        actionDelete:function() {
            this.hideActions();
            var that = this ;
            bootbox.confirm(App.config.i18n.DELETE_TAG_QUESTION, function(result){
                if(result){
                    that.model.destroy({
                        dataType: 'text' // server doesn't send a json hash in the response body
                    }).success(that.remove.bind(that));
                }
            });
            return false;
        }
    });

    return TagNavItemView;
});

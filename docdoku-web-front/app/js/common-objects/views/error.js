/*global _,$,define,App*/
define([
    'backbone',
    'mustache',
    'text!common-objects/templates/error.html'
], function (Backbone, Mustache, template) {
	'use strict';
    var ErrorView = Backbone.View.extend({

        events: {
            'click .disconnect': 'disconnect',
            'click .workspace-management': 'toWorkspaceManagement',
            'click .back': 'back'
        },

        initialize: function () {
            _.bindAll(this);
        },

        renderError:function(xhr){

            if(xhr.status === 502){
                return this.renderServerUnavailable();
            }

            if(xhr.status === 404){
                return this.render404();
            }

            if(xhr.status === 403){
                return this.render({
                    title:xhr.statusText,
                    content:xhr.responseText
                });
            }

            return this.renderUnexpectedError(xhr);

        },
        render404:function(){
            return this.render({
                title:App.config.i18n.SORRY,
                content:App.config.i18n.NOTHING_HERE
            });
        },
        renderServerUnavailable:function(){
            return this.render({
                title:App.config.i18n.SORRY,
                content:App.config.i18n.SERVER_NOT_AVAILABLE
            });
        },
        renderUnexpectedError:function(xhr){
            return this.render({
                title:App.config.i18n.SORRY,
                content:App.config.i18n.UNEXPECTED_ERROR,
                xhr:xhr
            });
        },
        render: function (opts) {
            this.$el.html(Mustache.render(template, {
                title: opts.title,
                content: opts.content,
                i18n: App.config.i18n,
                xhr: opts.xhr
            }));
            this.$el.addClass('error-page');
            return this;
        },

        toWorkspaceManagement:function(){
            window.location.href = App.config.contextPath + '/workspace-management/';
        },

        back:function(){
            window.history.back();
        },

        disconnect:function(){
            delete localStorage.jwt;
            $.get(App.config.contextPath + '/api/auth/logout').complete(function () {
                location.reload();
            });
        }

    });

    return ErrorView;
});

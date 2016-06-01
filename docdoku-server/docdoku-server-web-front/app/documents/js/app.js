/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/document-permalink.html',
    'views/document-revision',
    'common-objects/views/not-found'
], function (Backbone, Mustache, template, DocumentRevisionView, NotFoundView) {
	'use strict';

    var AppView = Backbone.View.extend({

        el: '#content',

        render: function () {
            this.$el.html(Mustache.render(template, {
                i18n: App.config.i18n
            })).show();
            this.$notifications = this.$('.notifications');
            return this;
        },

        onDocumentFetched:function(document){
            this.$('.document-revision').html(new DocumentRevisionView().render(document).$el, null);
        },

        showDocumentRevision:function(workspace, documentId, documentVersion){
            $.getJSON(App.config.contextPath + '/api/shared/' +  workspace + '/documents/'+documentId+'-'+documentVersion)
                .then(this.onDocumentFetched.bind(this), this.onError.bind(this));
        },

        showSharedEntity:function(uuid){
            this.uuid = uuid;
            $.getJSON(App.config.contextPath + '/api/shared/' + uuid + '/documents')
                .then(this.onSharedEntityFetched.bind(this), this.onSharedEntityError.bind(this));
        },

        onSharedEntityFetched:function(part){
            this.$('.document-revision').html(new DocumentRevisionView().render(part, this.uuid).$el);
        },

        onSharedEntityError:function(err){
            if(err.status == 404){
                this.$el.html(new NotFoundView().render(err).$el);
            }
        },

        onError:function(err){
            if(err.status == 404){
                this.$el.html(new NotFoundView().render(err).$el);
            }
            else if(err.status === 403 || err.status === 401){
                 window.location.href = App.config.contextPath + '/?denied=true&originURL=' + encodeURIComponent(window.location.pathname + window.location.hash);
            }
        }

    });

    return AppView;
});

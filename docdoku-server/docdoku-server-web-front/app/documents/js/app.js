/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/document-permalink.html',
    'views/document-revision'
], function (Backbone, Mustache, template, DocumentRevisionView) {
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
            this.$('.document-revision').html(new DocumentRevisionView().render(document).$el);
        },

        showDocumentRevision:function(workspace, documentId, documentVersion){
            this.options.workspace = workspace;
            this.options.documentId = documentId;
            this.options.documentVersion = documentVersion;
            $.getJSON(App.config.contextPath + '/api/shared/' +  this.options.workspace + '/documents/'+this.options.documentId+'-'+this.options.documentVersion)
                .then(this.onDocumentFetched.bind(this), this.onError.bind(this));
        },

        onError:function(err){
            if(err.status === 403 || err.status === 401){
                window.location.href = App.config.contextPath + '/?denied=true&originURL=' + encodeURIComponent(window.location.pathname + window.location.hash);
            }
        }

    });

    return AppView;
});

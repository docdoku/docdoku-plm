/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/document-permalink.html',
    'views/document-revision',
    'common-objects/views/alert'
], function (Backbone, Mustache, template, DocumentRevisionView, AlertView) {
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

        showDocumentRevision:function(workspace, documentId, documentVersion){
            var _this = this;
            $.getJSON(App.config.contextPath + '/api/workspaces/' + workspace + '/documents/'+documentId+'-'+documentVersion)
                .then(function(document){
                    _this.$('.document-revision').html(new DocumentRevisionView().render(document).$el);
                }, this.onError.bind(this));
        },

        onError:function(err){
            if(err.status === 401){
                window.location.href = App.config.contextPath + '/?denied=true&originURL=' + encodeURIComponent(window.location.pathname + window.location.hash);
            }
        }

    });

    return AppView;
});

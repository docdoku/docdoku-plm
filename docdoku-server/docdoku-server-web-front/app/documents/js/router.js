/*global define,App*/
define([
    'backbone',
    'common-objects/common/singleton_decorator'
],
function (Backbone, singletonDecorator) {
    'use strict';
    var Router = Backbone.Router.extend({
        routes: {
            ':workspaceId/:documentId/:documentVersion':   'showDocumentRevision',
            ':uuid':   'showSharedEntity'
        },

        showDocumentRevision:function(workspace, documentId, documentVersion){
            App.appView.showDocumentRevision(workspace, documentId, documentVersion);
	    },

        showSharedEntity:function(uuid){
            App.appView.showSharedEntity(uuid);
        }
    });

    return singletonDecorator(Router);
});

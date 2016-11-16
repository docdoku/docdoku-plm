/*global $,define,App*/
define(['backbone', 'common-objects/models/document_baseline'],
    function (Backbone, ProductBaseline, DocumentBaseline) {
        'use strict';
        var Baselines = Backbone.Collection.extend({

            model: DocumentBaseline,

            url: function () {
                return App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/document-baselines/';
            }

        });

        return Baselines;
    });

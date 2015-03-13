/*global $,define,App*/
define(['backbone', 'common-objects/models/product_baseline', 'common-objects/models/document_baseline'],
    function (Backbone, ProductBaseline, DocumentBaseline) {
        'use strict';
        var Baselines = Backbone.Collection.extend({
            initialize: function (attributes, options) {
                if (options) {
                    this.type = (options.type) ? options.type : 'product';
                    if (this.type === 'product') {
                        this.productId = options.productId;
                    }
                }
            },

            model: function (attrs, options) {
                switch (options.collection.type) {
                    case 'document' :
                        return new DocumentBaseline(attrs, options);
                    case 'product' :
                        return new ProductBaseline(attrs, options);
                    default :
                        return null;
                }
            },

            url: function () {
                switch (this.type) {
                    case 'document' :
                        return App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/document-baselines/';
                    case 'product' :
                        if (this.productId) {
                            return App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/products/' + this.productId + '/baselines';
                        }
                        return App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/products/baselines';
                    default :
                        return '';
                }
            },

            getLastReleaseRevision: function (callback) {
                var lastReleaseRevision = null;
                if (this.productId) {
                    $.ajax({
                        type: 'GET',
                        url: App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/products/' + this.productId + '/releases/last',
                        contentType: 'application/json; charset=utf-8',
                        success: function (data) {
                            lastReleaseRevision = data;
                            if (callback && callback.success) {
                                callback.success(data);
                            }
                        },
                        error: function (data) {
                            if (callback && callback.error) {
                                callback.error(data);
                            }
                        }
                    });
                }
                return lastReleaseRevision;
            }

        });

        return Baselines;
    });

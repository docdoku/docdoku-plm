/*global $,define,App*/
define(['backbone', 'common-objects/models/product_baseline'],
    function (Backbone, ProductBaseline) {
        'use strict';
        var ProductBaselines = Backbone.Collection.extend({

            initialize: function (args, options) {
                this.productId = options ? options.productId : null;
            },

            url: function () {
                if (this.productId) {
                    return App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/product-baselines/'
                        + this.productId + '/baselines';
                }
                return App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/product-baselines';
            },

            model: ProductBaseline
        });

        return ProductBaselines;
    });

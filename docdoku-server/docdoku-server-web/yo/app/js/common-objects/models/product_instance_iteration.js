/*global _,$,define,App*/
define(['backbone'], function (Backbone) {
	'use strict';
    var ProductInstanceIteration = Backbone.Model.extend({
        idAttribute: 'iteration',

        initialize: function () {
            this.className = 'ProductInstanceIteration';
            _.bindAll(this);
        },

        initBaselinedParts: function (context, callbacks) {
            var that = this;
            $.ajax({
                context: context,
                type: 'GET',
                url: this.url() + '/baselined-parts',
                success: function (baselinedParts) {
                    that.setBaselinedParts(baselinedParts);
                    callbacks.success(this);
                }
            });
        },

        urlRoot: function () {
            if (this.getConfigurationItemId) {
                return App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/products/' + this.getConfigurationItemId() +
                    '/product-instances/' + this.getSerialNumber() + '/iterations/';
            } else {
                return this.prototype.urlRoot();
            }
        },
        getSerialNumber: function () {
            return this.get('serialNumber');
        },
        getIteration: function () {
            return this.get('iteration');
        },
        setIteration: function (iteration) {
            return this.set('iteration', iteration);
        },
        getIterationNote: function () {
            return this.get('iterationNote');
        },
        setIterationNote: function (iterationNote) {
            this.set('iterationNote', iterationNote);
        },
        getConfigurationItemId: function () {
            return this.get('configurationItemId');
        },
        getUpdateAuthor: function () {
            return this.get('updateAuthor');
        },
        getUpdateAuthorName: function () {
            return this.get('updateAuthorName');
        },
        getUpdateDate: function () {
            return this.get('updateDate');
        },
        getBaselinedParts: function () {
            return this.get('baselinedPartsList');
        },
        setBaselinedParts: function (baselinedParts) {
            this.set('baselinedPartsList', baselinedParts);
        },
        getBaselinePartsWithReference: function (ref, callback) {
            var baselinedParts = null;
            $.ajax({
                type: 'GET',
                url: this.url() + '/baselined-parts?q=' + ref,
                contentType: 'application/json; charset=utf-8',
                success: function (data) {
                    baselinedParts = data;
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
            return baselinedParts;
        }
    });

    return ProductInstanceIteration;
});
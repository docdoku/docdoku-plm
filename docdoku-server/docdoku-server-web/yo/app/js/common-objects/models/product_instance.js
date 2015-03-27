/*global _,define,App*/
define(['backbone', 'common-objects/collections/product_instance_iterations','common-objects/utils/date'
], function (Backbone, ProductInstanceList,date) {
	'use strict';
    var ProductInstance = Backbone.Model.extend({

        initialize: function () {
            _.bindAll(this);
        },

        idAttribute: 'serialNumber',
        urlRoot: function () {
            if (this.getConfigurationItemId()) {
                return App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/products/' + this.getConfigurationItemId() + '/product-instances';
            } else {
                return App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/products/product-instances';
            }
        },
        parse: function (data) {
            if (data) {
                this.iterations = new ProductInstanceList(data.productInstanceIterations);
                this.iterations.setMaster(this);
                delete data.productInstanceIterations;
                return data;
            }
        },
        getSerialNumber: function () {
            return this.get('serialNumber');
        },
        getConfigurationItemId: function () {
            return this.get('configurationItemId');
        },
        setConfigurationItemId: function (configurationItemId) {
            this.set('configurationItemId', configurationItemId);
        },
        getIterations: function () {
            return this.iterations;
        },
        getNbIterations: function () {
            return this.getIterations().length;
        },
        getLastIteration: function () {
            return this.getIterations().last();
        },
        hasIterations: function () {
            return !this.getIterations().isEmpty();
        },
        getUpdateAuthor: function () {
            return this.get('updateAuthor');
        },
        getUpdateAuthorName: function () {
            return this.get('updateAuthorName');
        },
        getUpdateDate: function () {
            return date.formatTimestamp(
                App.config.i18n._DATE_FORMAT,
                this.get('updateDate')
            );
        },

        getBomUrl: function () {
            return App.config.contextPath + '/product-structure/#' + App.config.workspaceId + '/' + encodeURIComponent(this.getConfigurationItemId()) + '/config-spec/pi-'+encodeURIComponent(this.getSerialNumber())+'/bom' ;
        },

        getSceneUrl: function () {
            return App.config.contextPath + '/product-structure/#' + App.config.workspaceId + '/' + encodeURIComponent(this.getConfigurationItemId()) + '/config-spec/pi-'+encodeURIComponent(this.getSerialNumber())+'/scene' ;
        }
    });

    return ProductInstance;
});

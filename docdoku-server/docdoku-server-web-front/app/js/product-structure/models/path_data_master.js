/*global define,_,App*/
define(['backbone',
    'common-objects/collections/path_data_iterations'
], function (Backbone, PathDataIterations) {

    'use strict';

    var PathDataMaster = Backbone.Model.extend({

        defaults: {
            pathDataIterations: []
        },

        initialize: function (data) {
            this.path = data.path;
            this.serialNumber = data.serialNumber;
            this.iterations = new PathDataIterations(data.pathDataIterations);
            this.iterations.setPathDataMaster(this);
            this.iteration = this.getLastIteration();
            _.bindAll(this);
        },

        url: function () {
            return App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/products/' + App.config.productId + '/product-instances/' + this.serialNumber + '/pathdata/' + this.getPath();
        },

        parse: function (data) {
            if (data) {
                this.iterations = new PathDataIterations(data.pathDataIterations);
                this.iterations.setPathDataMaster(this);
                delete data.pathDataIterations;
                return data;
            }
        },

        getId: function () {
            return this.get('id');
        },

        getIterations: function () {
            return this.iterations;
        },

        getPartAttributes:function(){
            return this.get('partAttributes');
        },
        getPartAttributeTemplates:function(){
            return this.get('partAttributeTemplates');
        },
        getLastIteration: function () {
            if (this.getIterations().length > 0) {
                return this.getIterations().last();
            } else {
                return null;
            }
        },

        getPath: function () {
            return this.get('path');
        },

        getPartLinks: function () {
            return this.get('partLinksList').partLinks;
        },

        setPath: function (path) {
            this.set('path', path);
        },

        setSerialNumber: function (serialNumber) {
            this.set('serialNumber', serialNumber);
        },

        getSerialNumber: function () {
            return this.get('serialNumber');
        },

        hasIterations: function () {
            return this.get('pathDataIterations').length > 0;
        }


    });
    return PathDataMaster;

});



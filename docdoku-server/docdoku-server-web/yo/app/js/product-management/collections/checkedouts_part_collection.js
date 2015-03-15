/*global define,App*/
define([
    'backbone',
    'common-objects/models/part'
], function (Backbone, Part) {
    'use strict';
    var PartList = Backbone.Collection.extend({
        model: Part,

        className: 'PartList',

        setMainPart: function (part) {
            this.part = part;
        },
        initialize: function () {
            this.urlBase = App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/parts/checkedout';
        },

        fetchPageCount: function () {
        },

        hasSeveralPages: function () {
            return false;
        },

        setCurrentPage: function () {
            return this;
        },

        getPageCount: function () {
            return this.pageCount;
        },

        getCurrentPage: function () {
            return 0;
        },

        isLastPage: function () {
            return true;
        },

        isFirstPage: function () {
            return true;
        },

        setFirstPage: function () {
            return this;
        },

        setLastPage: function () {
            return this;
        },

        setNextPage: function () {
            return this;
        },

        setPreviousPage: function () {
            return this;
        },

        url: function () {
            return this.urlBase;
        }

    });

    return PartList;
});

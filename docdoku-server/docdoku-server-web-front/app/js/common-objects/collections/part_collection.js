/*global $,define,App*/
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
        initialize: function (start) {

            this.currentPage = 0;
            this.pageCount = 0;
            this.resultsPerPage = 20;

            this.urlBase = App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/parts?start=';

            if (start) {
                this.currentPage = start;
            }

        },

        setResultsPerPage: function (count) {
            this.resultsPerPage = count;
        },

        fetchPageCount: function () {
            var self = this;
            $.ajax({
                url: App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/parts/count',
                success: function (data) {
                    self.pageCount = Math.ceil(data.count / self.resultsPerPage);
                    self.trigger('page-count:fetch');
                },
                error: function () {
                    self.trigger('page-count:fetch');
                }
            });
        },

        hasSeveralPages: function () {
            return this.pageCount > 1;
        },

        setCurrentPage: function (page) {
            this.currentPage = page;
            return this;
        },

        getPageCount: function () {
            return this.pageCount;
        },

        getCurrentPage: function () {
            return this.currentPage + 1;
        },

        isLastPage: function () {
            return this.currentPage >= this.pageCount - 1;
        },

        isFirstPage: function () {
            return this.currentPage <= 0;
        },

        setFirstPage: function () {
            if (!this.isFirstPage()) {
                this.currentPage = 0;
            }
            return this;
        },

        setLastPage: function () {
            if (!this.isLastPage()) {
                this.currentPage = this.pageCount - 1;
            }
            return this;
        },

        setNextPage: function () {
            if (!this.isLastPage()) {
                this.currentPage++;
            }
            return this;
        },

        setPreviousPage: function () {
            if (!this.isFirstPage()) {
                this.currentPage--;
            }
            return this;
        },

        url: function () {
            return this.urlBase + this.currentPage * this.resultsPerPage + '&length=' + this.resultsPerPage;
        }

    });

    return PartList;
});

/*global _,define,App*/
define(['backbone', 'common-objects/utils/date'],
    function (Backbone, date) {
	    'use strict';
        var ComponentModule = {};

        ComponentModule.Model = Backbone.Model.extend({

            initialize: function () {
                if (this.isAssembly()) {
                    this.children = new ComponentModule.Collection([], { parentUsageLinkId: this.getPartUsageLinkId(), path: this.getPath() });
                }
                _.bindAll(this);
            },

            defaults: {
                name: null,
                number: null,
                version: null,
                description: null,
                author: null,
                authorLogin: null,
                iteration: null,
                standardPart: false,
                partUsageLinkId: null,
                amount: 0,
                components: [],
                assembly: false,
                mail: null,
                checkOutDate: null,
                checkOutUser: null
            },

            getId: function () {
                return this.getNumber() + '-' + this.getVersion() + '-' + this.getIteration();
            },

            isCheckout: function () {
                return !_.isNull(this.attributes.checkOutDate);
            },

            getCheckoutUser: function () {
                return this.get('checkOutUser');
            },

            getCheckoutDate: function () {
                return this.get('checkOutDate');
            },

            getFormattedCheckoutDate: function () {
                if (this.isCheckout()) {
                    return date.formatTimestamp(
                        App.config.i18n._DATE_FORMAT,
                        this.getCheckoutDate()
                    );
                } else {
                    return false;
                }
            },

            isCheckoutByConnectedUser: function () {
                return this.isCheckout() ? this.getCheckoutUser().login === App.config.login : false;
            },

            hasUnreadModificationNotifications: function () {
                // TODO: get the modification notification collection
                return _.select(this.get('notifications') || [], function(notif) {
                    return !notif.acknowledged;
                }).length;
            },

            isAssembly: function () {
                return this.get('assembly');
            },

            isLeaf: function () {
                return !this.isAssembly();
            },

            getPartUsageLinkId: function () {
                return this.get('partUsageLinkId');
            },

            getPath: function () {
                return this.get('path');
            },

            getAmount: function () {
                return this.get('amount');
            },

            getUnit: function () {
                return this.get('unit');
            },

            getName: function () {
                return this.get('name');
            },

            getNumber: function () {
                return this.get('number');
            },

            getVersion: function () {
                return this.get('version');
            },

            getDescription: function () {
                return this.get('description');
            },

            getAuthor: function () {
                return this.get('author');
            },

            getAuthorLogin: function () {
                return this.get('authorLogin');
            },

            getIteration: function () {
                return this.get('iteration') !== 0 ? this.get('iteration') : null;
            },

            isLastIteration: function (iterationNumber) {
                // return TRUE if the iteration is the very last (check or uncheck)
                return this.get('lastIterationNumber') === iterationNumber;
            },

            isStandardPart: function () {
                return this.get('standardPart');
            },

            isForbidden: function () {
                return this.get('accessDeny');
            },

            isReleased : function(){
                return this.get('released');
            },

            getInstancesUrl: function () {
                return App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/products/' + App.config.productId + '/instances?configSpec=' + App.config.configSpec + '&path=' + this.getEncodedPath();
            },

            getEncodedPath : function(){
                var path = this.getPath();
                if(!path){
                    return  '-1';
                }else{
                    return '-1-'+path;
                }
            },

            getUrlForBom: function () {
                if (this.isAssembly()) {
                    return App.config.contextPath +
                        '/api/workspaces/' +
                        App.config.workspaceId +
                        '/products/' + App.config.productId +
                        '/bom?configSpec=' + App.config.configSpec +
                        '&path=' + this.getEncodedPath();
                } else {
                    return App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/parts/' + this.getNumber() + '-' + this.getVersion();
                }
            },

            getRootUrlForBom: function () {
                return App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/parts/' + this.getNumber() + '-' + this.getVersion();
            }

        });

        ComponentModule.Collection = Backbone.Collection.extend({
            model: ComponentModule.Model,

            initialize: function (models, options) {
                this.isRoot = _.isUndefined(options.isRoot) ? false : options.isRoot;
                if (!this.isRoot) {
                    this.parentUsageLinkId = options.parentUsageLinkId;
                    this.path = options.path;
                }
            },

            url: function () {
                var path = this.path ? '-1-'+this.path : '-1';
                return this.urlBase() + '?configSpec=' + App.config.configSpec + '&path=' + path + '&depth=1';
            },

            urlBase: function () {
                return App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/products/' + App.config.productId;
            },

            parse: function (response) {
                if (this.isRoot) {
                    response.path = null;
                    return [response];
                } else {
                    var self = this;
                    return _.map(response.components, function (component) {
                        var path = self.path === null ? component.partUsageLinkId : self.path + '-' + component.partUsageLinkId;
                        return _.extend(component, {path: path});
                    });
                }
            }
        });
        return ComponentModule;
    });

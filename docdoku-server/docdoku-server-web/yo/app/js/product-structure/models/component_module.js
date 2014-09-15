/*global define,APP_CONFIG*/
'use strict';
define(['backbone', 'common-objects/utils/date'],
    function (Backbone, date) {

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
                        APP_CONFIG.i18n._DATE_FORMAT,
                        this.getCheckoutDate()
                    );
                } else {
                    return false;
                }
            },

            isCheckoutByConnectedUser: function () {
                return this.isCheckout() ? this.getCheckoutUser().login === APP_CONFIG.login : false;
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

            getInstancesUrl: function () {
                return APP_CONFIG.contextPath + '/api/workspaces/' + APP_CONFIG.workspaceId + '/products/' + APP_CONFIG.productId + '/instances?configSpec=' + APP_CONFIG.configSpec + '&path=' + this.getPath();
            },

            getUrlForBom: function () {

                if (this.isAssembly()) {
                    return APP_CONFIG.contextPath + '/api/workspaces/' + APP_CONFIG.workspaceId + '/products/' + APP_CONFIG.productId + '/bom?configSpec=' + APP_CONFIG.configSpec + '&partUsageLink=' + this.getPartUsageLinkId();
                } else {
                    return APP_CONFIG.contextPath + '/api/workspaces/' + APP_CONFIG.workspaceId + '/parts/' + this.getNumber() + '-' + this.getVersion();
                }

            },

            getRootUrlForBom: function () {
                return APP_CONFIG.contextPath + '/api/workspaces/' + APP_CONFIG.workspaceId + '/parts/' + this.getNumber() + '-' + this.getVersion();
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
                if (this.isRoot) {
                    return this.urlBase() + '?configSpec=' + APP_CONFIG.configSpec + '&depth=0';
                } else {
                    return this.urlBase() + '?configSpec=' + APP_CONFIG.configSpec + '&partUsageLink=' + this.parentUsageLinkId + '&depth=1';
                }
            },

            urlBase: function () {
                return APP_CONFIG.contextPath + '/api/workspaces/' + APP_CONFIG.workspaceId + '/products/' + APP_CONFIG.productId;
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
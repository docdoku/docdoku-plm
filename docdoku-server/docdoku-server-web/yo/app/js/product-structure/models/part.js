/*global _,$,define,App*/
define(['backbone', 'common-objects/utils/date', 'collections/part_iteration_collection'],
    function (Backbone, Date, PartIterationList) {
	    'use strict';
        var Part = Backbone.Model.extend({

            idAttribute: 'partKey',

            parse: function (data) {
                this.iterations = new PartIterationList(data.partIterations);
                this.iterations.setPart(this);
                delete data.partIterations;
                return data;
            },

            initialize: function () {
                _.bindAll(this);
            },

            init: function (number, version) {
                this.set('number', number);
                this.set('version', version);
                return this;
            },

            getNumber: function () {
                return this.get('number');
            },

            getName: function () {
                return this.get('name');
            },

            getVersion: function () {
                return this.get('version');
            },

            getDescription: function () {
                return this.get('description');
            },

            getPartKey: function () {
                return this.get('partKey');
            },

            getWorkspace: function () {
                return this.get('workspaceId');
            },

            getCheckoutUser: function () {
                return this.get('checkOutUser');
            },

            getFormattedCheckoutDate: function () {
                if (this.isCheckout()) {
                    return Date.formatTimestamp(
                        App.config.i18n._DATE_FORMAT,
                        this.getCheckoutDate()
                    );
                }
            },

            getFormattedCreationDate: function () {
                return Date.formatTimestamp(
                    App.config.i18n._DATE_FORMAT,
                    this.getCreationDate()
                );
            },

            getCheckoutDate: function () {
                return this.get('checkOutDate');
            },

            getCreationDate: function () {
                return this.get('creationDate');
            },

            isCheckoutByConnectedUser: function () {
                return this.isCheckout() ? this.getCheckOutUserLogin() === App.config.login : false;
            },

            getUrl: function () {
                return this.url();
            },

            hasIterations: function () {
                return !this.getIterations().isEmpty();
            },

            getLastIteration: function () {
                return this.getIterations().last();
            },

            getIteration: function () {
                return this.getIterations().last().getIteration();
            },

            getIterations: function () {
                return this.iterations;
            },

            isLastIteration: function (iterationNumber) {
                // return TRUE if the iteration is the very last (check or uncheck)
                return this.get('lastIterationNumber') === iterationNumber;
            },

            getAuthorLogin: function () {
                return this.get('author').login;
            },

            getAuthorName: function () {
                return this.get('author').name;
            },

            getAuthor: function () {
                return this.get('author').name;
            },

            getCheckOutUserName: function () {
                if (this.isCheckout()) {
                    return this.getCheckoutUser().name;
                }
            },

            getCheckOutUserLogin: function () {
                if (this.isCheckout()) {
                    return this.getCheckoutUser().login;
                }
            },

            isStandardPart: function () {
                return this.get('standardPart') ? 1 : 0;
            },

            isStandardPartReadable: function () {
                return this.get('standardPart') ? App.config.i18n.TRUE : App.config.i18n.FALSE;
            },

            isAttributesLocked: function () {
                return this.get('attributesLocked');
            },

            checkout: function () {
                $.ajax({
                    context: this,
                    type: 'PUT',
                    url: this.url() + '/checkout',
                    success: function () {
                        this.fetch();
                    }
                });
            },

            undocheckout: function () {
                $.ajax({
                    context: this,
                    type: 'PUT',
                    url: this.url() + '/undocheckout',
                    success: function () {
                        this.fetch();
                    }
                });
            },

            checkin: function () {
                $.ajax({
                    context: this,
                    type: 'PUT',
                    url: this.url() + '/checkin',
                    success: function () {
                        this.fetch();
                    }
                });
            },

            getPermalink: function () {
                return encodeURI(
                        window.location.origin +
                        App.config.contextPath +
                        '/parts/' +
                        this.getWorkspace() +
                        '/' +
                        this.getNumber() +
                        '/' +
                        this.getVersion()
                );
            },

            isCheckout: function () {
                return !_.isNull(this.get('checkOutDate'));
            },

            url: function () {
                return App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/parts/' + this.getPartKey();
            }

        });

        return Part;

    });

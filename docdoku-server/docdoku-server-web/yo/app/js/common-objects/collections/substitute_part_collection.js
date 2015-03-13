/*global define*/
define([
    'backbone',
    'common-objects/models/part'
], function (Backbone, Part) {
    'use strict';
    var SubstitutePartList = Backbone.Collection.extend({
        model: Part,

        className: 'SubstitutePartList',

        setMainPart: function (part) {
            this.part = part;
        },
        initialize: function () {

        }
    });

    return SubstitutePartList;
});

/*global define*/
define(['backbone'], function (Backbone) {
	'use strict';
    var AttachedFile = Backbone.Model.extend({
        idAttribute: 'fullName'
    });

    return AttachedFile;

});
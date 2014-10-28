/*global define*/
define(['backbone'], function (Backbone) {
	'use strict';
    var Tag = Backbone.Model.extend({
        initialize: function () {
            this.className = 'Tag';
        }
    });
    return Tag;
});

/*global define*/
define(['backbone'],
function (Backbone) {
	'use strict';
    var UserGroupModel = Backbone.Model.extend({
        getId: function () {
            return this.get('id');
        }
    });

    return UserGroupModel;
});

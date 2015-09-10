/*global define,App,$*/
define([
    'backbone',
    'common-objects/models/tag'
], function (Backbone, Tag) {
	'use strict';
    var TagList = Backbone.Collection.extend({
        model: Tag,

        className: 'TagList',

        comparator: function (tagA, tagB) {
            // sort tags by label
            var labelA = tagA.get('label');
            var labelB = tagB.get('label');

            if (labelA === labelB) {
                return 0;
            }
            return (labelA < labelB) ? -1 : 1;
        },


        url: function () {
            return App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/tags';
        },

        createTags: function (tags, callbackSuccess, callbackError) {
            $.ajax({
                context: this,
                type: 'POST',
                url: this.url() + '/multiple',
                data: JSON.stringify({tags:tags}),
                contentType: 'application/json; charset=utf-8',
                success: function () {
                    this.fetch({reset: true});
                    callbackSuccess();
                },
                error:callbackError
            });

        }
    });

    return TagList;
});

/*global _,define*/
define([
    'common-objects/views/components/list'
], function (ListView) {
	'use strict';
    var CollapsibleListView = ListView.extend({
        show: function () {
            this.$el.show();
            this.$el.addClass('in');
            var that = this;
            this.collection.fetch({
                reset: true,
                success: function () {
                    if (_.isFunction(that.shown)) {
                        that.shown();
                    }
                }
            });
        },
        hide: function () {
            this.$el.hide();
            this.$el.removeClass('in');
            this.clear();
        }
    });
    return CollapsibleListView;
});

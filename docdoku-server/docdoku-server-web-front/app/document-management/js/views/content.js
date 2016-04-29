/*global define*/
define([
    'common-objects/views/base'
], function (BaseView) {
    'use strict';
    var ContentView = BaseView.extend({
        el: '#document-management-content',
        initialize: function () {
            BaseView.prototype.initialize.apply(this, arguments);
            // destroy previous content view if any
            if (ContentView._instance) {
                ContentView._instance.destroy();
            }
            // keep track of the created content view
            ContentView._instance = this;
        },
        destroyed: function () {
            this.$el.html('');
        }
    });
    return ContentView;
});

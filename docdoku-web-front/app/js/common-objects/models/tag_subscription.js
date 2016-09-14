/*global define*/
define(['backbone'], function (Backbone) {

    'use strict';

    var TagSubscription = Backbone.Model.extend({

        getTag: function() {
            return this.get('tag');
        },

        setTag: function(tag) {
            this.set('tag', tag);
        },

        isOnIterationChange: function() {
            return this.get('onIterationChange');
        },

        setOnIterationChange: function(onIterationChange) {
            this.set('onIterationChange', onIterationChange);
        },

        isOnStateChange: function() {
            return this.get('onStateChange');
        },

        setOnStateChange: function(onStateChange) {
            this.set('onStateChange', onStateChange);
        }

    });

    return TagSubscription;
});

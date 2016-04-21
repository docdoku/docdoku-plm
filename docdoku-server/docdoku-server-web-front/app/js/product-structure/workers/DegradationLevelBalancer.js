/*global _,self*/
'use strict';
var DegradationLevelBalancer = {};

(function (DLB) {

    /*
     *  Spread qualities on maximum eligible parts
     *
     * input : list of sorted instances
     * output : directives array [{instance:instance,quality:quality}, ...];
     *
     * */


    DLB.apply = function (sorterResult) {

        var instancesList = sorterResult.sortedInstances;
        var directives = {};

        // Init all parts to undefined
        _(instancesList).each(function (instance) {
            directives[instance.id] = {
                instance: instance,
                quality: undefined
            };
        });

        // Take out maxInstances
        var shortenList = instancesList.splice(0, self.WorkerManagedValues.maxInstances);
        var shortenListLength = shortenList.length;

        var onScene = 0;

        var slices = {
            0: 10,
            1: 80,
            2: 10
        };

        var offset = 0;
        var percent = 0;
        _(_.keys(slices)).each(function (degradationLevel) {
            percent += slices[degradationLevel];
            var index = Math.ceil(shortenListLength * percent / 100);
            _(shortenList.slice(offset, index)).each(function (instance) {
                if (instance.globalRating !== -1) {
                    directives[instance.id] = {
                        instance: instance,
                        quality: Math.min(instance.qualities-1,degradationLevel)
                    };
                    onScene++;
                }
            });
            offset = index;
        });

        return {
            directives: _.values(directives).sort(function (a, b) {
                return b.instance.globalRating - a.instance.globalRating;
            }),
            onScene: onScene
        };
    };
})(DegradationLevelBalancer);

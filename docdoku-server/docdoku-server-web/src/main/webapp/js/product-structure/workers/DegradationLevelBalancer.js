var DegradationLevelBalancer = {};

// Restrict the number of qualities asked to ADS
var availableLevels  = [0, 1];

(function (DLB) {

    /*
     * Split an array into n arrays
     * */
    function splitArrayIntoArrays(a, n) {
        var len = a.length, out = [], i = 0;
        while (i < len) {
            var size = Math.ceil((len - i) / n--);
            out.push(a.slice(i, i += size));
        }
        return out;
    }


    /*
     *  Spread qualities on maximum eligible parts
     *
     * input : list of sorted instances
     * output : directives array [{instance:instance,quality:quality}, ...];
     *
     * */


    DLB.apply = function (sorterResult) {

        if(debug){console.log("[Worker] SorterResult | eligible : " + sorterResult.eligible + " eliminated : " + sorterResult.eliminated);}

        var instancesList = sorterResult.sortedInstances;


        var directives = {};

        // Be sure that none of the rest of parts are on scene
        _(instancesList).each(function (instance) {
            directives[instance.id] = {
                instance: instance,
                quality: undefined
            };
        });

        // Take out maxInstances
        var shortenList = instancesList.splice(0, WorkerManagedValues.maxInstances);
        var onScene = 0;
        var explodedShortenList = splitArrayIntoArrays(shortenList,availableLevels.length);

        function getBestQuality(instance, i) {
                while(instance.qualities[i] === undefined && i > 0){i--}
                return i;
        }

        for(var i = 0, l=explodedShortenList.length; i<l; i++){
            _.each(explodedShortenList[i], function(instance){
                var q = getBestQuality(instance,i);
                if (q !== undefined && instance.globalRating != -1) {
                    onScene++;
                    directives[instance.id] = {
                        instance: instance,
                        quality: q
                    };
                } else {
                    // Else it must be unloaded
                    directives[instance.id] = {
                        instance: instance,
                        quality: undefined
                    };
                }
            });
        }
        if(debug){console.log("[Worker] Instances: " + onScene );}

        return {
            directives: _.values(directives).sort(function (a, b) {
                return b.instance.globalRating - a.instance.globalRating;
            }),
            onScene:onScene
        };
    };
})(DegradationLevelBalancer);
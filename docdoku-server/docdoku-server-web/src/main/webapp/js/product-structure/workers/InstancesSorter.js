var InstancesSorter = {};

(function (IS) {
    IS.sort = function (instances) {

        /*
         * Performs rating calculation
         * */

        var result = {
            eligible: 0,
            eliminated: 0,
            minDist:0,
            maxDist:0,
            minAngle:0,
            maxAngle:0,
            minPSize:0,
            maxPSize:0,
            minRating:0,
            maxRating:0,
            sortedInstances: null
        };

        var minProjectedSize = WorkerManagedValues.minProjectedSize/1000;

        // Evaluate global
        _(instances).each(function (instance) {


            /*
            * No geometric data
            * */
            if(!instance.cog || ! instance.radius){
                instance.globalRating = -1;
                result.eliminated++;
                return;
            }

            /*
             * Checked unchecked
             * */

            if (!instance.checked) {
                instance.globalRating = -1; // eliminated
                result.eliminated++;
                return;
            }

            /*
            * Filtering
            * */

            /*if(instance.ENV && !_(WorkerManagedValues.envFilters).contains(instance.ENV)){
                instance.globalRating = -1; // eliminated
                result.eliminated++;
                return;
            }

            if(instance.STATUS && !_(WorkerManagedValues.statusFilters).contains(instance.STATUS)){
                instance.globalRating = -1; // eliminated
                result.eliminated++;
                return;
            }*/

            /*
            * Max distance/angle/projectedSize filtering
            * */

            var dist = Context.cameraDist(instance);
            if (dist > WorkerManagedValues.maxDist) {
                instance.globalRating = -1; // eliminated
                result.eliminated++;
                return;
            }

            var angle = Context.cameraAngle(instance);
            if (angle > WorkerManagedValues.maxAngle) {
                instance.globalRating = -1; // eliminated
                result.eliminated++;
                return;
            }

            var projectedSize = Math.pow(instance.radius*2,1/3) / dist;


            if (projectedSize < minProjectedSize ) {
                instance.globalRating = -1; // eliminated
                result.eliminated++;
                return;
            }

            result.minDist = Math.min(result.minDist,dist);
            result.maxDist = Math.max(result.maxDist,dist);
            result.minAngle = Math.min(result.minAngle,angle);
            result.maxAngle = Math.max(result.maxAngle,angle);
            result.minPSize = Math.min(result.minPSize,projectedSize);
            result.maxPSize = Math.max(result.maxPSize,projectedSize);

            instance.dist=dist;
            instance.angle=angle;
            instance.projectedSize=projectedSize;
            instance.globalRating=0; // eliglibe
            result.eligible++;

        });

        // calculate ratings
        _(instances).each(function(instance){

            if(instance.globalRating>-1){

                var angleRating =  1 - instance.angle/WorkerManagedValues.maxAngle;
                var distanceRating = 1 - instance.dist / result.maxDist;
                var volRating = instance.projectedSize / result.maxPSize;

                angleRating *= WorkerManagedValues.angleRating;
                distanceRating *= WorkerManagedValues.distanceRating;
                volRating *= WorkerManagedValues.volRating;

                // Geometric mean (nth-root of n value's product)
                //instance.globalRating = Math.pow(angleRating * distanceRating * volRating , 1/3);

                // Arithmetic mean
                instance.globalRating = angleRating + distanceRating + volRating ;

                result.minRating = Math.min(result.minRating,instance.globalRating);
                result.maxRating = Math.max(result.maxRating,instance.globalRating);

            }

        });

        if(debug){console.log("[InstancesSorter] " + JSON.stringify(result))}

        // Sort instances on their global rating, descending
        var sortedInstances = _.values(instances);

        sortedInstances.sort(function (a, b) {
            return (b.globalRating - a.globalRating);
        });

        result.sortedInstances = sortedInstances;

        return result;

    }


})(InstancesSorter);

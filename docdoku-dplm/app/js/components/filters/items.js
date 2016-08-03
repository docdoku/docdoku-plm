(function () {
    'use strict';

    angular.module('dplm.filters')

        .filter('lastIteration', function ($filter) {
            var last = $filter('last');
            return function (item) {
                return last(item.documentIterations||item.partIterations);
            };
        })

        .filter('itemsFiles', function ($filter) {
            var lastIteration = $filter('lastIteration');
            return function (items) {
                var fileMap = {};
                items.forEach(function (item) {
                    var lastItemIteration = lastIteration(item);
                    if (item.number && lastItemIteration.nativeCADFile) {
                        fileMap[lastItemIteration.nativeCADFile.fullName] = item;
                    }
                    if (item.id) {
                        lastItemIteration.attachedFiles.forEach(function (file) {
                            fileMap[file.fullName] = item;
                        });
                    }
                });
                return fileMap;
            };
        })

})();

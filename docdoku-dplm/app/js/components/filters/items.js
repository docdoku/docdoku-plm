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
                    if (item.documentMasterId) {
                        lastItemIteration.attachedFiles.forEach(function (file) {
                            fileMap[file.fullName] = item;
                        });
                    }
                });
                return fileMap;
            };
        })

        .filter('canCheckOut',function(){
            return function(selection){
                return selection.filter(function (file) {
                    var item = file.item;
                    return item && !item.checkOutUser && !item.releaseAuthor && !item.obsoleteAuthor;
                });
            };
        })

        .filter('canCheckIn',function(ConfigurationService){
            return function(selection){
                return selection.filter(function (file) {
                    var item = file.item;
                    return item && item.checkOutUser && item.checkOutUser.login === ConfigurationService.configuration.login;
                });
            };
        })

        .filter('canUndoCheckOut',function(ConfigurationService){
            return function(selection){
                return selection.filter(function (file) {
                    var item = file.item;
                    var lastItemIteration = item ? lastIteration(item) : null;
                    return lastItemIteration && item.checkOutUser && item.checkOutUser.login === ConfigurationService.configuration.login && lastItemIteration.iteration > 1;
                });
            };
        });


})();

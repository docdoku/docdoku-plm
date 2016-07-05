(function() {

    'use strict';

    angular.module('dplm.directives.filedrop', [])
        .directive('droppable',function(FolderService){
            return {
                restrict:'A',
                scope:{
                    onFileDropped:'&'
                },
                compile: function compile(tElement, tAttrs, transclude) {
                    return {
                        post: function postLink(scope, iElement, iAttrs, controller) {

                            window.ondragover = function(e) {e.preventDefault(); return false; };
                            window.ondrop = function(e) { e.preventDefault(); return false; };

                            var holder = iElement;
                            holder.on('dragover', function () { holder.addClass('hover'); return false; });
                            holder.on('dragend',function () { holder.removeClass('hover'); return false; });
                            holder.on('dragleave',function () { holder.removeClass('hover'); return false; });
                            holder.on('drop',function (e) {

                                holder.removeClass('hover');

                                var file = e.dataTransfer.files[0];

                                FolderService.isFolder(file.path).then(function(){
                                    scope.onFileDropped({path:file.path});
                                });

                                e.preventDefault();

                                return false;
                            });

                        }
                    };
                }
            };
        });

})();
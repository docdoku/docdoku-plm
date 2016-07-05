angular.module('dplm.menu', [])
    .directive('menuButton',function(){
        return {
            restrict:'E',
            templateUrl:'js/menu/menu-button.html',
            scope:false
        };
    })
    .controller('MenuController', function ($scope,$filter,FolderService,ConfigurationService,WorkspaceService) {
        $scope.workspaces = WorkspaceService.workspaces;

        function buildMenu(){
            $scope.menu = {
                sections : [{
                    id:'folders',
                    name: 'Folders',
                    type: 'toggle',
                    pages: FolderService.folders.map(function(folder){
                        return {
                            name: $filter('fileshortname')(folder.path),
                            type: 'link',
                            url:'#/folder/'+folder.uuid
                        };
                    })
                },{
                    id:'workspaces',
                    name: 'Workspaces',
                    type: 'toggle',
                    pages: $scope.workspaces.map(function(workspace){
                        return {
                            name: workspace,
                            type: 'link',
                            url:'#/workspace/'+workspace
                        };
                    })
                }]
            };
        }
        $scope.openedSection = null;
        $scope.isOpened = function(section){
            return $scope.openedSection === section;
        };
        $scope.open = function(section){
            return $scope.openedSection = $scope.isOpened(section) ? null : section;
        };
        $scope.$watchCollection('workspaces',function(){
            buildMenu();
        });

        $scope.configuration = ConfigurationService.configuration;

        $scope.onFileDropped = function(path){
            if(path){
                FolderService.add(path);
            }
        };

    })

    .controller('FolderMenuController', function ($scope) {

        $scope.onDrop = function () {
        };

    })
    .controller('WorkspaceMenuController', function ($scope, WorkspaceService) {

        $scope.onDrop = function () {
        };

        $scope.refreshWorkspaces = function(){
            WorkspaceService.reset();
            WorkspaceService.getWorkspaces();
        };

    })
    .filter('nospace', function () {
        return function (value) {
            return (!value) ? '' : value.replace(/ /g, '');
        };
    })
    .directive('menuLink', function() {
        return {
            templateUrl: 'js/menu/menu-link.html'
        };
    })

    .directive('menuToggle', function($timeout, $mdUtil) {
        return {

            templateUrl: 'js/menu/menu-toggle.html',
            link: function($scope, $element) {

                $mdUtil.nextTick(function() {
                    $scope.$watch(
                        function () {
                            return $scope.isOpened($scope.section);
                        },
                        function (open) {
                            var $ul = $element.find('ul');

                            var targetHeight = open ? getTargetHeight() : 0;
                            $timeout(function () {
                                $ul.css({height: targetHeight + 'px'});
                            }, 0, false);

                            function getTargetHeight() {
                                var targetHeight;
                                $ul.addClass('no-transition');
                                $ul.css('height', '');
                                targetHeight = $ul.prop('clientHeight');
                                $ul.css('height', 0);
                                $ul.removeClass('no-transition');
                                return targetHeight;
                            }
                        }
                    );
                });

                var parentNode = $element[0].parentNode.parentNode.parentNode;
                if(parentNode.classList.contains('parent-list-item')) {
                    var heading = parentNode.querySelector('h2');
                    $element[0].firstChild.setAttribute('aria-describedby', heading.id);
                }
            }
        };
    })
;
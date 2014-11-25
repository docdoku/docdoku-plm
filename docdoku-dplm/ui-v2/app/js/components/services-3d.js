'use strict';

angular.module('dplm.services.3d',[])

    .service('ModelLoaderService',function($q){

        /*
         Create a threejs 3D object from a file
         * */

        this.load = function(filename){

            var fs = require('fs');
            var deferred = $q.defer();
            var extension = filename.split('.').pop();

            switch (extension){
                case 'obj':

                    fs.readFile(filename, 'utf-8' ,function (err, data) {
                        if (err) deferred.reject(err);
                        else{
                            var loader = new THREE.OBJLoader();
                            var object = loader.parse(data);
                            deferred.resolve(object);
                        }
                    });

                    break;

                case 'stl':

                    fs.readFile(filename, 'binary', function (err, data) {
                        if (err) deferred.reject(err);
                        else{
                            var loader = new THREE.STLLoader();
                            var material = new THREE.MeshPhongMaterial( { ambient: 0x555555, color: 0xAAAAAA, specular: 0x111111, shininess: 200 } );
                            var geometry = loader.parse(data);
                            deferred.resolve(new THREE.Mesh(geometry,material));
                        }
                    });

                    break;

                case 'wrl':

                    fs.readFile(filename, 'utf-8', function (err, data) {
                        if (err) deferred.reject(err);
                        else{
                            var loader = new THREE.VRMLLoader();
                            var object = loader.parse(data);
                            deferred.resolve(object);
                        }
                    });

                    break;

                case 'bin':

                    fs.readFile(filename, 'binary', function (err, data) {
                        if (err) deferred.reject(err);
                        else{
                            var loader = new THREE.BinaryLoader();
                            var str = data.toString();
                            var buffer = new ArrayBuffer( str.length );
                            var bufView = new Uint8Array( buffer );
                            for ( var i = 0, l = str.length; i < l; i ++ ) {
                                bufView[ i ] = str.charCodeAt( i ) & 0xff;
                            }
                            loader.createBinModel( buffer, function(geometry){
                                var mesh = new THREE.Mesh(geometry, new THREE.MeshNormalMaterial());
                                deferred.resolve(mesh);
                            }, '', {} );
                        }
                    });

                    break;

                case 'dae':

                    fs.readFile(filename, 'utf-8', function (err, data) {
                        if (err) deferred.reject(err);
                        else{
                            var loader = new THREE.ColladaLoader();
                            var xmlParser = new DOMParser();
                            var doc = xmlParser.parseFromString( data, "application/xml" );
                            loader.parse( doc, function(dae){
                                deferred.resolve(dae);
                            });
                        }
                    });

                    break;


                default :
                    deferred.reject();
                    break;
            }

            return deferred.promise;

        };

    })

    .directive('modelViewer',function(ModelLoaderService){

        return {
            restrict: 'A',
            scope: {
                'width': '=',
                'height': '=',
                'fillcontainer': '=',
                'filename':'='
            },

            link: function postLink(scope, element, attrs) {

                var camera, scene, renderer,
                    shadowMesh, mesh, light, controls,
                    contW = (scope.fillcontainer) ?
                        element[0].clientWidth : scope.width,
                    contH = scope.height,
                    windowHalfX = contW / 2,
                    windowHalfY = contH / 2;

                var spin = document.createElement('i');
                spin.classList.add('fa','fa-spinner','fa-spin');
                element[0].appendChild(spin);
                var destroy = false;

                scope.init = function () {

                    // Camera
                    camera = new THREE.PerspectiveCamera( 20, contW / contH, 1, 10000 );
                    camera.position.z = 1800;

                    controls = new THREE.OrbitControls( camera,element[0]);
                    //controls.addEventListener( 'change', render );
                    // Scene
                    scene = new THREE.Scene();

                    // Ligthing
                    light = new THREE.DirectionalLight( 0xffffff );
                    light.position.set( 0, 0, 1 );
                    scene.add( light );

                    var canvas = document.createElement( 'canvas' );
                    canvas.width = 128;
                    canvas.height = 128;

                    renderer = new THREE.WebGLRenderer( { antialias: true , alpha:true} );
                    renderer.setClearColor( 0x000000, 0 );
                    renderer.setSize( contW, contH );

                    window.addEventListener( 'resize', scope.onWindowResize, false );

                };

                scope.loadMeshFromFile = function(){
                    ModelLoaderService.load(scope.filename).then(function(object){
                        element[0].removeChild(spin);
                        element[0].appendChild( renderer.domElement );
                        scene.add( object );
                        console.log(object)
                        scope.animate();
                    });
                }

                // -----------------------------------
                // Event listeners
                // -----------------------------------
                scope.onWindowResize = function () {
                    scope.resizeCanvas();
                };

                // -----------------------------------
                // Updates
                // -----------------------------------
                scope.resizeCanvas = function () {

                    contW = (scope.fillcontainer) ?
                        element[0].clientWidth : scope.width;
                    contH = scope.height;

                    windowHalfX = contW / 2;
                    windowHalfY = contH / 2;

                    camera.aspect = contW / contH;
                    camera.updateProjectionMatrix();

                    renderer.setSize( contW, contH );

                };

                // -----------------------------------
                // Draw and Animate
                // -----------------------------------
                scope.animate = function () {
                    if(!destroy){
                        requestAnimationFrame( scope.animate );
                    }
                    controls.update();
                    scope.render();
                };

                scope.render = function () {
                    camera.lookAt( scene.position );
                    renderer.render( scene, camera );
                };

                // -----------------------------------
                // Watches
                // -----------------------------------
                scope.$watch('fillcontainer + width + height', function () {
                    scope.resizeCanvas();
                });

                scope.$on('$destroy',function(){
                    destroy = true;
                });

                // Begin
                scope.init();
                scope.loadMeshFromFile();
            }
        };
    });
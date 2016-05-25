/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/login.html'
], function (Backbone, Mustache, template) {
	'use strict';

    function hasWebGL() {
        try {
            return !! window.WebGLRenderingContext && !! document.createElement( 'canvas' ).getContext( 'experimental-webgl' );
        } catch( e ) {
            return false;
        }
    }

    var errorMessage = window.WebGLRenderingContext ? [
        'Your graphics card does not seem to support <a href="http://khronos.org/webgl/wiki/Getting_a_WebGL_Implementation">WebGL</a>.<br />',
        'Find out how to get it <a href="http://get.webgl.org/">here</a>.'
    ].join( '\n' ) : [
        'Your browser does not seem to support <a href="http://khronos.org/webgl/wiki/Getting_a_WebGL_Implementation">WebGL</a>.<br/>',
        'Find out how to get it <a href="http://get.webgl.org/">here</a>.'
    ].join( '\n' );

    function getParameterByName(name) {
        var url = window.location.href;
        name = name.replace(/[\[\]]/g, "\\$&");
        var regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)"),
            results = regex.exec(url);
        if (!results) return null;
        if (!results[2]) return '';
        return decodeURIComponent(results[2].replace(/\+/g, " "));
    }

    var AppView = Backbone.View.extend({

        el: '#content',

        events:{
            'click .recovery-link':'showRecoveryForm',
            'click .login-form-link':'showLoginForm',
            'submit #login_form':'onLoginFormSubmit'
        },

        render: function () {
            this.$el.html(Mustache.render(template, {
                i18n: App.config.i18n,
                contextPath:App.config.contextPath
            })).show();
            this.$loginForm = this.$('#login_form');
            this.$recoveryForm = this.$('#recovery_form');
            this.renderHomeAnimation();
            return this;
        },

        showRecoveryForm:function(){
            this.$recoveryForm.show()
            this.$loginForm.hide();
        },

        showLoginForm:function(){
            this.$loginForm.show();
            this.$recoveryForm.hide();
        },

        renderHomeAnimation:function(){

            if ( ! hasWebGL() ){
                container.innerHTML = errorMessage;
            }
            // standard global variables
            var scene, camera, renderer, controls;

            // custom global variables
            var model;

            var container = this.$('#demo-scene')[0];

            // FUNCTIONS
            function update(){
                if(model){
                    model.rotation.set(0.45, model.rotation.y+0.005, model.rotation.z+0.005);
                }
                controls.update();
            }

            function animateCamera(){
                camera.position.copy(App.SceneOptions.startCameraPosition);
                new TWEEN.Tween(camera.position)
                    .to(App.SceneOptions.endCameraPosition, 1000)
                    .interpolation(TWEEN.Interpolation.CatmullRom)
                    .easing(TWEEN.Easing.Sinusoidal.InOut)
                    .start();
            }

            function render(){
                renderer.render( scene, camera );
            }

            function animate(){
                requestAnimationFrame(animate);
                update();
                TWEEN.update();
                render();
            }

            function addModelToScene( geometry ){
                var material = new THREE.MeshLambertMaterial( { color: 0xffffff, shading: THREE.FlatShading, overdraw: true } );
                model = new THREE.Mesh( geometry, material );
                model.scale.set(1,1,1);
                model.position.set(0, 0, 0);
                model.rotation.set(0.45, 0, 1.55);
                scene.add( model );
                geometry.computeBoundingBox();
                controls.target.copy(model.geometry.boundingBox.center());
                animate();
                animateCamera();
            }

            function handleResize() {
                camera.aspect = container.clientWidth / container.clientHeight;
                camera.updateProjectionMatrix();
                renderer.setSize(container.clientWidth , container.clientHeight);
                controls.handleResize();
            }

            // SCENE
            scene = new THREE.Scene();

            // CAMERA
            camera = new THREE.PerspectiveCamera( 45, container.clientWidth/container.clientHeight,0.1, 5000);
            scene.add(camera);
            camera.position.set(0,250,200);

            // RENDERER
            renderer = new THREE.WebGLRenderer( {antialias:true, alpha:true} );
            renderer.setSize( container.clientWidth, container.clientHeight);

            // CONTROLS
            controls = new THREE.TrackballControls( camera, container );

            // LIGHT
            var light = new THREE.PointLight(0xffffff);
            light.position.set(-100,200,100);
            scene.add(light);
            var light2 = new THREE.PointLight(0xffffff);
            light2.position.set(100,-200,-100);
            scene.add(light2);

            // SKYBOX/FOG
            scene.fog = new THREE.FogExp2( 0x9999ff, 0.00025 );

            var binaryLoader = new THREE.BinaryLoader();
            binaryLoader.load( App.config.contextPath + '/images/pba.js', addModelToScene);

            var ambientLight = new THREE.AmbientLight(0x111111);
            scene.add(ambientLight);

            container.appendChild( renderer.domElement );
            window.addEventListener('resize', handleResize, false);

        },

        onLoginFormSubmit:function(e){
            delete localStorage.jwt;
            $.ajax({
                type: 'POST',
                url: App.config.contextPath + '/api/auth/login',
                data: JSON.stringify({
                    login:this.$('#login_form-login').val(),
                    password:this.$('#login_form-password').val()
                }),
                contentType: 'application/json; charset=utf-8'
            }).then(function(account, status, xhr ){
                var jwt = xhr.getResponseHeader('jwt');
                localStorage.jwt = jwt;
                var originURL = getParameterByName('originURL') || App.config.contextPath + '/workspace-management/';
                window.location.href = originURL;
            }, this.onLoginFailed.bind(this));
            e.preventDefault();
            return false;
        },

        onLoginFailed:function(err){

        }

    });

    return AppView;
});

(function() {

    window.DocdokuPLMClient = {

        loadAPI: function (host, useRequire, callback){

            var swaggerClient = host+'${server.contextRoot}/client/js/swagger-client.js';
            var swaggerDefinition = host+'${server.contextRoot}/client/swagger.json';

            var d = Date.now();

            var onLoaded = function(api){
                console.log('Api loaded in ' + (Date.now() - d) + ' ms');
                callback(api);
            };

            if(useRequire && typeof require === 'function'){
                require([swaggerClient],function(SwaggerAPI){
                    new SwaggerAPI.SwaggerClient({
                        url: swaggerDefinition,
                        usePromise: true
                    }).then(onLoaded);
                });
            }
            else if(typeof window !== 'undefined'){
                var s = document.createElement('script');
                s.type = 'text/javascript';

                s.onload=function(){
                    s.onload = null;
                    new SwaggerClient({
                        url: swaggerDefinition,
                        usePromise: true
                    }).then(onLoaded);
                };

                (document.getElementsByTagName( "head" )[ 0 ]).appendChild( s );

                s.src = swaggerClient;
            } else {
                console.error('Not supported');
            }
        }
    };

})();
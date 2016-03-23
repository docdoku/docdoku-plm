window.DocdokuPLMClient = {
    api:null,
    loadAPI: function (callback){

        if(DocdokuPLMClient.api){
            return callback(DocdokuPLMClient.api);
        }

        var src = '${server.contextRoot}/client/js/swagger-client.js';
        var s = document.createElement('script');
        s.setAttribute( 'src', src );

        s.onload=function(){
            var d = Date.now();
            new SwaggerClient({
                url: '${server.contextRoot}/client/swagger.json',
                usePromise: true
            }).then(function(api) {
                window.DocdokuPLMClient.api = api;
                console.log('Api loaded in ' + (Date.now() - d) + ' ms');
                console.log(api);
                callback(api);
            });
        };

        document.body.appendChild(s);
    }
};
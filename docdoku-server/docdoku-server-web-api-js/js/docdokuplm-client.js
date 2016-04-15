(function() {
    window.DocdokuPLMClient = {
        loadAPI: function (host){
            return new SwaggerClient({
                url: host,
                usePromise: true
            });
        }
    };
})();
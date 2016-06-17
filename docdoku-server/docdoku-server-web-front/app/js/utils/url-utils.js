define(function(){
    'use strict';
    return {
        getParameterByName:function(name) {
            var url = window.location.href;
            name = name.replace(/[\[\]]/g, '\\$&');
            var regex = new RegExp('[?&]' + name + '(=([^&#]*)|&|#|$)'),
            results = regex.exec(url);
            if (!results) {
                return null;
            }
            if (!results[2]){
                return '';
            }
            return decodeURIComponent(results[2].replace(/\+/g, ' '));
        }
    };
});

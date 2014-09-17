// Returns the fail reason from response headers
var findReasonInResponseHeaders = function(headers){
    var reason = '';
    headers.forEach(function(header){
        if(header.name === 'Reason-Phrase'){
            reason = header.value;
        }
    })
    return reason;
};
'use strict';
//JSF javascript functions
function uncheckPasswordFields(){  
    var p1 = document.getElementById('account-password');
    var p2 = document.getElementById('account-confirmPassword');
    var c = document.getElementById('account-changePassword');
    p2.disabled=p1.disabled=!c.checked;
}

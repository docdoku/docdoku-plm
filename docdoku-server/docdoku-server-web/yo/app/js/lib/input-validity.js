(function ($) {

    // Find a tab where's the first error found, and show it, returns true if the form is invalid, false otherwise
    $.fn.invalidFormTabSwitcher = function(){
        var $tabPanes = this.find('.tab-pane');
        for(var i = 0 ; i < $tabPanes.length; i++){
            if($tabPanes[i].querySelectorAll(':invalid').length){
                this.find('li:eq('+i+') a').tab('show');
                return true;
            }
        }
        return false;
    };

    // Changes the custom validity message
    $.fn.customValidity = function (message) {
        var element = this[0];
        element.oninvalid = function(e) {
            e.target.setCustomValidity("");
            if (!e.target.validity.valid) {
                e.target.setCustomValidity(message);
            }
        };
        element.oninput = function(e) {
            e.target.setCustomValidity("");
        };
    };

})(jQuery);

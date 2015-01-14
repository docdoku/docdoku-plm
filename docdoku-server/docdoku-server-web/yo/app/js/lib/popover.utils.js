(function ($) {
    'use strict';

    var removePopoversIfClickOutside = function(e){
        var $elem = $(e.target);
        if (!$elem.parents('.popover').length && !$elem.hasClass('popover') ) {
            removePopovers();
        }
    };
    var removePopovers = function(){
        $('.popover').remove();
    };

    addEventListener('mousewheel',removePopovers);
    addEventListener('click',removePopoversIfClickOutside);



})(jQuery);

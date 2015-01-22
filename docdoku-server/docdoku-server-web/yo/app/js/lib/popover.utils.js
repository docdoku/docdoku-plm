/*global jQuery*/
(function ($) {
    'use strict';

    function removePopoversIfClickOutside(e){
        var $elem = $(e.target);
        if (!$elem.parents('.popover').length && !$elem.hasClass('popover') ) {
            removePopovers();
        }
    }

    function removePopovers(){
        $('.popover').remove();
    }

    addEventListener('mousewheel',removePopovers);
    addEventListener('click',removePopoversIfClickOutside);



})(jQuery);

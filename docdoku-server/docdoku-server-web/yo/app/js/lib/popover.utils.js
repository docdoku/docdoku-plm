/*global jQuery*/
(function ($) {

    'use strict';

    function removePopovers(){
        $('.popover').remove();
    }

    function removePopoversIfClickOutside(e){
        var $elem = $(e.target);
        if (!$elem.parents('.popover').length && !$elem.hasClass('popover') ) {
            removePopovers();
        }
    }

    addEventListener('mousewheel',removePopovers);
    addEventListener('click',removePopoversIfClickOutside);

})(jQuery);

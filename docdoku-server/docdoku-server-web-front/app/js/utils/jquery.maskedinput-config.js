/*global jQuery*/
(function ($) {
	'use strict';
    $.mask = {
        // Override to handle our mask grammar
        definitions: {
            '#': '[0-9]',
            '%': '[A-Za-z]',
            '*': '[A-Za-z0-9]'
        },
        dataName: 'rawMaskFn',
        placeholder: '_'
    };
})(jQuery);

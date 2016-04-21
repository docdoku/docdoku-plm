/*global _,jQuery*/
(function ($) {
	'use strict';
    $.fn.highlightEffect = function () {
        $(this).effect('highlight', {color: '#999999'}, 1000);
    };

    $.fn.customResizable = function (args) {
        var options = {
            handles: 'e',
            autoHide: true,
            stop: function (e, ui) {
                var parent = ui.element.parent();
                ui.element.css({
                    width: ui.element.width() / parent.width() * 100 + '%',
                    height: '100%'
                });
            }
        };

        _.extend(options, args);
        $(this).resizable(options);
    };
})(jQuery);

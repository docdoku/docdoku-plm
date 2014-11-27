(function(){

    'use strict';

    angular.module('dplm.contextmenu', [])
        .run(function ($document, $filter) {

            function Menu(cutLabel, copyLabel, pasteLabel) {

                var gui = require('nw.gui'),
                    menu = new gui.Menu(),
                    cut = new gui.MenuItem({
                        label: cutLabel || "Cut",
                        click: function () {
                            document.execCommand("cut");
                        }
                    }),
                    copy = new gui.MenuItem({
                        label: copyLabel || "Copy",
                        click: function () {
                            document.execCommand("copy");
                        }
                    }),
                    paste = new gui.MenuItem({
                        label: pasteLabel || "Paste",
                        click: function () {
                            document.execCommand("paste");
                        }
                    });

                menu.append(cut);
                menu.append(copy);
                menu.append(paste);

                return menu;
            }

            var translate = $filter('translate');
            var menu = new Menu(translate('CUT'), translate('COPY'), translate('PASTE'));

            $document.on("contextmenu", function (e) {
                if(e.target.tagName === 'CANVAS'){
                    return;
                }
                e.preventDefault();
                menu.popup(e.x, e.y);
            });

        });

})();

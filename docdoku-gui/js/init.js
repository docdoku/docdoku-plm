if (typeof(window.require) !== "undefined") {
    window.requireNode = window.require;
    window.gui = window.requireNode('nw.gui');
    window.events = require('events');
    window.eventEmitter = new events.EventEmitter();
    window.exec = window.requireNode('child_process').exec;
    window.fs = window.requireNode('fs');
    window.moment = window.requireNode('moment');
    window.os = window.requireNode('os');
    window.path = window.requireNode('path');
    window.util = window.requireNode('util');
    window.wrench = window.requireNode('wrench');
    window.require = undefined;
}
if(gui.App.argv[0] == "devtools"){
    gui.Window.get().showDevTools();
}

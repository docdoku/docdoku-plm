var requireNode = window.requireNode;

var gui = requireNode("nw.gui");

var menu = new gui.Menu({ 'type': 'menubar' });

var fileMenu = new gui.MenuItem({ label: 'File' });
var helpMenu = new gui.MenuItem({ label: 'Help' });

var fileSub = new gui.Menu();
var helpSub= new gui.Menu();

fileSub.append(new gui.MenuItem({ label: 'Configuration', click:function(){
    require(["views/configuration"],function(ConfigView){
        var configView = new ConfigView();
        $("body").append(configView.render().el);
        configView.openModal();
    });
}}));

fileSub.append(new gui.MenuItem({ type: 'separator' }));
fileSub.append(new gui.MenuItem({ label: 'Exit' , click:function(){
    gui.App.closeAllWindows();
    gui.App.quit();
}}));

helpSub.append(new gui.MenuItem({ label: 'Docdoku PLM', click:function(){
    window.open("https://github.com/docdoku/docdoku-plm/wiki");
}}));
helpSub.append(new gui.MenuItem({ label: 'About ...', click:function(){
    alert("Docdoku dplm v1.0");
}}));

gui.Window.get().menu = menu;
menu.append(fileMenu);
menu.append(helpMenu);
helpMenu.submenu = helpSub;
fileMenu.submenu = fileSub;

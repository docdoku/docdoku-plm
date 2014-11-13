define(function(){

    var exec = window.requireNode('child_process').exec;

    var WindowsCMD = {
        explore:function(path){
            return 'explorer "'+path+'"';
        }
    };

    var LinuxCMD = {
        explore:function(path){
            return 'nautilus "' + path + '"';
        }
    };

    var OsxCMD = {
        explore:function(path){
            return 'open "' + path + '"';
        }
    };

    var shell = {
        openPath:function(path){
            var c = shell.explore(path);
            exec(c);
        }
    };

    switch(os.type()){
        case "Windows_NT" : _.extend(shell,WindowsCMD); break;
        case "Linux" : _.extend(shell,LinuxCMD); break;
        case "Darwin" : _.extend(shell,OsxCMD); break;
        default :_.extend(shell,LinuxCMD); break;
    }

    return shell;
});
function switchMenu(obj, btn, context, path) {
    var el = document.getElementById(obj);
                
    if ( el.style.display != 'none' ) {
        el.style.display = 'none';
        document.getElementById(btn).src=context+'/images/plus.png';
    }
    else {
        el.style.display = 'inline';
        document.getElementById(btn).src=context+'/images/minus.png';
    }
}

function openFullScreen(strUrl, strWindowName) {
    window.open(strUrl, strWindowName, "");
}

/*function checkExtention(verifExt,path) {
    var extension=path.substring(path.lastIndexOf(".")+1, path.length);

    if (extension==verifExt)
        return true;
    else
        return false;
}

function changeExtention(path) {
    var name=path.split(".");
    var file=name[0];
    var extension=name[1];
    var newExt=name[0] + '.ogg';

    return (newExt);
}*/
function switchMenu(obj, btn, context) {
    var el = document.getElementById(obj);
                
    if ( el.style.display != 'none' ) {
        el.style.display = 'none';
        document.getElementById(btn).src=context+'/images/plus.png';
    }
    else {
        el.style.display = 'block';
        document.getElementById(btn).src=context+'/images/minus.png';
    }
}

function openFullScreen(strUrl, strWindowName) {
    window.open(strUrl, strWindowName, "");
}

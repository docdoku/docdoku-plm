$.fn.authorPopover = function (author,context,placement) {
    
    var timestamp = (new Date()).getTime();
    
    var $content = "<div id='popover_"+timestamp+"'><span class=\"btn author-reach-action webRTC_invite\"> Video <i class=\"icon-facetime-video\"></i></span> "
        +"<span class=\"btn author-reach-action chat_invite\">Chat</span> "
        +"<span class=\"btn author-reach-action mailto\">Mail</span></div>";
    
    var popoverLink = $(this).popover({
        title : author,
        context : context,
        html:true,
        content:$content,
        trigger:"manual",
        placement:placement
    }).click(function(e){    
        $(this).popover('toggle');
        e.stopPropagation();
        e.preventDefault();
        return false;
    });    
   
    $("#popover_"+timestamp).live('click',function(e){
        console.log(e);
        console.log(popoverLink);
        
        if (e.srcElement.className == "btn author-reach-action webRTC_invite" || e.srcElement.className == "icon-facetime-video")
        {
            videoCall(author);
        }
        else if (e.srcElement.className == "btn author-reach-action chat_invite") 
        {
            
        }
        else if (e.srcElement.className == "btn author-reach-action mailto")
        {
            //alert (author);
            window.open("mailto:nicolas.ruault@docdoku.com");
        }

        popoverLink.popover("hide");
    });  
    
    return popoverLink;
}

function videoCall(author) {
    
    var webRtcModal = $("#webRtcModal");
    var webRtcModalBody = webRtcModal.find(".modal-body");
    var webRtcModalTitle = webRtcModal.find("h3");


    webRtcModal.one('shown', function() {
        webRtcModalBody.html("<iframe src=\""+ getWebRtcUrlRoom(author) +"\" />");
        webRtcModalTitle.text("Call to " + author);
    });
    webRtcModal.one('hidden', function() {
        webRtcModalBody.empty();
    });
      
    webRtcModal.modal('show');
}

function getWebRtcUrlRoom (author) {
    var getIntFromString = function(str) {
        var count = 0;
        for (var i = 0 ; i < str.length ; i++){
            count += str.charCodeAt(i)*60*i;
        }
        return count;
    }
    return "/webRTCRoom?r=" + getIntFromString(author);
}

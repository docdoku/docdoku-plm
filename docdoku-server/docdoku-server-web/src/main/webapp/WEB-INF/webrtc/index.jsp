<!-- BEGIN: main -->
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<link rel="canonical" href="${roomLink}"/>
<meta http-equiv="X-UA-Compatible" content="chrome=1"/>

<style type="text/css">
#local video{
    z-index: 10;
    bottom: 0px;
    position: absolute;
    right: 0px;
    height: 30%;
    width: 20%;
    opacity: 1;
}

    #remote{
        height: 100%;
        width: 100%;
        background-color: #CCC;
        position: absolute;
        top: 0px;
        z-index: 9;
    }
</style>

</head>
<body>
<script type="text/javascript">
  var localVideo;
  var miniVideo;
  var remoteVideo;
  var localStream;
  var channel;
  var channelReady = false;
  var pc;
  var socket;
  var initiator = ${initiator};
  var started = false;

  function initialize() {
    console.log("Initializing; room=${roomKey}.");
    card = document.getElementById("card");
    localVideo = document.getElementById("localVideo");
    miniVideo = document.getElementById("miniVideo");
    remoteVideo = document.getElementById("remoteVideo");
    resetStatus();
    openChannel();
    getUserMedia();
  }

  function openChannel() {
    console.log("Opening channel.");
	var location = "ws://${serverName}:8088/webRTCSocket";
    channel = new WebSocket(location);
	channel.onopen    = onChannelOpened;
	channel.onmessage = onChannelMessage;
	channel.onclose   = onChannelClosed;
	channel.onerror   = onChannelError;
  }

  function resetStatus() {
    if (!initiator) {
      setStatus("Waiting for someone to join: <a href=\"${roomLink}\">${roomLink}</a>");
    } else {
      setStatus("Initializing...");
    }
  }

  function getUserMedia() {
    try {
      navigator.webkitGetUserMedia({audio:true, video:true}, onUserMediaSuccess, onUserMediaError);
      console.log("Requested access to local media with new syntax.");
    } catch (e) {
      try {
        navigator.webkitGetUserMedia("video,audio", onUserMediaSuccess, onUserMediaError);
        console.log("Requested access to local media with old syntax.");
      } catch (e) {
        alert("webkitGetUserMedia() failed. Is the MediaStream flag enabled in about:flags?");
        console.log("webkitGetUserMedia failed with exception: " + e.message);
      }
    }
  }

  function createPeerConnection() {
    try {
      pc = new webkitPeerConnection00("${pcConfig}", onIceCandidate);
      console.log("Created webkitPeerConnnection00 with config \"${pcConfig}\".");
    } catch (e) {
      console.log("Failed to create PeerConnection, exception: " + e.message);
      alert("Cannot create PeerConnection object; Is the 'PeerConnection' flag enabled in about:flags?");
      return;
    }

    pc.onconnecting = onSessionConnecting;
    pc.onopen = onSessionOpened;
    pc.onaddstream = onRemoteStreamAdded;
    pc.onremovestream = onRemoteStreamRemoved;
  }

  function maybeStart() {
    if (!started && localStream && channelReady) {
      setStatus("Connecting...");
      console.log("Creating PeerConnection.");
      createPeerConnection();
      console.log("Adding local stream.");
      pc.addStream(localStream);
      started = true;
      // Caller initiates offer to peer.
      if (initiator)
        doCall();
    }
  }

  function setStatus(state) {
    footer.innerHTML = state;
  }

  function doCall() {
    console.log("Send offer to peer");
    var offer = pc.createOffer({audio:true, video:true});
    pc.setLocalDescription(pc.SDP_OFFER, offer);
    sendMessage({type: 'offer', sdp: offer.toSdp()});
    pc.startIce();
  }

  function doAnswer() {
    console.log("Send answer to peer");
    var offer = pc.remoteDescription;
    var answer = pc.createAnswer(offer.toSdp(), {audio:true,video:true});
    pc.setLocalDescription(pc.SDP_ANSWER, answer);
    sendMessage({type: 'answer', sdp: answer.toSdp()});
    pc.startIce();
  }

  function sendMessage(message) {
    var msgString = JSON.stringify(message);
    console.log('C->S: ' + msgString);
    path = 'http://${serverName}:${serverPort}/${PATH}webRTCMessage?r=${roomKey}' + '&u=${me}';
    var xhr = new XMLHttpRequest();
    xhr.open('POST', path, true);
    xhr.send(msgString);
  }

  function processSignalingMessage(message) {
	console.log("Processing signaling message: " + message);
    var msg = JSON.parse(message);

    if (msg.type === 'offer') {
      // Callee creates PeerConnection
      if (!initiator && !started)
        maybeStart();

      pc.setRemoteDescription(pc.SDP_OFFER, new SessionDescription(msg.sdp));
      doAnswer();
    } else if (msg.type === 'answer' && started) {
      pc.setRemoteDescription(pc.SDP_ANSWER, new SessionDescription(msg.sdp));
    } else if (msg.type === 'candidate' && started) {
      var candidate = new IceCandidate(msg.label, msg.candidate);
      pc.processIceMessage(candidate);
    } else if (msg.type === 'bye' && started) {
      onRemoteHangup();
    }
  }

  function onChannelOpened() {
    console.log('Channel opened for token:${token}');
	channel.send('token:${token}');
    channelReady = true;
    if (initiator) maybeStart();
  }
  function onChannelMessage(message) {
    console.log('S->C: ' + message.data);
    processSignalingMessage(message.data);
  }
  function onChannelError() {
    console.log('Channel error for token: ${token}');
  }
  function onChannelClosed() {
    console.log('Channel closed for token: ${token}');
    alert('Channel closed for user '+(initiator+1)+' with token ${token}.');
	channel = null;
  }

  function onUserMediaSuccess(stream) {
    console.log("User has granted access to local media.");
    var url = webkitURL.createObjectURL(stream);
    localVideo.style.opacity = 1;
    localVideo.src = url;
    localStream = stream;
    // Caller creates PeerConnection.
    if (initiator) maybeStart();
  }
  function onUserMediaError(error) {
    console.log("Failed to get access to local media. Error code was " + error.code);
    alert("Failed to get access to local media. Error code was " + error.code + ".");
  }

  function onIceCandidate(candidate, moreToFollow) {
    if (candidate) {
        sendMessage({type: 'candidate',
                     label: candidate.label, candidate: candidate.toSdp()});
    }

    if (!moreToFollow) {
      console.log("End of candidates.");
    }
  }

  function onSessionConnecting(message) {
    console.log("Session connecting.");
  }
  function onSessionOpened(message) {
    console.log("Session opened.");
  }

  function onRemoteStreamAdded(event) {
    console.log("Remote stream added.");
    var url = webkitURL.createObjectURL(event.stream);
    miniVideo.src = localVideo.src;
    remoteVideo.src = url;
    waitForRemoteVideo();  
  }
  function onRemoteStreamRemoved(event) {
    console.log("Remote stream removed.");
  }

  function onHangup() {
    console.log("Hanging up.");
    started = false;    // Stop processing any message
    transitionToDone();
    pc.close();
    // will trigger BYE from server
    socket.close();
    pc = null;
    //socket = null;
  }
   
  function onRemoteHangup() {
    console.log('Session terminated.');
    started = false;    // Stop processing any message
    transitionToWaiting();
    pc.close();
    pc = null;
    initiator = 0;
  }

  function waitForRemoteVideo() {
	console.log("Waiting for remote video.");
    if (remoteVideo.currentTime > 0) {
      transitionToActive();
    } else {
      setTimeout(waitForRemoteVideo, 100);
    }
  }
  function transitionToActive() {
	console.log("Video conference transiting to active state.");
    remoteVideo.style.opacity = 1;
    card.style.webkitTransform = "rotateY(180deg)";
    setTimeout(function() { localVideo.src = ""; }, 500);
    setTimeout(function() { miniVideo.style.opacity = 1; }, 1000);
    setStatus("<input type=\"button\" id=\"hangup\" value=\"Hang up\" onclick=\"onHangup()\" />");
  }
  function transitionToWaiting() {
	console.log("Video conference transiting to waiting state.");
    card.style.webkitTransform = "rotateY(0deg)";
    setTimeout(function() { localVideo.src = miniVideo.src; miniVideo.src = ""; remoteVideo.src = "" }, 500);
    miniVideo.style.opacity = 0;
    remoteVideo.style.opacity = 0;
    resetStatus();
  }
  function transitionToDone() {
	console.log("Video conference transiting to done state.");
    localVideo.style.opacity = 0;
    remoteVideo.style.opacity = 0;
    miniVideo.style.opacity = 0;
    setStatus("You have left the call. <a href=\"${roomLink}\">Click here</a> to rejoin.");
  }
  function enterFullScreen() {
	console.log("Entering full screen mode.");
    remote.webkitRequestFullScreen();
  }

  if (!window.WebSocket)
	alert("WebSocket not supported by this browser");
/*	
  window.onbeforeunload = function() {
    channel.onclose = function () {}; // disable onclose handler first
    channel.close()
  };
*/ 
  setTimeout(initialize, 1);
</script>
<div id="container" ondblclick="enterFullScreen()"> 
  <div id="card">
    <div id="local">
      <video width="100%" height="100%" id="localVideo" autoplay="autoplay"/>
    </div>
    <div id="remote">
      <video width="100%" height="100%" id="remoteVideo" autoplay="autoplay">
      </video>
      <div id="mini">
        <video width="100%" height="100%" id="miniVideo" autoplay="autoplay" />
      </div>
    </div>
  </div>
  <div id="footer">
  </div>
</div>

</body>
</html>

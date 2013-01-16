define(function(){

    WebRTCModuleView = Backbone.View.extend({

        el: "#webrtc_module_modal",

        initialize: function(){

            this.localStream = undefined ;

            _.bindAll(this);
        },

        //user initiate the call
        onNewWebRTCSession:function(remoteUser){

            mainChannel.sendJSON({
                type: ChannelMessagesType.WEBRTC_INVITE,
                callee: remoteUser,
                context:"empty"
            });
            this.runCall(remoteUser);
        },

        runCall : function(remoteUser){

            this.initMedia();

            var that = this ;

            this.$el.one('shown', function () {
                that.$("h3").text("Call to ");
            });

            this.$el.one('hidden', function () {
                that.$('#webrtc_local_video').attr("src","");
                that.$('#webrtc_remote_video').attr("src","");
            });

            this.$el.modal('show');
        },

        render: function() {
            return this;
        },


        initMedia : function(){

            try {
                // new Method
                navigator.webkitGetUserMedia({audio:true, video:true}, this.onUserMediaSuccess, this.onUserMediaError);
            } catch (e) {
                try {
                    // old Method
                    navigator.webkitGetUserMedia("video,audio", this.onUserMediaSuccess, this.onUserMediaError);
                } catch (e) {
                    alert("webkitGetUserMedia() failed. Is the MediaStream flag enabled in about:flags?");
                    console.log("webkitGetUserMedia failed with exception: " + e.message);
                }
            }

        },

        onUserMediaSuccess: function(stream){

            // set the source of local video
            var url = webkitURL.createObjectURL(stream);

            this.$('#webrtc_local_video').attr("src",url);

            // TODO : move this on remote video receive
            this.$('#webrtc_remote_video').attr("src",url);

            // keep the stream in memory
            this.localStream = stream;

        },

        onUserMediaError : function(error){
            this.exit();
        },

        onAcceptWebRTCInvite : function(message){
            console.log("User has accepted.");
            this.runCall(message.remoteUser);
        },

        onRejectWebRTCInvite : function(message){
            console.log("User has rejected.");
        },

        onWebRTCStatusChanged : function(status){
            console.log("onWebRTCStatusChanged."+status);
        },

        exit : function(){
            this.$el.modal('hide');
        }


    });

    return WebRTCModuleView;

});
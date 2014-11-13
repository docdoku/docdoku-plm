define(["views/directory"],function(DirectoryView){

    var DirectoryChooserView = Backbone.View.extend({

        render:function(){

            var self = this ;

            this.proxyEvents = {};

            _.extend(this.proxyEvents, Backbone.Events);

            this.proxyEvents.on("directory:chosen",function(view){
                self.trigger("directory:chosen",view.path);
            });

            if(os.type() == "Windows_NT") {
                this.dosInit();
            }else{
                this.unixInit();
            }

            return this;
        },

        unixInit:function(){
            var root = "/";
            var dv = new DirectoryView().setPath(root,root).render();
            dv.setSignalsProxy(this.proxyEvents);
            this.$el.append(dv.$el);
        },

        dosInit:function(){
            var self = this;
            exec(window.process.cwd() + '\\dplm\\windows-drives.bat', function (error, stdout, stderr) {
                if (error || stderr) {
                    console.log("Cannot parse drives");
                } else {
                    var drives = stdout.toString().split('\n');
                    _.each(drives,function(_drive){
                        var drive = _drive.trim();
                        if(drive){
                            var dv = new DirectoryView().setPath(drive+"/",drive).render();
                            dv.setSignalsProxy(self.proxyEvents);
                            self.$el.append(dv.$el);
                        }
                    })
                }
            });
        }

    });

    return DirectoryChooserView;

});
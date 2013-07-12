define(["views/directory_view"],function(DirectoryView){

    var DirectoryChooserView = Backbone.View.extend({

        render:function(){

            var self = this ;

            var proxyEvents = {};

            _.extend(proxyEvents, Backbone.Events);

            proxyEvents.on("directory:chosen",function(view){
                self.trigger("directory:chosen",view.path);
            });

            var root = "/";

            if(os.type() == "Windows_NT") {
                root = "C:/";
            }


            var dv = new DirectoryView().setPath(root,root).render();
            dv.setSignalsProxy(proxyEvents);

            this.$el.append(dv.$el);

            return this;
        }

    });

    return DirectoryChooserView;

});
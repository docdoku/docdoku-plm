define(["text!templates/directory.html"],function(template){

    var DirectoryView = Backbone.View.extend({

        tagName:'ul',

        className:"directory",

        template: Handlebars.compile(template),

        events:{
            "click span:first":"toggle"
        },

        setPath:function(path, name){
            this.path = path;
            this.name = name;
            return this ;
        },

        setSignalsProxy:function(signalsProxy){
            this.signalsProxy = signalsProxy;
        },

        render:function(){
            this.opened = false;
            this.$el.html(this.template({name:this.name}));
            this.$i = this.$("i:first");
            this.$subviews = this.$(".subviews:first");
            return this;
        },

        dive:function(){
            var root = this.path;
            var self = this;
            this.opened = true;
            fs.readdir(root, function(err, list) {
                _.each(list,function(f){
                    fs.stat(path.join(root, f), function(err, stat) {
                        if(stat && stat.isDirectory() && f[0] != "."){
                            var dv = new DirectoryView().setPath(path.join(root, f), f).render();
                            dv.setSignalsProxy(self.signalsProxy);
                            self.$subviews.append(dv.$el);
                        }
                    });
                });
            });
            this.dived = true;
        },

        toggle:function(e){
            if(!this.opened){
                if(this.dived){
                    this.$subviews.show();
                    this.opened = true;
                }else{
                    this.dive();
                }
            }else{
                this.close();
            }
            this.toggleIcon();
            this.signalsProxy.trigger("directory:chosen",this);
        },

        toggleIcon:function(){
            this.$i.toggleClass("icon-folder-close icon-folder-open");
        },

        close:function(){
            this.$subviews.hide();
            this.opened=false;
        }

    });

    return DirectoryView;

});
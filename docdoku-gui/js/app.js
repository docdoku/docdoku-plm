// Global vars
var APP_GLOBAL = {
    // Application Router
    ROUTER:{},
    // Application signals
    SIGNALS:{},
    // Sync between local storage and memory
    GLOBAL_CONF:{},
    // Current path settings (exec dir at startup)
    CURRENT_PATH:window.process.cwd(),
    // Current workspace id
    CURRENT_WORKSPACE:""
};

var OS_SLASH = os.type() == "Windows_NT" ? "\\":"/";

define(["text!templates/main.html", "views/menu_view", "views/content_view", "storage"], function (MainView, MenuView, ContentView, Storage) {

    var AppView = Backbone.View.extend({

        template:Handlebars.compile(MainView),

        render:function() {
            this.$el.html(this.template({}));
            this.checkConfAtStartup();
            return this;
        },

        reset:function(){
            this.render();
        },

        checkConfAtStartup:function(){
            var self = this ;

            if(Storage.needsGlobalConf()){
                require(["views/configuration_view"],function(ConfigurationView){
                    var configView = new ConfigurationView();
                    $("body").append(configView.render().el);
                    configView.openModal();
                    configView.on("configuration:done",self.checkConfAtStartup);
                });
            }else{
                APP_GLOBAL.GLOBAL_CONF = Storage.getGlobalConf();
                new MenuView({el: "#menu"}).render();
                this.contentView = new ContentView({el: "#content"}).render();
            }
        },

        showWorkspace:function(workspace){
            var self = this;
            require(["views/workspace_view"],function(WorkspaceView){
                new WorkspaceView().setWorkspace(workspace).render();
                self.contentView.breadcrumbWorkspace(workspace);
            });
        },

        showPath:function(path){
            var self = this;
            require(["views/local_dir_view"],function(LocalRepoView){
                new LocalRepoView().setPath(path).render();
                self.contentView.breadcrumbPath(path);
            });
        }
    });

    $(function() {

        _.extend(APP_GLOBAL.SIGNALS, Backbone.Events);

        var AppRouter = Backbone.Router.extend({
            routes: {
                "path/*path":	"path",
                "workspace/*workspace":	"workspace"
            }
        });

        APP_GLOBAL.ROUTER = new AppRouter();
        Backbone.history.start();

        Handlebars.registerHelper('folderName', function(context, block) {
            var lastSlash = context.lastIndexOf(OS_SLASH);
            return context.substr(lastSlash+1,context.length);
        });


        var app = new AppView({el:"body"}).render();

        APP_GLOBAL.ROUTER.on("route:workspace",function(workspaceId){
            app.showWorkspace(workspaceId);
        });

        APP_GLOBAL.ROUTER.on("route:path",function(path){
            app.showPath(path);
        });

        APP_GLOBAL.SIGNALS.on("configuration:changed",function(){
            app.reset();
        });

    });
});


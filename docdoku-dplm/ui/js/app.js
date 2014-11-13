/*global $,APP_GLOBAL,Handlebars,OS_SLASH*/
require(['text!templates/main.html', 'views/menu', 'views/content', 'views/home', 'views/nav','views/configuration','storage'], function (MainView, MenuView, ContentView, HomeView, NavView,ConfigurationView, Storage) {
	'use strict';
    $(function () {

        var AppView = Backbone.View.extend({

            template: Handlebars.compile(MainView),

            render: function () {
                this.$el.html(this.template());
                this.checkConfAtStartup();
                return this;
            },

            reset: function () {
                this.render();
            },

            error:function(){
                this.menuView.onConfigurationError();
                this.contentView.onConfigurationError();
                this.navView.onConfigurationError();
                this.homeView.onConfigurationError();
            },

            checkConfAtStartup: function () {

                if (Storage.needsGlobalConf()) {
                    var configView = new ConfigurationView();
                    $('body').append(configView.render().el);
                    configView.openModal();
                } else {
                    APP_GLOBAL.GLOBAL_CONF = Storage.getGlobalConf();
                    this.menuView = new MenuView({el: '#menu'}).render();
                    this.navView = new NavView().render();
                    this.contentView = new ContentView({el: '#content'}).render();
                    this.homeView = new HomeView().render();
                }
            },

            showWorkspace: function (workspace) {
                var self = this;
                Storage.addRecentlyUsedWorkspace(workspace);
                require(['views/workspace'], function (WorkspaceView) {
                    new WorkspaceView().setWorkspace(workspace).render();
                    self.navView.workspace(workspace);
                });
            },

            showPath: function (path) {
                var self = this;
                Storage.addRecentlyUsedPath(path);
                require(['views/local_dir'], function (LocalRepoView) {
                    new LocalRepoView().setPath(path).render();
                    self.navView.path(path);
                });
            }
        });

        _.extend(APP_GLOBAL.SIGNALS, Backbone.Events);

        var AppRouter = Backbone.Router.extend({
            routes: {
                '/':'home',
                'home': 'home',
                'path/*path': 'path',
                'workspace/*workspace': 'workspace'
            }
        });

        Handlebars.registerHelper('folderName', function(context) {
            var lastSlash = context.lastIndexOf(OS_SLASH);
            return context.substr(lastSlash+1,context.length);
        });

        APP_GLOBAL.ROUTER = new AppRouter();
        Backbone.history.start();

        var app = new AppView({el: 'body'}).render();

        APP_GLOBAL.ROUTER.on('route:workspace', function (workspaceId) {
            app.showWorkspace(workspaceId);
        });

        APP_GLOBAL.ROUTER.on('route:path', function (path) {
            app.showPath(path);
            process.chdir(path);
        });

        APP_GLOBAL.ROUTER.on('route:home', function () {
            app.reset();
        });

        APP_GLOBAL.SIGNALS.on('configuration:changed', function () {
            app.reset();
        });

        APP_GLOBAL.SIGNALS.on('configuration:error', function () {
            app.error();
        });

        APP_GLOBAL.SIGNALS.on('path:changed', function (path) {
            process.chdir(path);
        });

    });
});


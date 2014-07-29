define(["models/component_module", "views/component_views"], function (ComponentModule, ComponentViews) {

    var PartsTreeView = Backbone.View.extend({

        el:'#product_nav_list',

        events: {
            "change input": "checkChildrenInputs",
            "change li": "checkParentsInputs",
            "component_selected a": "onComponentSelected",
            "click #product_title":"onProductRootNode"
        },

        setSelectedComponent: function(component) {
            this.componentSelected = component;
        },

        onProductRootNode:function(){
            this.setSelectedComponent(this.rootComponent);
            this.trigger("component_selected", true);
        },

        render: function() {

            var self = this ;

            var rootCollection = new ComponentModule.Collection([], { isRoot: true });

            this.smartPath = [];

            this.rootComponent = undefined;

            this.listenTo(rootCollection, 'reset', function(collection) {
                //the default selected component is the root
                self.rootComponent = collection.first();
                self.setSelectedComponent(self.rootComponent);
            });

            this.componentViews = new ComponentViews.Components({
                collection: rootCollection,
                resultPathCollection: this.options.resultPathCollection,
                parentView: this.$el,
                parentChecked: false
            });

            return this;
        },

        getSmartPath: function() {
            return this.smartPath;
        },

        checkChildrenInputs: function(event) {
            var inputs = event.target.parentNode.querySelectorAll('input');
            for (var i = 0; i < inputs.length; i++) {
                inputs[i].checked = event.target.checked;
                // on retire les fils du smartPath
                this.removeFromSmartPath(inputs[i].id.substring(5));
            }
        },

        checkPath: function(path, checked) {
            var mainElement = this.el.querySelector('li#path_'+path);
            // if element is already load in the Tree
            if (mainElement) {
                mainElement.checked = checked;
                var inputs = mainElement.parentNode.querySelectorAll('input');
                for (var i = 0; i < inputs.length; i++) {
                    inputs[i].checked = checked;
                }
            }
        },

        expandToPath: function(path) {
            var depth = path.lastIndexOf("-");
            if (depth == -1){
                // first child, expand rooth (path = null)
                var root = this.el.querySelector('li#path_null div');
                $(root).click(); // .hitarea:first
                //setTimeout(function(){$(root).trigger("click")},500);
            } else {
                //extract parent path
                var parentPath = path.substring(0,depth);
                console.log(parentPath);
                var parentElement = this.el.querySelector('li#path_'+parentPath+' div');
                if (!parentElement) {
                    this.expandToPath(parentPath);
                }
                $(parentElement).click();//  .hitarea:first
            }
        },

        checkParentsInputs: function(event) {
            var relativeInput = event.currentTarget.querySelector('input');
            relativeInput.checked = event.target.checked;
            var childrenUl = event.currentTarget.querySelector('ul');
            // si c'est un noeud
            if (childrenUl != null) {
                var tempArray = [];
                var inputsChecked = 0;

                // on regarde regarde l'état des fils
                for (var i = 0; i < childrenUl.childNodes.length; i++) {
                    var li = childrenUl.childNodes[i];
                    if (li.querySelector('input').checked) {
                        inputsChecked++;
                        // on ajoute le fils dans un tableau temp
                        tempArray.push(li.id.substring(5));
                    }
                    // on retire les fils (on les ré-ajoutera après si le parent n'est pas coché)
                    this.removeFromSmartPath(li.id.substring(5));
                }
                // s'ils sont tous cochés
                if (inputsChecked == childrenUl.childNodes.length) {
                    // on essaye d'ajouter le noeud
                    this.addToSmartPath(relativeInput.parentNode.id.substring(5));
                } else {
                    relativeInput.checked = false;
                    // on essaye de retirer le noeud et d'ajouter les fils cochés
                    this.removeFromSmartPath(relativeInput.parentNode.id.substring(5));
                    //if (App.collaborativeView.isMaster) {
                        this.smartPath = this.smartPath.concat(tempArray);
                    //}
                }
            } else {
                if (relativeInput.checked) {
                    // si on coche -> ajoute la feuille
                    this.addToSmartPath(relativeInput.parentNode.id.substring(5));
                } else {
                    // si on décoche -> on essayer de retirer la feuille
                    this.removeFromSmartPath(relativeInput.parentNode.id.substring(5));
                }
            }

            if (relativeInput.parentNode.id == "path_null"){
                if (App.collaborativeView.isMaster) {
                    mainChannel.sendJSON({
                        type: ChannelMessagesType.COLLABORATIVE_COMMANDS,
                        key: App.collaborativeView.roomKey,
                        messageBroadcast: {smartPath: this.smartPath},
                        remoteUser: "null"
                    });
                }
            }

        },

        addToSmartPath: function(p){
            //if (App.collaborativeView.isMaster) {
                this.removeFromSmartPath(p);
                this.smartPath.push(p);
            //}
        },

        removeFromSmartPath: function(p){
            //if (App.collaborativeView.isMaster) {
                this.smartPath = _.filter(this.smartPath, function (e) {
                    return e != p;
                });
            //}
        },

        compareSmartPath: function (arrayPaths) {

            var pathToUnload = _.difference(this.smartPath, arrayPaths);
            if (pathToUnload.length != 0) {
                console.log("path to unload : ");
                console.log(pathToUnload);
                App.instancesManager.loadQueue.push({"process":"unload","path":pathToUnload});
            }

            var pathToLoad = _.difference(arrayPaths, this.smartPath);
            if (pathToLoad.length != 0) {
                console.log("path to load : ");
                console.log(pathToLoad);
                App.instancesManager.loadQueue.push({"process":"load","path":pathToLoad});
            }
            this.smartPath = arrayPaths;
            this.setCheckboxes();
        },

        setCheckboxes: function() {
            this.$("li input").prop('checked',false);

            _.each(this.smartPath, function(path){
                this.$('li[id^="path_'+path+'"] input').prop('checked',true);
            });
        },

        onComponentSelected: function(e, componentModel, li) {
            e.stopPropagation();
            this.$("li.active").removeClass("active");
            li.addClass("active");
            this.setSelectedComponent(componentModel);
            this.trigger("component_selected");
        },

        refreshAll:function(){
            this.componentViews.fetchAll();
        },

        onRefreshComponent:function(partKey){
            this.componentViews.refreshComponent(partKey);
        }

    });

    return PartsTreeView;

});
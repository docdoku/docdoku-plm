define([
    "text!templates/workspace.html",
    "models/remote_versioned_file",
    "views/remote_versioned_file",
    "views/loader",
    "dplm"
    ],
    function(template, RemoteVersionedFileModel, RemoteVersionedFileView, Loader, Dplm) {

    var WorkspaceView = Backbone.View.extend({

        el:"div#subContent",

        template: Handlebars.compile(template),

        events: {
            "click #loadMore":"loadMore",
            "submit #searchForm":"search",
            "keyup #searchForm>input":"setAttributeValueOnInput"
        },

        initialize:function(){
            this.start = 0 ;
            this.max = 20;
        },

        setWorkspace:function(workspace){
            this.workspace = workspace;
            return this;
        },

        render:function() {

            this.$el.html(this.template({workspace:this.workspace}));
            this.$workspace = $("#workspace");
            this.$loadMore = $("#loadMore");
            this.$search = $("#searchForm > input");
            this.showLoader();

            var self = this;

            Dplm.getPartMastersCount(this.workspace,{
                success:function(data){
                    self.hideLoader();
                    self.$workspace.empty();
                    self.partMastersCount = data.count;
                    self.loadNextParts();
                },
                error:function(error){
                }
            });

        },

        showLoader:function(){
            if(!this.$loader){
                this.$loader = new Loader();
            }
            this.$workspace.append(this.$loader);
        },

        hideLoader:function(){
            this.$loader.remove();
        },

        loadNextParts:function(){
            var self = this ;
            this.showLoader();
            Dplm.getPartMasters(this.workspace, this.start, this.max, {
                success : function(partMasters) {
                    _.each(partMasters, function (partMaster) {
                        var remoteVersionedFileModel = new RemoteVersionedFileModel({
                            partNumber: partMaster.partNumber,
                            name : partMaster.cadFileName,
                            version : partMaster.version,
                            status: partMaster
                        });
                        var remoteVersionedFileView =  new RemoteVersionedFileView({model: remoteVersionedFileModel}).render();
                        self.$workspace.append(remoteVersionedFileView.$el);
                    });
                    self.hideLoader();
                    if(self.partMastersCount > self.start + self.max){
                        self.$loadMore.show();
                    }else{
                        self.$loadMore.hide();
                    }
                },
                error:function(error){
                }
            });
        },

        loadMore:function(){
            this.$loadMore.hide();
            this.start += this.max;
            this.loadNextParts();
        },

        reset:function(){
            this.$workspace.empty();
            this.start = 0;
            this.loadNextParts();
        },

        setAttributeValueOnInput:function(){
            this.$search.attr("value",this.$search.val());
        },

        search:function(e){
            var self = this ;
            this.$loadMore.hide();
            var searchVal = this.$search.val();
            if(searchVal){
                this.$workspace.html("<h5>Search results for : " + searchVal+ "</h5>");
                this.showLoader();
                Dplm.searchPartMasters(this.workspace,searchVal,{
                    success : function(partMasters){
                        _.each(partMasters, function (partMaster) {
                            var remoteVersionedFileModel = new RemoteVersionedFileModel({partNumber: partMaster.partNumber, name : partMaster.cadFileName, version : partMaster.version, status: partMaster});
                            var remoteVersionedFileView =  new RemoteVersionedFileView({model: remoteVersionedFileModel}).render();
                            self.$workspace.append(remoteVersionedFileView.$el);
                        });
                        self.hideLoader();
                    },
                    error:function(error){
                    }
                });
            }else{
                this.reset();
            }
            e.preventDefault();
            e.stopPropagation();
            return false;
        },

        doSearch:function(searchValue){

        }

    });

    return WorkspaceView;
});
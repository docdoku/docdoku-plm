define([
    "models/tag",
    "collections/tag",
	"common-objects/views/components/modal",
	"views/document/document_tag",
	"text!templates/document/documents_tags.html"
], function (
    Tag,
    TagList,
	ModalView,
    TagView,
	template
) {
	var DocumentsTagsView = ModalView.extend({

        template: Mustache.compile(template),
        templateExtraData : {},

        initialize: function() {
            this._collectionSize = _.size(this.collection);
            this.templateExtraData = {
                isSingleChecked: this._collectionSize == 1,
                isEmptyCollection: this._collectionSize == 0
            };

            ModalView.prototype.initialize.apply(this, arguments);
            this.events["submit #form-" + this.cid] = "onSubmitForm";
            this.events["click .newTag-button"] = "onNewTagButton";

            this._existingTagsCollection = new TagList();
            this._existingTagsViews = [];

            this._existingTagsCollection.fetch({                                                                        // Get all Tag of the Workspace
                reset:true,
                success: this.onExistingTagsFetched
            });

            this._currentTagsCollection = new TagList();
            this._currentTagsViews = [];

            this._tagsToCreate = new TagList();
            this._tagsToDeleteCollection = new TagList();
            this._tagsToAddCollection = new TagList();

            _.bindAll(this);

        },

        onExistingTagsFetched:function(){                                                                               // Action to do after All tags fetching
            this.renderExistingTags();
            if(_.size(this.collection) == 1){
                this.activeSingleDocumentChecked();
            }else{
                this.launchEventBind();
            }
        },

        launchEventBind : function(){                                                                                   // Initialize event binding
            this._currentTagsCollection.bind('add', this.onTagAdded, this);
            this._currentTagsCollection.bind('remove', this.onTagRemoved, this);
            this._existingTagsCollection.bind('add', this.onTagCreated, this);
            this._existingTagsCollection.bind('remove', this.onTagDelete, this);
        },

        activeSingleDocumentChecked : function(){                                                                       // Initialize context for single document tags management
            this._oldTagsCollection = new TagList();
            this._tagsToRemoveCollection = new TagList();

            var that=this;
            var singleModel = this.collection.first();
            singleModel.fetch({
                reset:false,
                success:function(){
                    var oldTags=singleModel.getTags();
                    if(!_.isEmpty(oldTags)){
                        that.createOldTags(oldTags);
                    }
                    that.launchEventBind();
                }
            });
        },

        renderExistingTags:function(){
            var $existingTabs = this.$(".existing-tags-list");
            $existingTabs.empty();
            var that=this;
            _.each(this._existingTagsCollection.models,function(model){
                that.addTagViewToExistingTagsList(model)
            });
        },

        createOldTags:function(tags){
            var that = this ;
            _.each(tags,function(tagLabel){
                _.each(that._existingTagsCollection.models,function(model){
                    if(model.id == tagLabel){
                        that._currentTagsCollection.push(model);
                        that._oldTagsCollection.push(model);
                        that.addTagViewToDocumentTagsList(model);
                        that.removeTagViewFromExistingTagsList(model);
                    }
                });
            })
        },

        onNewTagButton:function(){
            var $newTagInput = this.$(".newTag");
            var tagId = $newTagInput.val();
            if(tagId){
                var newModel = new Tag({label:tagId,id:tagId,workspaceId:APP_CONFIG.workspaceId});
                var modelAlreadyExists = false ;

                _.each(this._currentTagsCollection.models,function(model){
                    if(model.id == newModel.id){
                        modelAlreadyExists = true;
                    }
                });

                _.each(this._existingTagsCollection.models,function(model){
                    if(model.id == newModel.id){
                        modelAlreadyExists = true;
                    }
                });

                if(!modelAlreadyExists){
                    this._existingTagsCollection.push(newModel);
                    if(this._collectionSize!=0){
                        this._currentTagsCollection.push(newModel);
                    }
                    $newTagInput.val("");
                }
            }
        },

        onTagCreated:function(model){
            this._tagsToCreate.push(model);
            this.addTagViewToExistingTagsList(model);
        },
        onTagDelete: function(model){
            var isInCreateList = _.contains(this._tagsToCreate.models,model);
            if(isInCreateList){
                this._tagsToCreate.remove(model);
            }else{
                this._tagsToDeleteCollection.push(model);
            }
            this.removeTagViewFromExistingTagsList(model);
        },
        onTagAdded:function(model){
            var isInTagsToRemove = false ;
            var isInOldTag = false ;
            if(this._tagsToRemoveCollection){
                isInTagsToRemove = _.contains(this._tagsToRemoveCollection.models,model);
            }
            if(this._oldTagsCollection){
                isInOldTag = _.contains(this._oldTagsCollection.models,model);
            }
            if(isInTagsToRemove){
                this._tagsToRemoveCollection.remove(model);
            }
            if(!isInOldTag){
                this._tagsToAddCollection.push(model);
            }
            this.addTagViewToDocumentTagsList(model);
            this.removeTagViewFromExistingTagsList(model);
        },
        onTagRemoved:function(model){
            var isInOldTag = false ;
            var isInTagsToAdd = _.contains(this._tagsToAddCollection.models,model);
            if(this._oldTagsCollection){
                isInOldTag = _.contains(this._oldTagsCollection.models, model);
            }
            if(isInTagsToAdd){
                this._tagsToAddCollection.remove(model);
            }
            if(isInOldTag){
                this._tagsToRemoveCollection.push(model);
            }
            this.addTagViewToExistingTagsList(model);
            this.removeTagViewFromDocumentTagsList(model);
        },

        onSubmitForm: function() {
            var that = this ;

            this.createNewTags(function(){
                that.cleanDeleteTags(function(){
                    if(that._collectionSize!=0){
                        that.addTagsToDocuments();
                        that.removeTagsToDocument();
                    }
                });
            });

            this.hide();
            return false;
        },


        addTagViewToExistingTagsList : function(model){
            var $existingTabs = this.$(".existing-tags-list");
            var that = this ;
            var tagView =  null;

            if(this._collectionSize==0){
                tagView = new TagView({
                    model:model,
                    isAdded:false,
                    isAvailable:true,
                    clicked:function(){
                        that._currentTagsCollection.push(model);
                    },
                    cross_clicked:function(){
                        that._existingTagsCollection.remove(model);
                    }
                }).render();
            }else{
                tagView = new TagView({
                    model:model,
                    isAdded:false,
                    isAvailable:false,
                    clicked:function(){
                        that._currentTagsCollection.push(model);
                    }
                }).render();
            }



             $existingTabs.append($(tagView.el));
            this._existingTagsViews.push(tagView);
        },
        removeTagViewFromExistingTagsList : function(model){
            var viewToRemove = _(this._existingTagsViews).select(function(view) {
                return view.model === model;
            })[0];
            if(viewToRemove){
                $(viewToRemove.el).remove();
                this._existingTagsViews = _(this._existingTagsViews).without(viewToRemove);
            }
        },
        addTagViewToDocumentTagsList : function(model){
            var $tagsToAdd = this.$(".tags-to-add-list");
            var that = this ;
            var tagView =  new TagView({model:model, isAdded:true, clicked:function(){
                that._currentTagsCollection.remove(model);
            }}).render();
            var $tag = $(tagView.el);
            $tagsToAdd.append($tag);
            this._currentTagsViews.push(tagView);
        },
        removeTagViewFromDocumentTagsList : function(model){
            var viewToRemove = _(this._currentTagsViews).select(function(view) {
                return view.model === model;
            })[0];
            if(viewToRemove){
                $(viewToRemove.el).remove();
                this._currentTagsViews = _(this._currentTagsViews).without(viewToRemove);
            }
        },


        createNewTags: function (callbackSuccess) {

            if(this._tagsToCreate.length){
                var that = this ;
                this._existingTagsCollection.createTags(
                    that._tagsToCreate.models,
                    function(){
                        callbackSuccess();
                        Backbone.Events.trigger("refreshTagNavViewCollection");
                    }
                )
            }else{
                callbackSuccess();
            }
        },
        cleanDeleteTags: function (callbackSuccess){
            if(this._tagsToDeleteCollection.length){
                var baseUrl = "/api/workspaces/" + APP_CONFIG.workspaceId + "/tags/";
                var total = this._tagsToDeleteCollection.length;
                this._tagsToDeleteCollection.each(function(tag){
                    var count = 0;
                    $.ajax({
                        type: "DELETE",
                        url:baseUrl+tag.id,
                        success: function() {
                            count ++;
                            if(count >= total){
                                callbackSuccess();
                                Backbone.Events.trigger("refreshTagNavViewCollection");
                            }
                        }
                    });
                })
            }else{
                callbackSuccess();
            }
        },
        addTagsToDocuments: function () {
            var that = this ;
            if(this.collection.length){
                _.each(this.collection.models,function(document){
                    document.addTags(that._tagsToAddCollection);
                });
            }
        },
        removeTagsToDocument: function () {
            var that = this ;

            if(this.collection.length){
                _.each(this.collection.models,function(document){
                    if(that._tagsToRemoveCollection && that._tagsToRemoveCollection.length){
                        _.each(that._tagsToRemoveCollection.models,function(tag){
                            document.removeTag(tag.id,function(){});
                        });
                    }
                });
            }
        }

    });

    return DocumentsTagsView;

});
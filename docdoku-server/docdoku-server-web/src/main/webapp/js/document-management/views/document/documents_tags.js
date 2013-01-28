define([
    "models/tag",
    "collections/tag",
	"views/components/modal",
	"views/document_tag",
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

        initialize: function() {

            ModalView.prototype.initialize.apply(this, arguments);

            this.events["submit #form-" + this.cid] = "onSubmitForm";
            this.events["click .newTag-button"] = "onNewTagButton";

            this._existingTagsCollection = new TagList();
            this._existingTagsViews = [];

            this._existingTagsCollection.fetch({
                success: this.renderExistingTabs
            });

            this._tagsToAddCollection = new TagList();
            this._tagsToAddViews = [];

            this._tagsToAddCollection.bind('add', this.onTagAdded, this);
            this._tagsToAddCollection.bind('remove', this.onTagRemoved, this);
            _.bindAll(this);

        },

        renderExistingTabs:function(){

            var that = this ;

            var $existingTabs = this.$(".existing-tags-list");
            $existingTabs.empty();

            this._existingTagsCollection.each(function(model){

                var tagView =  new TagView({model:model, iconClass:"icon-plus", clicked:function(){
                    that.addExistingTag(model);
                }}).render();

                $existingTabs.append($(tagView.el));

                that._existingTagsViews.push(tagView);

            });

            this._existingTagsCollection.bind('add', this.onTagCreated, this);

        },

        onNewTagButton:function(){

            var $newTagInput = this.$(".newTag");

            var tagId = $newTagInput.val();

            if(tagId){

                var newModel = new Tag({label:tagId,id:tagId,workspaceId:APP_CONFIG.workspaceId});

                var modelAlreadyExists = false ;

                _.each(this._tagsToAddCollection.models,function(model){
                    modelAlreadyExists |= (model.id == newModel.id);
                });

                _.each(this._existingTagsCollection.models,function(model){
                    modelAlreadyExists |= (model.id == newModel.id);
                });

                if(!modelAlreadyExists){
                    this._tagsToAddCollection.push(newModel);
                    this._existingTagsCollection.push(newModel);
                    $newTagInput.val("");
                }

            }

        },

        onTagCreated:function(model){
          //  this._existingTagsCollection.createTag(model);
            // TODO : refresh nav tag list
        },

        addExistingTag:function(model){
            this._tagsToAddCollection.push(model);
        },

        onTagAdded:function(model){

            var that = this ;

            var $tagsToAdd = this.$(".tags-to-add-list");

            var tagView =  new TagView({model:model, iconClass:"icon-remove", clicked:function(){
                that._tagsToAddCollection.remove(model);
            }}).render();

            var $tag = $(tagView.el);

            $tagsToAdd.append($tag);

            this._tagsToAddViews.push(tagView);

        },

        onTagRemoved:function(model){

            var viewToRemove = _(this._tagsToAddViews).select(function(view) {
                return view.model === model;
            })[0];

            if(viewToRemove){
                $(viewToRemove.el).remove();
                this._tagsToAddViews = _(this._tagsToAddViews).without(viewToRemove);
            }

        },

        onSubmitForm: function() {

            var that = this ;

            _.each(this.collection,function(document){
               document.addTags(that._tagsToAddCollection);
            });

            this.hide();

            return false;
        }

    });

    return DocumentsTagsView;

});

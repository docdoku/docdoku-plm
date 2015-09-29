/*global $,_,define,App*/
define([
    'backbone',
    'common-objects/models/tag',
    'common-objects/collections/tag',
    'common-objects/views/components/modal',
    'common-objects/views/tags/tag',
    'text!common-objects/templates/tags/tags_management.html'
], function (Backbone,Tag, TagList, ModalView, TagView, template) {
	'use strict';
    var TagsManagementView = ModalView.extend({

        template: template,

        templateExtraData: {},

        initialize: function () {
            this._collectionSize = this.collection.size();                                                              // Get number of item check
            this.templateExtraData = {                                                                                  // Set template variable to know the number of item check
                isSingleChecked: this._collectionSize === 1,
                isEmptyCollection: this._collectionSize === 0
            };

            ModalView.prototype.initialize.apply(this, arguments);
            this.events['submit #form-' + this.cid] = 'onSubmitForm';
            this.events['click .newTag-button'] = 'onNewTagButton';
            this.events['input .newTag'] = 'onInputChange';
            this.$newTagButton = this.$('.newTag-button').hide();
            this.buttonEnabled = false;

            this._existingTagsCollection = new TagList();
            this._existingTagsCollection.fetch({                                                                        // Get all Tag of the Workspace
                reset: true,
                success: this.onExistingTagsFetched
            });
            this._existingTagsViews = [];

            this._currentTagsCollection = new TagList();
            this._currentTagsViews = [];

            this._tagsToCreate = [];
            this._tagsToDeleteCollection = [];
            this._tagsToAddCollection = [];

            _.bindAll(this);
            this.$el.on('remove', this.removeSubviews);                                                                 // Remove cascade
        },

        onInputChange: function(e) {
            //Show or hide only if it's not already hidden/shown
            if(!this.buttonEnabled && e.target.value) {
                this.$newTagButton.show();
                this.buttonEnabled = true;
            }
            if(this.buttonEnabled && !e.target.value) {
                this.$newTagButton.hide();
                this.buttonEnabled = false;
            }
        },

        removeSubviews: function () {                                                                                   // Remove all tagViews
            _(this._existingTagsViews).invoke('remove');
            _(this._currentTagsViews).invoke('remove');
            delete this._existingTagsViews;
            delete this._currentTagsViews;
        },

        onExistingTagsFetched: function () {                                                                            // Action to do after All tags fetching
            this.renderExistingTags();
            if (this._collectionSize === 1) {
                this.activeSingleElementChecked();
            } else {
                this.launchEventBind();
            }
        },

        launchEventBind: function () {                                                                                  // Initialize event binding
            this._currentTagsCollection.on('add', this.onTagAdded, this);
            this._currentTagsCollection.on('remove', this.onTagRemoved, this);
            this._existingTagsCollection.on('add', this.onTagCreated, this);
            this._existingTagsCollection.on('remove', this.onTagDelete, this);
        },

        activeSingleElementChecked: function () {                                                                       // Initialize context for single document tags management
            this._oldTagsCollection = [];
            this._tagsToRemoveCollection = [];

            var that = this;
            var singleModel = this.collection.first();
            singleModel.fetch({
                reset: false,
                success: function () {
                    var oldTags = singleModel.getTags();
                    if (!_.isEmpty(oldTags)) {
                        that.createPreviousTags(oldTags);
                    }
                    that.launchEventBind();
                }
            });
        },

        renderExistingTags: function () {
            var $existingTabs = this.$('.existing-tags-list');
            $existingTabs.empty();
            var that = this;
            _.each(this._existingTagsCollection.models, function (model) {
                that.addTagViewToExistingTagsList(model);
            });
        },

        createPreviousTags: function (tags) {
            var that = this;
            _.each(tags, function (tagLabel) {
                _.each(that._existingTagsCollection.models, function (model) {
                    if (model.id === tagLabel) {
                        that._currentTagsCollection.push(model);
                        that._oldTagsCollection.push(model);
                        that.addTagViewToResourceTagsList(model);
                        that.removeTagViewFromExistingTagsList(model);
                    }
                });
            });
        },

        onNewTagButton: function () {
            var $newTagInput = this.$('.newTag');
            var tagId = $newTagInput.val();
            if (tagId) {
                var newModel = new Tag({label: tagId, id: tagId, workspaceId: App.config.workspaceId});
                var modelAlreadyExists = false;

                _.each(this._currentTagsCollection.models, function (model) {
                    if (model.id === newModel.id) {
                        modelAlreadyExists = true;
                    }
                });

                _.each(this._existingTagsCollection.models, function (model) {
                    if (model.id === newModel.id) {
                        modelAlreadyExists = true;
                    }
                });

                if (!modelAlreadyExists) {
                    this._existingTagsCollection.push(newModel);
                    if (this._collectionSize !== 0) {
                        this._currentTagsCollection.push(newModel);
                    }
                    $newTagInput.val('').trigger('input');
                }
            }
        },

        onTagCreated: function (model) {
            this._tagsToCreate.push(model);
            this.addTagViewToExistingTagsList(model);
        },
        onTagDelete: function (model) {
            var isInCreateList = _.contains(this._tagsToCreate, model);
            if (isInCreateList) {
                this._tagsToCreate = _(this._tagsToCreate).without(model);
            } else {
                this._tagsToDeleteCollection.push(model);
            }
            this.removeTagViewFromExistingTagsList(model);
        },
        onTagAdded: function (model) {
            var isInTagsToRemove = false;
            var isInOldTag = false;
            if (this._tagsToRemoveCollection) {
                isInTagsToRemove = _.contains(this._tagsToRemoveCollection, model);
            }
            if (this._oldTagsCollection) {
                isInOldTag = _.contains(this._oldTagsCollection, model);
            }
            if (isInTagsToRemove) {
                this._tagsToRemoveCollection = this._tagsToRemoveCollection.without(model);
            }
            if (!isInOldTag) {
                this._tagsToAddCollection.push(model);
            }
            this.addTagViewToResourceTagsList(model);
            this.removeTagViewFromExistingTagsList(model);
        },
        onTagRemoved: function (model) {
            var isInOldTag = false;
            var isInTagsToAdd = _.contains(this._tagsToAddCollection, model);
            var isInTagsToCreate = _.contains(this._tagsToCreate, model);
            if (this._oldTagsCollection) {
                isInOldTag = _.contains(this._oldTagsCollection, model);
            }
            if (isInTagsToAdd) {
                this._tagsToAddCollection = _.without(this._tagsToAddCollection, model);
            }
            if (isInOldTag) {
                this._tagsToRemoveCollection.push(model);
            }
            if (isInTagsToCreate) {                                                                                     // It's a new tag that you remove from resource
                this.addTagViewToExistingTagsList(model, true);
            } else {
                this.addTagViewToExistingTagsList(model);
            }
            this.removeTagViewFromResourceTagsList(model);
        },

        onSubmitForm: function () {
            var that = this;

            this.createNewTags(function () {
                that.clearDeleteTags(function () {
                    if (that._collectionSize !== 0) {
                        that.addTagsToResources();
                        that.removeTagsToResource();
                    }
                    that.hide();
                });
            });
            return false;
        },


        addTagViewToExistingTagsList: function (model, editmode) {
            var $existingTags = this.$('.existing-tags-list');
            var that = this;
            var tagView = null;

            if (this._collectionSize === 0){
	            tagView = new TagView({
		            model: model,
		            isAdded: false,
		            isAvailable: true,
		            clicked: function () {},
		            crossClicked: function () {
			            that._existingTagsCollection.remove(model);
		            }
	            }).render();
            } else if(editmode === true) {
                tagView = new TagView({
                    model: model,
                    isAdded: false,
                    isAvailable: true,
                    clicked: function () {
                        that._currentTagsCollection.push(model);
                    },
                    crossClicked: function () {
                        that._existingTagsCollection.remove(model);
                    }
                }).render();
            } else {
                tagView = new TagView({
                    model: model,
                    isAdded: false,
                    isAvailable: false,
                    clicked: function () {
                        that._currentTagsCollection.push(model);
                    }
                }).render();
            }

            $existingTags.append($(tagView.el));
            this._existingTagsViews.push(tagView);
        },
        removeTagViewFromExistingTagsList: function (model) {
            var viewToRemove = _(this._existingTagsViews).select(function (view) {
                return view.model === model;
            })[0];
            if (viewToRemove) {
                $(viewToRemove.el).remove();
                this._existingTagsViews = _(this._existingTagsViews).without(viewToRemove);
                viewToRemove.remove();
            }
        },
        addTagViewToResourceTagsList: function (model) {
            var $tagsToAdd = this.$('.tags-to-add-list');
            var that = this;
            var tagView = new TagView({model: model, isAdded: true, clicked: function () {
                that._currentTagsCollection.remove(model);
            }}).render();
            var $tag = $(tagView.el);
            $tagsToAdd.append($tag);
            this._currentTagsViews.push(tagView);
        },
        removeTagViewFromResourceTagsList: function (model) {
            var viewToRemove = _(this._currentTagsViews).select(function (view) {
                return view.model === model;
            })[0];
            if (viewToRemove) {
                $(viewToRemove.el).remove();
                this._currentTagsViews = _(this._currentTagsViews).without(viewToRemove);
                viewToRemove.remove();
            }
        },


        createNewTags: function (callbackSuccess) {
            if (this._tagsToCreate.length) {
                var that = this;
                this._existingTagsCollection.createTags(
                    that._tagsToCreate,
                    function () {
                        callbackSuccess();
                        Backbone.Events.trigger('refreshTagNavViewCollection');
                    },
                    function (error) {
                        that.alert({message: error.responseText, type: 'error'});
                    }
                );
            } else {
                callbackSuccess();
            }
        },
        clearDeleteTags: function (callbackSuccess) {
            if (this._tagsToDeleteCollection && this._tagsToDeleteCollection.length) {
                var baseUrl = App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/tags/';
                var total = this._tagsToDeleteCollection.length;
                var count = 0;
                _(this._tagsToDeleteCollection).each(function (tag) {
                    $.ajax({
                        type: 'DELETE',
                        url: baseUrl + tag.id,
                        success: function () {
                            count++;
                            if (count >= total) {
                                callbackSuccess();
                                Backbone.Events.trigger('refreshTagNavViewCollection');
                            }
                        }
                    });
                });
            } else {
                callbackSuccess();
            }
        },
        addTagsToResources: function () {
            var that = this;
            if (this.collection.length) {
                _.each(this.collection.models, function (model) {
                    model.addTags(that._tagsToAddCollection);
                });
            }
        },
        removeTagsToResource: function () {
            var that = this;

            if (this.collection.length) {
                _.each(this.collection.models, function (model) {
                    if (that._tagsToRemoveCollection && that._tagsToRemoveCollection.length) {
                        _.each(that._tagsToRemoveCollection, function (tag) {
                            model.removeTag(tag.id, function () {
                            });
                        });
                    }
                });
            }
        }

    });

    return TagsManagementView;
});

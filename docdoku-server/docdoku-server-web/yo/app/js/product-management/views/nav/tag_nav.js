/*global define,App*/
define([
    'backbone',
    'mustache',
    'common-objects/common/singleton_decorator',
    'text!templates/nav/tag_nav.html',
    'views/nav/tag_nav_item',
    'views/part/part_content',
    'common-objects/collections/tag',
    'collections/tag_part_collection'
], function (Backbone, Mustache, singletonDecorator, template, TagNavItemView, PartContentView, TagCollection, TagPartsCollection) {
	'use strict';
    var TagNavView = Backbone.View.extend({
        el: '#tag-nav',

        events:{
            'click .tag-toggle':'toggleCollapse'
        },

        collection : new TagCollection(),

        tag:null,

        tagPartCollection:new TagPartsCollection(),

        initialize: function () {
            var that = this;
            Backbone.Events.on('refreshTagNavViewCollection', function () {
                that.collection.fetch({reset: true});
            });
            this.collection.on('reset',this.onTagListReset.bind(this));
            this.render();
            this.$('.items').hide();
        },

        onTagListReset:function(){
            this.$('.items').empty();
            this.collection.each(this.addTagView.bind(this));
            if(this.tag){
                this.$('.items').find('.header[data-tag="'+this.tag+'"]').addClass('active');
            }
        },

        addTagView:function(tag){
            this.$('.items').append(new TagNavItemView({model:tag}).render().$el);
        },

        render: function () {
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n, workspaceId: App.config.workspaceId}));
            this.collection.fetch({reset: true});
        },

        showContent: function (tag) {
            this.tag = tag;
            this.$('.items').show();
            App.$productManagementMenu.find('.active').removeClass('active');
            this.$('.items').find('.header[data-tag="'+this.tag+'"]').addClass('active');

            if(this.partContentView){
                this.partContentView.destroy();
            }

            this.partContentView = new PartContentView();
            this.tagPartCollection.setTag(this.tag);
            this.partContentView.setCollection(this.tagPartCollection).render();
            App.$productManagementContent.html(this.partContentView.el);
        },

        toggleCollapse:function(){
            this.$('.items').toggle();
        }
    });

    TagNavView = singletonDecorator(TagNavView);
    return TagNavView;
});

/*global _,$,define,App*/
define([
    'backbone',
    'mustache',
    'text!common-objects/templates/part/used_by_view.html',
    'common-objects/views/part/used_by_group_list_view',
    'common-objects/views/part/used_by_list_item_part_view',
    'common-objects/models/part'
], function (Backbone, Mustache, template, UsedByGroupListView, UsedByListItemPartView, Part) {
    'use strict';
    var UsedByView = Backbone.View.extend({

        className: 'used-by-items-view',

        initialize:function(){
            this.linkedPart = this.options.linkedPart;
        },

        render:function(){

            var data = {
                i18n: App.config.i18n
            };

            this.$el.html(Mustache.render(template, data));

            this.bindDomElements();
            if(this.linkedPart){
                var self = this;
                $.ajax({
                    type:'GET',
                    url: App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/parts/' + this.linkedPart.getPartKey() + '/used-by-as-component',
                    contentType:'application/json',
                    success:function(parts){
                        _.each(parts, function(part){
                            var newPartModel = new Part(part);
                            var usedByView = new UsedByListItemPartView({
                                model: newPartModel
                            }).render();

                            self.$componenetOfUL.append(usedByView.$el);
                        });

                        if(parts.length === 0){
                            self.$('#title-used-by-is-component-of').hide();
                        }

                    },
                    error: function(){

                    }
                });

                $.ajax({
                    type:'GET',
                    url: App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/parts/' + this.linkedPart.getPartKey() + '/used-by-as-substitute',
                    contentType:'application/json',
                    success:function(parts){
                        _.each(parts, function(part){
                            var newPartModel = new Part(part);
                            var usedByView = new UsedByListItemPartView({
                                model: newPartModel
                            }).render();

                            self.$substituteOfUL.append(usedByView.$el);
                        });

                        if(parts.length === 0){
                            self.$('#title-used-by-is-substitute-of').hide();
                        }
                    },
                    error: function(){

                    }
                });
            }

            this.initUsedByGroup();

            return this;
        },

        bindDomElements: function () {
            this.$componenetOfUL = this.$('#used-by-is-component-of');
            this.$substituteOfUL = this.$('#used-by-is-substitute-of');
        },

        initUsedByGroup:function(){
            this.usedByGroupListView = new UsedByGroupListView({
                linkedPart: this.linkedPart
            }).render();

            /* Add the usedByGroupListView to the tab */
            this.$('#used-by-group-list-view').html(this.usedByGroupListView.el);
        }
    });

    return UsedByView;
});

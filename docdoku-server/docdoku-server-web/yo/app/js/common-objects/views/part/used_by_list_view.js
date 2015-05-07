/*global _,$,define,App*/
define([
    'backbone',
    'mustache',
    'common-objects/views/part/used_by_list_item_view',
    'text!common-objects/templates/part/used_by_list.html',
    'common-objects/views/part/used_by_list_item_part_view',
    'common-objects/models/part'
], function (Backbone, Mustache, UsedByListItemView, template, UsedByListItemPartView, Part) {
    'use strict';
    var UsedByListView = Backbone.View.extend({

        className: 'used-by-items-view',

        initialize: function () {
            _.bindAll(this);
            if(this.options.part){
                this.partModel = this.options.part;
            }
        },

        render: function () {
            var data = {
                configurationItemId: this.options.collection.at(0).getConfigurationItemId(),
                i18n: App.config.i18n
            };

            this.$el.html(Mustache.render(template, data));
            this.bindDomElements();

            this.usedByProductInstanceViews = [];
            this.options.collection.each(this.addProductInstanceView.bind(this));

            if(this.partModel){

                var self = this;
                $.ajax({
                    type:'GET',
                    url: App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/parts/' + this.partModel.getPartKey() + '/used-by-as-component',
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
                    url: App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/parts/' + this.partModel.getPartKey() + '/used-by-as-substitute',
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

            return this;
        },

        bindDomElements: function () {
            this.productInstancesUL = this.$('#used-by-product-instances');
            this.$componenetOfUL = this.$('#used-by-is-component-of');
            this.$substituteOfUL = this.$('#used-by-is-substitute-of');
        },

        addProductInstanceView: function (model) {
            var usedByView = new UsedByListItemView({
                model: model
            }).render();

            this.usedByProductInstanceViews.push(usedByView);
            this.productInstancesUL.append(usedByView.$el);
        }

    });

    return UsedByListView;
});

/*global define,App,$*/
define([
    'backbone',
    'mustache',
    'text!common-objects/templates/pathToPathLink/path_to_path_link_item.html',
    'common-objects/views/alert'
], function (Backbone, Mustache, template,AlertView){
    'use strict';

    var PathToPathLinkItemView = Backbone.View.extend({

        className:'well',

        events:{
            'click .delete-item' : 'onDeleteItem'
        },

        initialize: function(){
            this.model = this.options.model;
        },


        render: function () {

            var data = {
                i18n: App.config.i18n,
                creationMode : this.model.creationMode,
                sourceComponents : this.model.pathToPath.sourceComponents,
                targetComponents : this.model.pathToPath.targetComponents,
                availableType: this.model.availableType,
                description : this.model.pathToPath.description,
                type : this.model.pathToPath.type,
                canSuppress: this.model.canSuppress
            };

            this.$el.html(Mustache.render(template, data));
            this.$('.link-source .fa-long-arrow-right').last().remove();
            this.$('.link-target .fa-long-arrow-right').last().remove();

            return this;
        },
        onDeleteItem: function () {
            if (this.model.canSuppress) {
                var self = this;
                var urlToDelete = App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/products/' + this.model.serialNumber + '/path-to-path-links/' + this.model.pathToPath.id;
                $.ajax({
                    type: 'DELETE',
                    url: urlToDelete,
                    contentType: 'application/json',
                    success: function () {
                        self.trigger('pathToPathLink:remove');
                        self.remove();
                    },
                    error: function (errorMessage) {
                        self.$('.error-div').append(new AlertView({
                            type: 'error',
                            message: errorMessage.responseText
                        }).render().$el);
                    }
                });
            }
        }



    });

    return PathToPathLinkItemView;
});

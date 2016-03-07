/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!common-objects/templates/part/import_status.html'
], function (Backbone,Mustache,template) {
    'use strict';
    var ImportStatusView = Backbone.View.extend({
        className:'import-status',

        events:{
            'click .delete':'deleteImport'
        },

        render:function(){
            this.$el.html(Mustache.render(template, {status:this.model,i18n:App.config.i18n}));
            if(this.model.pending){
                setTimeout(this.refresh.bind(this),1000);
            }
            return this;
        },

        deleteImport:function(){
            var url = App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/parts/import/'+this.model.id;
            $.ajax({
                url:url,
                method:'DELETE',
                success:this.remove.bind(this)
            });
        },

        refresh:function(){
            var url = App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/parts/import/'+this.model.id;
            var _this = this;
            $.get(url).then(function(pImport){
                _this.model = pImport;
                _this.render();
            });

        }

    });
    return ImportStatusView;
});

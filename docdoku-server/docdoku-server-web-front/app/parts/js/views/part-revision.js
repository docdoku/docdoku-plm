/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/part-revision.html',
    'views/cad-file-view'
], function (Backbone, Mustache, template, CADFileView) {
    'use strict';

    var PartRevisionView = Backbone.View.extend({

        events : {
            'click a[href="#tab-cad-file"]':'showCADFileView'
        },

        showCADFileView:function(){
            if(this.lastIteration.geometryFileURI){
                if(!this.cadFileView){
                    this.cadFileView =  new CADFileView().render(App.config.contextPath + this.lastIteration.geometryFileURI);
                    this.$('#tab-cad-file').html(this.cadFileView.$el);
                }
                this.cadFileView.resize();
            }
        },

        render: function (part) {

            var _this = this;
            this.part = part;
            var lastIteration = part.partIterations[part.partIterations.length-1];
            this.lastIteration = lastIteration;

            this.$el.html(Mustache.render(template, {
                i18n: App.config.i18n,
                contextPath:App.config.contextPath,
                part:part,
                lastIteration:lastIteration
            })).show();

            this.$accordion = this.$('#tab-part-files > .accordion');

            _.each(lastIteration.attachedFiles,function(file){
                $.get(App.config.contextPath+'/api/viewer?fileName='+encodeURIComponent(file)).then(function(data){
                    _this.$accordion.append(data);
                });
            });

            console.log(lastIteration)
            return this;
        }
    });

    return PartRevisionView;
});

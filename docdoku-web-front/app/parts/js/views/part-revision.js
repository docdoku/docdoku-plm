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
                    var fileName = App.config.contextPath + this.lastIteration.geometryFileURI;
                    var nativeCADFile = App.config.contextPath + '/api/files/' + this.lastIteration.nativeCADFile;
                    this.cadFileView =  new CADFileView().render(nativeCADFile, fileName, this.uuid);
                    this.$('#tab-cad-file').html(this.cadFileView.$el);
                }
                this.cadFileView.resize();
            }
        },

        render: function (part, uuid) {

            var _this = this;
            this.uuid = uuid;
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

            _.each(lastIteration.attachedFiles,function(binaryResource){
                var url = App.config.contextPath+'/api/viewer?';
                if(uuid){
                    url+= 'uuid=' + encodeURIComponent(uuid) + '&';
                }
                $.get(url+'fileName='+encodeURIComponent(binaryResource.fullName)).then(function(data){
                    _this.$accordion.append(data);
                });
            });

            return this;
        }
    });

    return PartRevisionView;
});

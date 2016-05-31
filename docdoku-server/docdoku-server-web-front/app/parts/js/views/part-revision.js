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
            'click .nav.nav-tabs > li':'tabClicked'
        },

        tabClicked:function(e){
            if($(e.target).attr('href') === '#tab-cad-file'){
                this.cadFileView.resize();
            }
        },

        render: function (part) {

            var _this = this;

            var lastIteration = part.partIterations[part.partIterations.length-1];

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

            if(lastIteration.geometryFileURI){
                this.cadFileView =  new CADFileView({partIteration:lastIteration}).render(App.config.contextPath + lastIteration.geometryFileURI);
                this.$('#tab-cad-file').html(this.cadFileView.$el);
            }else{
                this.$('#tab-cad-file').html(App.config.i18n.NO_DATA);
            }

            return this;
        }
    });

    return PartRevisionView;
});

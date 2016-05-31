/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/part-revision.html'
], function (Backbone, Mustache, template) {
    'use strict';

    var PartRevisionView = Backbone.View.extend({
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

            console.log(part)
            return this;
        }
    });

    return PartRevisionView;
});

/*global define*/
define([
    'backbone',
    'common-objects/views/part/part_link_view'
], function (Backbone, PartLinkView) {

    'use strict';

    var PartSubstituteLinkView = PartLinkView.extend({
        className:'component part-substitute-link',
        handleSubstitutes:false,
        getComponent:function(){
            return this.model.get('substitute');
        }
    });

    return PartSubstituteLinkView;
});


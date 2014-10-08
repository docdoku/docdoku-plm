define([], function() {
	'use strit';
    var Part = Backbone.Model.extend({

        init:function(number, version){
            this.set('number',number);
            this.set('version',version);
            return this;
        },

        getNumber:function(){
            return this.get('number');
        },

        getName:function(){
            return this.get('name');
        },

        getVersion: function() {
            return this.get('version');
        },

        getDescription: function() {
            return this.get('description');
        },

        getPartKey:function(){
            return this.get('partKey');
        },

        getWorkspace: function() {
            return this.get('workspace');
        },

        getCheckoutUser: function() {
            return this.get('checkOutUser');
        },

        getFormattedCheckoutDate: function() {
//            if(this.isCheckout()){
//                return Date.formatTimestamp(
//                    i18n._DATE_FORMAT,
//                    this.getCheckoutDate()
//                );
//            }
            // TODO ?
        },

        getFormattedCreationDate: function() {
//            return Date.formatTimestamp(
//                i18n._DATE_FORMAT,
//                this.getCreationDate()
//            );
            // TODO ?
        },

        getCheckoutDate: function() {
            return this.get('checkOutDate');
        },

        getCreationDate: function() {
            return this.get('creationDate');
        },


        isCheckoutByConnectedUser: function() {
//            return this.isCheckout() ? this.getCheckOutUserLogin() == APP_CONFIG.login : false;
            // TODO ?
        },

        getUrl: function() {
            return this.url();
        },

        hasIterations: function() {
            return !this.getIterations().isEmpty();
        },

        getLastIteration: function() {
            return this.getIterations().last();
        },

        getIterations: function() {
            return this.iterations;
        },

        getAuthorLogin:function(){
            return this.get('author').login;
        },

        getAuthorName:function(){
            return this.get('author').name;
        },

        getCheckOutUserName:function(){
            if(this.isCheckout()){
                return this.getCheckoutUser().name;
            }
        },

        getCheckOutUserLogin:function(){
            if(this.isCheckout()){
                return this.getCheckoutUser().login;
            }
        },

        isStandardPart:function(){
//          return this.get('standardPart') ? 1:0;
            // TODO ?
        },

        isStandardPartReadable:function(){
//            return this.get('standardPart') ? i18n.TRUE:i18n.FALSE;
            // TODO ?
        },


        isCheckout:function(){
            return !_.isNull(this.get('checkOutDate'));
        }

    });

    return Part;
});
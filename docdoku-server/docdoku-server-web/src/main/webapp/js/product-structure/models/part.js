define(["i18n!localization/nls/product-structure-strings","common-objects/utils/date","collections/part_iteration_collection"], function(i18n, Date, PartIterationList) {

    var Part = Backbone.Model.extend({

        idAttribute : "partKey",

        parse: function(data) {
            this.iterations = new PartIterationList(data.partIterations);
            this.iterations.setPart(this);
            delete data.partIterations;
            return data;
        },

        init:function(number, version){
            this.set("number",number);
            this.set("version",version);
            return this;
        },

        getNumber:function(){
            return this.get("number");
        },

        getName:function(){
            return this.get("name");
        },

        getVersion: function() {
            return this.get("version");
        },

        getDescription: function() {
            return this.get("description");
        },

        getPartKey:function(){
            return this.get("partKey");
        },

        getWorkspace: function() {
            return this.get("workspaceId");
        },

        getCheckoutUser: function() {
            return this.get('checkOutUser');
        },

        getFormattedCheckoutDate: function() {
            if(this.isCheckout()){
                return Date.formatTimestamp(
                    i18n._DATE_FORMAT,
                    this.getCheckoutDate()
                );
            }
        },

        getFormattedCreationDate: function() {
            return Date.formatTimestamp(
                i18n._DATE_FORMAT,
                this.getCreationDate()
            );
        },

        getCheckoutDate: function() {
            return this.get('checkOutDate');
        },

        getCreationDate: function() {
            return this.get('creationDate');
        },

        isCheckout:function(){
            return !_.isNull(this.get("checkOutDate"));
        },

        isCheckoutByConnectedUser: function() {
            return this.isCheckout() ? this.getCheckOutUserLogin() == APP_CONFIG.login : false;
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
            return this.get("author").login;
        },

        getAuthorName:function(){
            return this.get("author").name;
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
          return this.get("standardPart") ? 1:0;
        },

        checkout: function() {
            $.ajax({
                context: this,
                type: "PUT",
                url: this.url() + "/checkout",
                success: function() {
                    this.fetch();
                }
            });
        },

        undocheckout: function() {
            $.ajax({
                context: this,
                type: "PUT",
                url: this.url() + "/undocheckout",
                success: function() {
                    this.fetch();
                }
            });
        },

        checkin: function() {
            $.ajax({
                context: this,
                type: "PUT",
                url: this.url() + "/checkin",
                success: function() {
                    this.fetch();
                }
            });
        },

        isCheckout: function() {
            return !_.isNull(this.attributes.checkOutDate);
        },

        url:function(){
            return "/api/workspaces/" + APP_CONFIG.workspaceId + "/parts/" + this.getPartKey();
        }

    });

    return Part;

});

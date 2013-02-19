define(function () {

    var Attribute = Backbone.Model.extend({

        getType:function () {
            return this.get("type");
        },

        getName:function () {
            return this.get("name");
        },

        getValue:function () {
            return this.get("value");
        },

        toString:function () {
            return this.getName() + ":" + this.getValue() + "(" + this.getType() + ") ";
        }

    });

    Attribute.types = {
        NUMBER:"NUMBER", DATE:"DATE", BOOLEAN:"BOOLEAN", TEXT:"TEXT", URL:"URL"};

    return Attribute;
});
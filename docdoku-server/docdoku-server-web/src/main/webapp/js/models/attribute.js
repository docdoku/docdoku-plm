define([
    "i18n",
    "models/document_iteration"
], function (i18n, iteration) {

    var Attribute = Backbone.Model.extend({
        initialize:function () {
            var self = this;
            this.className = "Attribute";

            //expected : documentIteration
            kumo.assertNotEmpty(this.attributes.type,
                "an Attribute Model should have type");
            kumo.assert(
                _.any(Attribute.types,
                    function (val) {
                        return val == self.getType()
                    }),
                "Attribute type : " + this.getType() + " not in " + JSON.stringify(Attribute.types)
            );

            switch (this.getType()){
                //forcing number
                case Attribute.types.NUMBER :
                case Attribute.types.DATE :
                    this.set("value", parseFloat(this.getValue()));
            }

            //_.bindAll(this);
        },

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
        },

        //from backbone Doc : If the attributes are valid, don't return anything from validate
        validate:function () {
            if (kumo.any([this.getType(), this.getName(), this.getValue()])) {
                return i18n.VALIDATION_FAILED_FOR+this.getName();
            }
            try{
                var value = this.getValue();
                var ok = true;
                switch (this.getType()) {

                    case Attribute.types.NUMBER :
                    case Attribute.types.DATE :
                        value = parseFloat(value);
                        this.set({value : value}, {silent:true});//fixing missed conversion
                        ok = (_.isNumber(value) && !_.isNaN(value));
                        break;

                    case Attribute.types.BOOLEAN :
                        value = Boolean(value);
                        this.set({value : value}, {silent:true});//fixing missed conversion
                        ok = (value === true || value === false) && typeof(value) == "boolean";
                        break;

                    case Attribute.types.TEXT :
                    case Attribute.types.URL :
                        ok = typeof(value) == "string" && value !="";

                        break;
                }
            }catch (e){
                console.error("Unexpected fail during validation "+e);
                return i18n.VALIDATION_FAILED_FOR+this.getName();
            }

            if (!ok){
                console.error ("validation error :"+this.getName()+" : "+this.getValue());
                return i18n.VALIDATION_FAILED_FOR+this.getName()+" : "+this.getValue();

            }

        }

    });

    Attribute.types = {
        NUMBER:"NUMBER", DATE:"DATE", BOOLEAN:"BOOLEAN", TEXT:"TEXT", URL:"URL"};

    return Attribute;
});
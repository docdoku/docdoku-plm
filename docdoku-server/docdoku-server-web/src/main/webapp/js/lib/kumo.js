var kumo = {
    urlRegex:new RegExp(/[-a-zA-Z0-9@:%_\+.~#?&//=]{2,256}\.[a-z]{2,4}\b(\/[-a-zA-Z0-9@:%_\+.~#?&//=]*)?/gi),
    //isEmpty or undefined or null, or empty array ; 0 is said as NOT empty

    isEmpty:function (object) {
        return this.e(object);
    },
    isNotEmpty:function (object) {
        return this.ne(object);
    },
    //if object is empty, object is set to initValue
    initIfEmpty:function (object, initValue) {
        if (this.e(object)) {
            object = initValue;
        }
        return object;
    },

    e:function (value) {

        if (value === null || typeof (value) == 'undefined') {
            return true;
        }


        if (typeof (value) == 'function') {
            return false;
        }

        if (typeof (value) == 'number') {
            if (isNaN(value)) return true;
            else return false;
        }

        //check for an empty array or string
        if (typeof(value.length) != 'undefined')
            return value.length <= 0;


        if (typeof(value)=='string'){
          return (value == "");
        }

        if (typeof(value)=='object'){
            //check if we have an empty object
            for (var key in value){
                if (value.hasOwnProperty(key) ){
                    return false;
                }
            }
            //we have an empty object
            return true;
        }



        return false;
    },
    //notEmpty
    ne:function (value) {
        return !this.e(value);
    },
    //assert
    assert:function (condition, messageIfFalse) {
        if (!condition && this.enableAssert) {
            if (this.ne(messageIfFalse)) {
                console.error(messageIfFalse);
            } else {
                console.log("An unexpected error happened with an assert");
            }
        }
    },
    assertNotEmpty:function (requiredValue, message) {
        this.assertNotAny([requiredValue], message);
    },
    //assert no value are empty
    assertNotAny:function (requiredValues, message) {
        this.assert(!this.any(requiredValues), message);

    },
    xor:function (x, y) {
        return this.e(x) ? !this.e(y) : this.e(y)
    },
    //returns true if any value in the array is empty
    any:function (array) {
        for (var i = 0; i < array.length; i++) {
            if (this.e(array[i])) {
                return true;
            }
        }
        return false;
    },
    enableAssert:true,

    replaceAll:function (string, that, byThat) {
        var regex = new RegExp(that, 'g');
        return string.replace(regex, byThat);
    },
    //accept minuscules, nombres et tirets
    checkSimpleName:function (name) {
        return /^([a-z1-9_\-.])+$/.test(name);
    },

    preventDoubleClick:function (element, duration) {
        if (this.isEmpty(duration)) duration = 1000;
        var timeout = function () {
            element.disabled = false;
            console.log("release lock");
        };
        element.click(function () {
            element.disabled = true;
            console.log("locking");
            setTimeout(timeout, duration);
        });
        element.release = function () {
            console.log("force release");
            element.disabled = false;
            clearTimeout(timeout);
        }

    },


    enableActionState:function (object) {
        object.actionRunning = "Une action est en cours. Veuillez l'annuler.";
        var States = {
            IDLE:0,
            ACTION:1
        };

        object.state = States.IDLE;

        object.setAction = function () {
            if (object.state != States.IDLE) {
                console.log(object.actionRunning);
                return false;
            } else {
                object.state = States.ACTION;
                return true;
            }
        };
        object.clearAction = function () {
            kumo.assert(object.state != States.IDLE, "state is already IDLE");
            object.state = States.IDLE;
        }

        object.checkClear = function () {
            if (object.state == States.IDLE) {
                return true;
            } else {
                return false;
            }
        };
    },

    createUuid:function () {
        var s = [], itoh = '0123456789ABCDEF';
        for (var i = 0; i < 36; i++)
            s[i] = Math.floor(Math.random() * 0x10);

        // Conform to RFC-4122, section 4.4
        s[14] = 4;  // Set 4 high bits of time_high field to version
        s[19] = (s[19] & 0x3) | 0x8;  // Specify 2 high bits of clock sequence

        // Convert to hex chars
        for (var i = 0; i < 36; i++)
            s[i] = itoh[s[i]];

        // Insert '-'s
        s[8] = s[13] = s[18] = s[23] = '-';

        return s.join('');
    },

    getDateStr: function() {
        var temp = new Date();
        var dateStr =   kumo.padStr(temp.getDate()) + "/" +
                        kumo.padStr(1 + temp.getMonth()) + "/" +
                        kumo.padStr(temp.getFullYear()) + " " +
                        kumo.padStr(temp.getHours()) + ":" +
                        kumo.padStr(temp.getMinutes()) + ":" +
                        kumo.padStr(temp.getSeconds());
        return dateStr;
    },

    padStr: function(i) {
        return (i < 10) ? "0" + i : "" + i;
    },

    isEmptyOrUndefined: function(value) {
        if (_.isObject(value)) {
            return _.isEmpty(value);
        } else if (_.isString(value)) {
            return value == "";
        }

        return _.isUndefined(value);
    }

};



if (!window.console) {
    console = {
        log:function () {
        },
        trace:function () {
        },
        error:function () {
        }
    }
}



/*global define*/

define(function () {

    'use strict';

    return {

        STATE: {
            OPEN: 0,
            LOCKED: 1,
            DISPLAY_ONLY: 2,
            FROZEN: 3,
            NO_EDIT: 4
        },

        /**
         * Create an object which define the availables action for each STATE.
         */
        getAvailabilityByState: function (state) {
            var proto = {
                remove: true,
                sortable: true,
                type: true,
                name: true,
                value: true,
                displayRequired: true
            };
            switch (state) {

                case this.STATE.NO_EDIT:
                    proto.value = false;
                    proto.name = false;
                    proto.type = false;
                    proto.remove = false;
                    proto.sortable = false;
                    break;

                case this.STATE.FROZEN:
                    proto.sortable = false;
                    proto.remove = false;
                    proto.name = false;
                    proto.type = false;
                    break;

                case this.STATE.LOCKED:
                    proto.remove = false;
                    proto.name = false;
                    proto.type = false;
                    break;

                case this.STATE.DISPLAY_ONLY:
                    proto.sortable = false;
                    proto.type = true;
                    // won't print the display label nor specify the input value as required.
                    proto.displayRequired = false;
                    break;

                //Prototype is already in open mode
                //no need to redefine.
                default:
            }

            return proto;
        },
        /**
         * Get the availability state in function of the attribute data.
         */
        getState: function (editMode, attributesLocked, locked, displayOnly) {

            if (!editMode) {
                return this.STATE.NO_EDIT;
            } else if (displayOnly) {
                return this.STATE.DISPLAY_ONLY;
            }
            else if (attributesLocked) {
                return this.STATE.FROZEN;
            } else if (locked) {
                return this.STATE.LOCKED;
            } else {
                return this.STATE.OPEN;
            }
        },

        /**
         * Get the object which define the available actions in function of the attribute data.
         */
        getAvailability: function (editMode, attributesLocked, locked, displayOnly) {
            return this.getAvailabilityByState(this.getState(editMode, attributesLocked, locked, displayOnly));
        }
    };
});

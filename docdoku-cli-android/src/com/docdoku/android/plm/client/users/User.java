/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
 *
 * This file is part of DocDokuPLM.
 *
 * DocDokuPLM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDokuPLM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with DocDokuPLM.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.docdoku.android.plm.client.users;

import android.provider.ContactsContract;
import android.util.Log;

import java.util.ArrayList;

/**
 * Model for a user of the workspace
 *
 * @author: Martin Devillers
 */
public class User {
    private static final String LOG_TAG = "com.docdoku.android.plm.client.users.User";

    public static final String JSON_KEY_USER_NAME = "name";
    public static final String JSON_KEY_USER_EMAIL = "email";
    public static final String JSON_KEY_USER_LOGIN = "login";

    private final String name;
    private final String login;
    private final String email;

    private boolean existsOnPhone;
    private ArrayList<PhoneNumber> phoneNumbers;

    public User(String name, String email, String login){
        this.name = name;
        this.email = email;
        this.login = login;
        existsOnPhone = false;
        phoneNumbers = new ArrayList<PhoneNumber>();
    }

    public boolean existsOnPhone(){
        return existsOnPhone;
    }

    /**
     * Indicate if a contact with an email address matching this user's was found on the phone
     * @param existsOnPhone if a contact was found
     */
    public void setExistsOnPhone(boolean existsOnPhone){
        this.existsOnPhone = existsOnPhone;
    }

    /**
     * Add a phone number belonging to a contact on the phone to this user
     * @param number the phone number
     * @param type the type of phone number (mobile, home, ...)
     * @param typeCode
     */
    public void addPhoneNumber(String number, String type, int typeCode){
        phoneNumbers.add(new PhoneNumber(number, type, typeCode));
    }

    public String getName(){
        return name;
    }

    public String getEmail(){
        return email;
    }

    public String getLogin(){
        return login;
    }

    /**
     * Searches the phone number provided for this <code>User</code>. If a mobile phone number is found, returns it.
     * Otherwise, if a phone number is found, returns the first one in the <code>ArrayList</code>. If no phone numbers
     * are found, returns an empty <code>String</code>.
     * @return the best phone number found for contact
     */
    public String getPhoneNumber(){
        for (PhoneNumber phoneNumber : phoneNumbers) {
            if (phoneNumber.typeCode == ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE) {
                Log.i(LOG_TAG, "Found mobile number for contact " + name);
                return phoneNumber.getNumber();
            }
        }
        try{
            return phoneNumbers.get(0).getNumber();
        }catch (IndexOutOfBoundsException e){
            Log.i(LOG_TAG, "No phone number found for a contact on phone");
            return "";
        }
    }

    public String[] getPhoneNumbers(){
        String[] phoneNumbersArray = new String[phoneNumbers.size()];
        for (int i = 0; i<phoneNumbersArray.length; i++){
            phoneNumbersArray[i] = phoneNumbers.get(i).number;
        }
        return phoneNumbersArray;
    }

    /**
     * Class representing a phone number, with a type and a number.
     */
    public class PhoneNumber{

        private final String number, type;
        private final int typeCode;

        public PhoneNumber(String number, String type, int typeCode){
            this.number = number;
            this.type = type;
            this.typeCode = typeCode;
        }

        public String getNumber(){
            return number;
        }
    }
}

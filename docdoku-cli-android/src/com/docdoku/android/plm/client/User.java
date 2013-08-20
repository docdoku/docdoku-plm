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

package com.docdoku.android.plm.client;

import android.provider.ContactsContract;
import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author: Martin Devillers
 */
public class User {

    private String name;
    private String login;
    private String email;

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

    public void setExistsOnPhone(boolean existsOnPhone){
        this.existsOnPhone = existsOnPhone;
    }

    public void addPhoneNumber(String number, String type){
        phoneNumbers.add(new PhoneNumber(number, type));
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

    public String getPhoneNumber(){
        Iterator<PhoneNumber> iterator = phoneNumbers.iterator();
        while (iterator.hasNext()){
            PhoneNumber phoneNumber = iterator.next();
            if (phoneNumber.type.toString().equals(ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)){
                Log.i("com.docdoku.android.plm", "Found mobile number for contact " + name);
                return phoneNumber.getNumber();
            }
        }
        try{
            return phoneNumbers.get(0).getNumber();
        }catch (IndexOutOfBoundsException e){
            Log.i("com.docdoku.android.plm", "No phone number found for a contact on phone");
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

    public class PhoneNumber{

        private String number;
        private String type;

        public PhoneNumber(String number, String type){
            this.number = number;
            this.type = type;
        }

        public String getNumber(){
            return number;
        }
    }
}

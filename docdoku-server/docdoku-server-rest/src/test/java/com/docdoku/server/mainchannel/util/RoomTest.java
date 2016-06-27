/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2015 DocDoku SARL
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
package com.docdoku.server.mainchannel.util;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import javax.websocket.Session;
import java.security.Principal;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Asmae CHADID
 */
@RunWith(MockitoJUnitRunner.class)
    public class RoomTest {

    private static Room room = Mockito.mock(Room.class);
    private static ConcurrentMap<String, Room> DB = Mockito.mock(ConcurrentHashMap.class);
    private static Session userSession1 = Mockito.mock(Session.class);
    private static Principal principal1 = Mockito.mock(Principal.class);
    private static Principal principal2 = Mockito.mock(Principal.class);
    private static Principal principal3 = Mockito.mock(Principal.class);
    private static Session userSession2 = Mockito.mock(Session.class);
    private static Room secondRoom = Mockito.spy(new Room("PLMRoom"));
    private static Room thirdRoom = Mockito.spy(new Room("ChatRoom"));
    private static Session userSession3 = Mockito.mock(Session.class);

    @BeforeClass
    public static void setUp() {

        Mockito.when(room.addUserSession(userSession1)).thenCallRealMethod();
        Mockito.when(room.addUserSession(userSession2)).thenCallRealMethod();
        Mockito.when(room.key()).thenReturn("plm");

        Mockito.when(userSession1.getUserPrincipal()).thenReturn(principal1);
        Mockito.when(principal1.getName()).thenReturn("user1");
        Mockito.when(room.getUser1Login()).thenCallRealMethod();

        Mockito.when(userSession2.getUserPrincipal()).thenReturn(principal2);
        Mockito.when(principal2.getName()).thenReturn("user2");
        Mockito.when(room.getUser2Login()).thenCallRealMethod();

        Mockito.when(principal3.getName()).thenReturn("user3");
        Mockito.when(userSession3.getUserPrincipal()).thenReturn(principal3);

        Mockito.when(room.getUserSession(Matchers.anyString())).thenCallRealMethod();
        Mockito.when(room.getOtherUserSession(Matchers.any(Session.class))).thenCallRealMethod();
        Mockito.when(DB.get(Matchers.anyString())).thenReturn(room);
        Mockito.when(DB.put(Matchers.anyString(), Matchers.any(Room.class))).thenReturn(room);
        Mockito.when(DB.get("plm").getSessionForUserLogin(Matchers.anyString())).thenCallRealMethod();
        Mockito.when(room.getOccupancy()).thenCallRealMethod();
        Mockito.doCallRealMethod().when(room).put();

        Mockito.when(room.hasUser(Matchers.anyString())).thenCallRealMethod();

        room.addUserSession(userSession1);
        room.addUserSession(userSession2);

        secondRoom.put();
        thirdRoom.put();

        Mockito.when(room.key()).thenReturn("plm");

    }

    @Test
    public void testGetByKeyName() {
        Assert.assertTrue("plm".equals(DB.get(" ").key()));
    }

    @Test
    public void testGetRoom() {
        Assert.assertTrue(DB.get("null").equals(room));
    }

    @Test
    public void testAddUserSession() {
        Assert.assertTrue(room.addUserSession(userSession1));
        Assert.assertTrue(room.addUserSession(userSession2));

    }

    @Test
    public void testGetUser1Login() {
        Assert.assertTrue("user1".equals(DB.get("plm").getUser1Login()));

    }

    @Test
    public void testGetUser2Login() {
        Assert.assertTrue("user2".equals(DB.get("plm").getUser2Login()));

    }

    @Test
    public void testGetSessionForUserLogin() {
        Assert.assertTrue(room.getSessionForUserLogin("user1") == userSession1);
        Assert.assertTrue(room.getSessionForUserLogin("user4") == null);
    }

    @Test
    public void testGetOccupency() {
        Assert.assertTrue(DB.get("plm").getOccupancy() == 2);
    }

    @Test
    public void testGetOtherUserSession() {
        //When
        Session userSession3 = Mockito.mock(Session.class);
        //Then
        Assert.assertTrue(DB.get("plm").getOtherUserSession(userSession1) == userSession2);
        Assert.assertTrue(DB.get("plm").getOtherUserSession(userSession2) == userSession1);
        Assert.assertTrue(DB.get("plm").getOtherUserSession(userSession3) == null);

    }

    @Test
    public void testHasUser() {
        Assert.assertTrue(DB.get("plm").hasUser("user1"));
    }

    @Test
    public void testGetUserSession() {
        Assert.assertTrue(DB.get("plm").getUserSession("user1").equals(userSession1));
        Assert.assertTrue(DB.get("plm").getUserSession("user2").equals(userSession2));
    }


    @Test
    public void testAddUserSessionSecondRoom() {
        //When
        secondRoom.addUserSession(userSession1);
        secondRoom.addUserSession(userSession2);

        //Then
        Mockito.verify(secondRoom, Mockito.times(1)).addUserSession(userSession1);
        Mockito.verify(secondRoom, Mockito.times(1)).addUserSession(userSession2);
        Mockito.verify(secondRoom, Mockito.times(1)).put();
    }

    @Test
    public void testRemoveUser() {
        //Given
        thirdRoom.addUserSession(userSession1);
        thirdRoom.addUserSession(userSession2);

        secondRoom.addUserSession(userSession1);
        secondRoom.addUserSession(userSession2);
        //When
        secondRoom.removeUser("user1");
        thirdRoom.removeUser("user2");
        //Then
        Assert.assertTrue(!secondRoom.hasUser("user1"));
        Assert.assertTrue(room.hasUser("user1"));

        Assert.assertTrue(secondRoom.hasUser("user2"));
        Assert.assertTrue(!thirdRoom.hasUser("user2"));
        Assert.assertTrue(room.hasUser("user2"));
    }

    @Test
    public  void testRemoveUserFromAllRooms() {
        //Given
        thirdRoom.addUserSession(userSession3);
        thirdRoom.addUserSession(userSession2);

        secondRoom.addUserSession(userSession1);
        secondRoom.addUserSession(userSession2);
        //When
        Room.removeUserFromAllRoom("user1");
        //Then
        Assert.assertTrue(!thirdRoom.hasUser("user1"));

    }
}
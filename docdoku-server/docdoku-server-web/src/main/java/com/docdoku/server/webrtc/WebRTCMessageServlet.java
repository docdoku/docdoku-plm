package com.docdoku.server.webrtc;

import com.docdoku.server.webrtc.util.Helper;
import com.docdoku.server.webrtc.util.Room;
import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "WebRTCMessageServlet", urlPatterns = {"/webRTCMessage"})
public class WebRTCMessageServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest pRequest,
            HttpServletResponse pResponse)
            throws ServletException, IOException {
        String message = Helper.getStringFromStream(pRequest.getInputStream());
        Map<String, String> params = Helper.getQueryMap(pRequest.getQueryString());
        String roomKey = params.get("r");
        Room room = Room.getByKeyName(roomKey);
        if (room != null) {
            String user = params.get("u");
            String otherUser = room.getOtherUser(user);
            if (otherUser != null && !otherUser.equals("")) {
                // special case the loopback scenario
                if (otherUser.equals(user)) {
                    message = message.replace("\"offer\"", "\"answer\"");
                    message = message.replace("a=crypto:0 AES_CM_128_HMAC_SHA1_32", "a=xrypto:0 AES_CM_128_HMAC_SHA1_32");
                }
                WebRTCApplication.send(Helper.makeToken(room, otherUser), message);
            }
        }
    }
}

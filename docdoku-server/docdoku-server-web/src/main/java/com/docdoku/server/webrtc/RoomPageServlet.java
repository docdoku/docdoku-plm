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

/**The main UI page */
@WebServlet(name = "RoomPageServlet", urlPatterns = {"/webRTCRoom"})
public class RoomPageServlet extends HttpServlet {

    /** Renders the main page. When this page is shown, we create a new channel to push asynchronous updates to the client.*/
    @Override
    protected void doGet(HttpServletRequest pRequest,
            HttpServletResponse pResponse)
            throws ServletException, IOException {
        String contextPath = pRequest.getContextPath().replace("/", "");
        String path;
        if(contextPath.isEmpty())
            path = "webRTCRoom";
        else
            path = contextPath + "/webRTCRoom";
        
        String query = pRequest.getQueryString();
        if (query == null) {
            String redirect = "/" + path + "?r=" + Helper.generateRandom(8);
            pResponse.sendRedirect(redirect);
            return;
        }
        Map<String, String> params = Helper.getQueryMap(query);
        String roomKey = Helper.sanitize(params.get("r"));
        String debug = params.get("debug");
        String stunServer = params.get("ss");
        String audioVideo = params.get("av");
        if (roomKey == null || roomKey.equals("")) {
            roomKey = Helper.generateRandom(8);
            String redirect = "/" + path + "?r=" + roomKey;
            if (debug != null) {
                redirect += ("&debug=" + debug);
            }
            if (stunServer != null || !stunServer.equals("")) {
                redirect += ("&ss=" + stunServer);
            }
            pResponse.sendRedirect(redirect);
            return;
        } else {
            String user = null;
            int initiator = 0;
            Room room = Room.getByKeyName(roomKey);
            if (room == null && (debug == null || !"full".equals(debug))) {
                user = Helper.generateRandom(8);
                room = new Room(roomKey);
                room.addUser(user);
                if (!"loopback".equals(debug)) {
                    initiator = 0;
                } else {
                    room.addUser(user);
                    initiator = 1;
                }
            } else if (room != null && room.getOccupancy() == 1 && !"full".equals(debug)) {
                user = Helper.generateRandom(8);
                room.addUser(user);
                initiator = 1;
            } else {
                pRequest.setAttribute("roomKey", roomKey);
                pRequest.getRequestDispatcher("/WEB-INF/webrtc/full.jsp").forward(pRequest, pResponse);
                return;
            }

            String server_name = pRequest.getServerName();
            int server_port = pRequest.getServerPort();
            String room_link = "http://" + server_name + ":" + server_port + "/" + path + "?r=" + roomKey;
            if (debug != null) {
                room_link += ("&debug=" + debug);
            }
            if (stunServer != null) {
                room_link += ("&ss=" + stunServer);
            }

            String token = Helper.makeToken(roomKey, user);
            String pc_config = Helper.makePCConfig(stunServer);

            pRequest.setAttribute("serverName", server_name);
            pRequest.setAttribute("serverPort", server_port + "");
            pRequest.setAttribute("PATH", contextPath);
            pRequest.setAttribute("token", token);
            pRequest.setAttribute("me", user);
            pRequest.setAttribute("roomKey", roomKey);
            pRequest.setAttribute("roomLink", room_link);
            pRequest.setAttribute("initiator", "" + initiator);
            pRequest.setAttribute("pcConfig", pc_config);

            pRequest.getRequestDispatcher("/WEB-INF/webrtc/index.jsp").forward(pRequest, pResponse);
        }
    }
}

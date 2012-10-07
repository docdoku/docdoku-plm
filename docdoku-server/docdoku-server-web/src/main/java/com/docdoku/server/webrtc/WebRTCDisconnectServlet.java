package com.docdoku.server.webrtc;

import com.docdoku.server.webrtc.util.Room;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "WebRTCDisconnectServlet", urlPatterns = {"/webRTCDisconnect"})
public class WebRTCDisconnectServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest pRequest,
            HttpServletResponse pResponse)
            throws ServletException, IOException {
        String[] key = pRequest.getParameterValues("from");
        if (key != null && key.length > 0) {
            Room.disconnect(key[0]);
        }

    }
}

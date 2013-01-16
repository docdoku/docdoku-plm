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

package com.docdoku.client.ui.workflow;

import javax.swing.*;
import java.awt.*;


public class HorizontalSeparatorCanvas extends JPanel {

    private int mRank;

    public HorizontalSeparatorCanvas(int pRank){
        mRank=pRank;
        setPreferredSize(new Dimension(10, 81));
    }

    public int getRank(){
        return mRank;
    }

    @Override
    protected void paintComponent(Graphics g) {
        int y = getHeight() / 2;
        g.fillRect(0, y, getWidth(), 2);
    }
}

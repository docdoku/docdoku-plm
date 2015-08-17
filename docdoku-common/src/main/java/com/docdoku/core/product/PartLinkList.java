package com.docdoku.core.product;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Elisabel Généreux
 */
public class PartLinkList {

    private List<PartLink> path = new ArrayList<>();

    public PartLinkList(List<PartLink> path) {
        this.path = path;
    }

    public List<PartLink> getPath() {
        return path;
    }

    public void setPath(List<PartLink> path) {
        this.path = path;
    }

    public int size() {
        return this.path.size();
    }

    public boolean isEmpty() {
        return this.path.isEmpty();
    }

    public PartLink[] toArray() {
        return path.toArray(new PartLink[this.size()]);
    }

}

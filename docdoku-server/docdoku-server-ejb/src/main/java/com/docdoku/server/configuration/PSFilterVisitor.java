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

package com.docdoku.server.configuration;

import com.docdoku.core.common.User;
import com.docdoku.core.configuration.PSFilter;
import com.docdoku.core.exceptions.EntityConstraintException;
import com.docdoku.core.exceptions.NotAllowedException;
import com.docdoku.core.exceptions.PartMasterNotFoundException;
import com.docdoku.core.product.*;
import com.docdoku.server.dao.PartMasterDAO;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public abstract class PSFilterVisitor {

    // Context
    private User user;
    private Locale locale;
    private PSFilter filter;
    private EntityManager em;
    private PartMasterDAO partMasterDAO;
    private Component component;
    private int stopAtDepth = -1;
    private boolean stopped = false;

    private String workspaceId;

    public PSFilterVisitor(EntityManager pEm, User pUser, PSFilter pFilter)
            throws PartMasterNotFoundException, NotAllowedException, EntityConstraintException {

        filter = pFilter;
        em = pEm;
        user = pUser;
        workspaceId = user.getWorkspaceId();
        locale = new Locale(user.getLanguage());
        partMasterDAO = new PartMasterDAO(locale, em);

    }

    public void visit(PartMaster pNodeFrom, Integer pDepth) throws PartMasterNotFoundException, EntityConstraintException, NotAllowedException {
        if(pNodeFrom != null){
            startVisit(pNodeFrom,pDepth);
        }else{
            throw new IllegalArgumentException("Provide either a node from which starting visit or a resolved part links list");
        }
    }

    public void visit(List<PartLink> pStartingPath, Integer pDepth) throws PartMasterNotFoundException, EntityConstraintException, NotAllowedException {
        if(pStartingPath != null){
            startVisit(pStartingPath,pDepth);
        }else{
            throw new IllegalArgumentException("Provide either a node from which starting visit or a resolved part links list");
        }
    }

    public void stop(){
        stopped = true;
    }

    private void startVisit(List<PartLink> pStartingPath, Integer pDepth) throws NotAllowedException, EntityConstraintException, PartMasterNotFoundException {
        List<PartLink> currentPath = pStartingPath;
        List<PartMaster> currentPathParts = new ArrayList<>();
        List<PartIteration> currentPathPartIterations = new ArrayList<>();

        // Visit last
        PartMaster rootNode = currentPath.get(currentPath.size() - 1).getComponent();
        currentPathParts.add(rootNode);

        stopAtDepth = (pDepth == null) ? -1 : pDepth + currentPath.size();

        component = new Component(rootNode.getAuthor(),rootNode,currentPath,null);
        component.setComponents(visit(component,currentPathPartIterations, currentPathParts, currentPath));

    }

    private void startVisit(PartMaster pNodeFrom, Integer pDepth) throws NotAllowedException, EntityConstraintException, PartMasterNotFoundException {

        List<PartLink> currentPath = new ArrayList<>();
        List<PartMaster> currentPathParts = new ArrayList<>();
        List<PartIteration> currentPathPartIterations = new ArrayList<>();

        PartMaster rootNode = pNodeFrom;

        // Add root node and its virtual link
        currentPathParts.add(rootNode);
        currentPath.add(new PartLink() {
            @Override
            public int getId() {
                return 1;
            }

            @Override
            public double getAmount() {
                return 1;
            }

            @Override
            public String getUnit() {
                return null;
            }

            @Override
            public String getComment() {
                return "";
            }

            @Override
            public boolean isOptional() {
                return false;
            }

            @Override
            public PartMaster getComponent() {
                return rootNode;
            }

            @Override
            public List<PartSubstituteLink> getSubstitutes() {
                return null;
            }

            @Override
            public String getReferenceDescription() {
                return null;
            }

            @Override
            public Character getCode() {
                return '-';
            }

            @Override
            public String getFullId() {
                return "-1";
            }

            @Override
            public List<CADInstance> getCadInstances() {
                return null;
            }
        });

        stopAtDepth = (pDepth == null) ? -1 : pDepth;

        component = new Component(rootNode.getAuthor(),rootNode,currentPath,null);
        component.setComponents(visit(component,currentPathPartIterations, currentPathParts, currentPath));
    }


    private List<Component> visit(Component currentComponent, List<PartIteration> pCurrentPathPartIterations, List<PartMaster> pCurrentPathParts, List<PartLink> pCurrentPath) throws PartMasterNotFoundException, NotAllowedException, EntityConstraintException {

        List<Component> components = new ArrayList<>();

        if(stopped){
            return components;
        }

        if(!onPathWalk(new ArrayList<>(pCurrentPath), new ArrayList<>(pCurrentPathParts))) {
            return components;
        }



        // Current depth
        int depth = pCurrentPathParts.size() - 1;

        // Current part master is the last from pCurrentPathParts
        PartMaster currentUsagePartMaster = pCurrentPathParts.get(depth);

        // Find filtered iterations to visit
        List<PartIteration> partIterations = filter.filter(currentUsagePartMaster);

        if(partIterations.isEmpty()){
            onUnresolvedVersion(currentUsagePartMaster);
        }

        if(partIterations.size() > 1){
            onIndeterminateVersion(currentUsagePartMaster, new ArrayList<>(partIterations));
        }

        if(partIterations.size()==1){
            currentComponent.setRetainedIteration(partIterations.get(0));
        }

        // Visit them all, potentially diverging branches
        for (PartIteration partIteration : partIterations) {

            // We know which iteration of current partMaster, add it to list
            List<PartIteration> copyPartIteration = new ArrayList<>(pCurrentPathPartIterations);
            copyPartIteration.add(partIteration);

            // Is branch over ?
            if(partIteration.getComponents().isEmpty()){
                onBranchDiscovered(new ArrayList<>(pCurrentPath),new ArrayList<>(copyPartIteration));
            }

            // Navigate links
            for (PartUsageLink usageLink : partIteration.getComponents()) {

                List<PartLink> currentPath = new ArrayList<>(pCurrentPath);
                currentPath.add(usageLink);

                // Filter the current path, potentially diverging branches
                List<PartLink> eligiblePath = filter.filter(currentPath);

                if(eligiblePath.isEmpty() && !usageLink.isOptional()){
                    onUnresolvedPath(new ArrayList<>(currentPath), new ArrayList<>(copyPartIteration));
                }

                if(eligiblePath.size() > 1 ){
                    onIndeterminatePath(new ArrayList<>(currentPath), new ArrayList<>(copyPartIteration));
                }

                if (eligiblePath.size() == 1 && eligiblePath.get(0).isOptional()){
                    onOptionalPath(new ArrayList<>(currentPath), new ArrayList<>(copyPartIteration));
                }

                for(PartLink link : eligiblePath){
                    List<PartLink> nextPath = new ArrayList<>(pCurrentPath);
                    nextPath.add(link);

                    // Stop if depth reached
                    if (stopAtDepth == -1 || stopAtDepth >= pCurrentPathParts.size()) {

                        // Going on a new path
                        PartMaster pm = loadPartMaster(link.getComponent().getNumber());

                        // Run cyclic integrity check here
                        if(pCurrentPathParts.contains(pm)){
                            throw new EntityConstraintException(locale,"EntityConstraintException12");
                        }

                        // Continue tree walking on pm
                        List<PartMaster> copyPathParts = new ArrayList<>(pCurrentPathParts);
                        List<PartLink> copyPath = new ArrayList<>(nextPath);
                        List<PartIteration> copyPartIterations = new ArrayList<>(copyPartIteration);
                        copyPathParts.add(pm);

                        // Recursive
                        Component subComponent= new Component(pm.getAuthor(), pm, copyPath, null);
                        subComponent.setComponents(visit(subComponent,copyPartIterations, copyPathParts, copyPath));
                        components.add(subComponent);
                    }

                }

            }
        }

        return components;
    }



    private PartMaster loadPartMaster(String partNumber) throws PartMasterNotFoundException {
        return partMasterDAO.loadPartM(new PartMasterKey(workspaceId, partNumber));
    }

    /**
     * Getters
     */
    public Component getComponent() {
        return component;
    }

    /**
     * Abstracts
     * */

    public abstract void onIndeterminateVersion(PartMaster partMaster, List<PartIteration> partIterations) throws NotAllowedException;
    public abstract void onUnresolvedVersion(PartMaster partMaster) throws NotAllowedException;
    public abstract void onIndeterminatePath(List<PartLink> pCurrentPath, List<PartIteration> pCurrentPathPartIterations) throws NotAllowedException;
    public abstract void onUnresolvedPath(List<PartLink> pCurrentPath, List<PartIteration> partIterations) throws NotAllowedException;
    public abstract void onBranchDiscovered(List<PartLink> pCurrentPath, List<PartIteration> copyPartIteration);
    public abstract void onOptionalPath(List<PartLink> path, List<PartIteration> partIterations);
    public abstract boolean onPathWalk(List<PartLink> path, List<PartMaster> parts);
}

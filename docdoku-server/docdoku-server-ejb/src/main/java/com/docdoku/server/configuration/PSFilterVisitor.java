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
import com.docdoku.core.util.Tools;
import com.docdoku.server.dao.PartMasterDAO;

import javax.persistence.EntityManager;
import java.util.*;
import java.util.logging.Logger;


public abstract class PSFilterVisitor {

    private static final Logger LOGGER = Logger.getLogger(PSFilterVisitor.class.getName());

    // Context
    private User user;
    private Locale locale;
    private PSFilter filter;
    private EntityManager em;
    private PartMasterDAO partMasterDAO;
    private Component component;
    private int stopAtDepth = -1;

    // Walked parts and paths
    private Set<String> retainedSubstitutes = new HashSet();
    private Set<String> retainedOptionals = new HashSet();
    private Set<PartIteration> retainedBaselinedIterations = new HashSet();


    private String workspaceId;

    public PSFilterVisitor(EntityManager pEm, User pUser, PSFilter pFilter, PartMaster nodeFrom, Integer pDepth)
            throws PartMasterNotFoundException, NotAllowedException, EntityConstraintException {

        filter = pFilter;
        em = pEm;
        user = pUser;
        workspaceId = user.getWorkspaceId();
        locale = new Locale(user.getLanguage());
        partMasterDAO = new PartMasterDAO(locale, em);

        stopAtDepth = (pDepth == null) ? -1 : pDepth;


        // Lists that will be copied through the tree
        List<PartLink> currentPath = new ArrayList<>();
        List<PartMaster> currentPathParts = new ArrayList<>();
        List<PartIteration> currentPathPartIterations = new ArrayList<>();

        PartMaster rootNode = nodeFrom;

        // Add root node and its virtual link
        currentPathParts.add(rootNode);
        currentPath.add(new PartLink() {
            @Override
            public int getId() {
                return -1;
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
        });

        // Start visiting
        log(0, "Starting visit method");
        component = new Component(rootNode.getAuthor(),rootNode,currentPath,
                visit(currentPathPartIterations, currentPathParts, currentPath));
    }


    private List<Component> visit(List<PartIteration> pCurrentPathPartIterations, List<PartMaster> pCurrentPathParts, List<PartLink> pCurrentPath) throws PartMasterNotFoundException, NotAllowedException, EntityConstraintException {

        List<Component> components = new ArrayList<>();

        int depth = pCurrentPathParts.size() - 1;

        // Current part master is the last from pCurrentPathParts
        PartMaster currentUsagePartMaster = pCurrentPathParts.get(depth);

        log(depth, "Visit " + currentUsagePartMaster.getNumber());

        // Find filtered iterations to visit
        List<PartIteration> partIterations = filter.filter(currentUsagePartMaster);

        if(partIterations.isEmpty()){
            log(depth, "No iteration available for " + currentUsagePartMaster.getNumber() + ". Should raise an error in strict mode.");
            onUnresolvedVersion(currentUsagePartMaster);
        }

        if(partIterations.size() == 1){
            log(depth, "One iteration available for " + currentUsagePartMaster.getNumber() + ". Not diverging.");
        }

        if(partIterations.size() > 1){
            log(depth, "Many iterations available for " + currentUsagePartMaster.getNumber() + ". Should raise an error in strict mode.");
            onIndeterminateVersion(currentUsagePartMaster, partIterations);
        }

        // Retain filtered iterations
        if(!partIterations.isEmpty()){
            retainedBaselinedIterations.addAll(partIterations);
        }

        // Visit them all, potentially diverging branches
        for (PartIteration partIteration : partIterations) {

            // We know which iteration of current partMaster, add it to list
            List<PartIteration> copyPartIteration = new ArrayList<>(pCurrentPathPartIterations);
            copyPartIteration.add(partIteration);

            // Is branch over ?
            if(partIteration.getComponents().isEmpty()){
                log(depth, "This branch is over");
                log(depth, "------------------------------------------------------------");
                onBranchDiscovered(pCurrentPath,copyPartIteration);
            }

            // Navigate links

            for (PartUsageLink usageLink : partIteration.getComponents()) {

                List<PartLink> currentPath = new ArrayList<>(pCurrentPath);
                currentPath.add(usageLink);

                // Filter the current path, potentially diverging branches
                List<PartLink> eligibleLinks = filter.filter(currentPath);

                if(eligibleLinks.isEmpty() && usageLink.isOptional()){
                    retainedOptionals.add(Tools.getPathAsString(currentPath));
                }

                log(depth, "No link chosen " + usageLink.getComponent().getNumber() + " ! ");

                if(eligibleLinks.isEmpty() && !usageLink.isOptional()){
                    // This should not happen, however this is invalid, some cases should throw an exception here
                    onUnresolvedPath(currentPath);
                }

                if(eligibleLinks.size() == 1){
                    PartLink partLink = eligibleLinks.get(0);
                    log(depth, "One link chosen " + partLink.getComponent().getNumber() + " ! ");
                }

                if(eligibleLinks.size() > 1){
                    log(depth, "Cannot decide for usage link " + usageLink.getComponent().getNumber() + " ! Walking through usage link and substitutes");
                    onIndeterminatePath(pCurrentPath, copyPartIteration, usageLink);
                }

                for(PartLink link : eligibleLinks){
                    if(link instanceof PartSubstituteLink){
                        List<PartLink> substitutePath = new ArrayList<>(pCurrentPath);
                        substitutePath.add(link);
                        retainedSubstitutes.add(Tools.getPathAsString(substitutePath));
                    }
                    visitLink(link, pCurrentPathParts, pCurrentPath, copyPartIteration, components);
                }

            }
        }

        return components;
    }

    private void visitLink(PartLink link, List<PartMaster> pCurrentPathParts, List<PartLink> pCurrentPath, List<PartIteration> pCurrentPathPartIterations, List<Component> components) throws PartMasterNotFoundException, NotAllowedException, EntityConstraintException {

        // Going on a new path
        PartMaster pm = loadPartMaster(link.getComponent().getNumber());

        // Run cyclic integrity check here
        if(pCurrentPathParts.contains(pm)){
            throw new EntityConstraintException(locale,"EntityConstraintException12");
        }

        // Stop if depth reached
        if(stopAtDepth > pCurrentPathParts.size()){
            return;
        }

        // Continue tree walking on pm
        List<PartMaster> copyPathParts = new ArrayList<>(pCurrentPathParts);
        List<PartLink> copyPath = new ArrayList<>(pCurrentPath);
        List<PartIteration> copPartIterations = new ArrayList<>(pCurrentPathPartIterations);
        copyPathParts.add(pm);
        copyPath.add(link);

        // Recursive
        components.add(new Component(pm.getAuthor(), pm, copyPath, visit(copPartIterations, copyPathParts, copyPath)));

    }

    private PartMaster loadPartMaster(String partNumber) throws PartMasterNotFoundException {
        return partMasterDAO.loadPartM(new PartMasterKey(workspaceId, partNumber));
    }

    /**
    * Utils
    * */

    private void log(int depth, String message){
        char[] whiteSpace = new char[depth*4];
        Arrays.fill(whiteSpace, ' ');
        String whiteSpaces = new String(whiteSpace);
        LOGGER.info("[VISITOR] "+whiteSpaces+message);
    }

    /**
     * Getters
     */

    public List<String> getRetainedSubstitutes() {
        return new ArrayList(retainedSubstitutes);
    }

    public List<String> getRetainedOptionals() {
        return new ArrayList(retainedOptionals);
    }

    public List<PartIteration> getRetainedBaselinedIterations() {
        return new ArrayList(retainedBaselinedIterations);
    }

    public Component getComponent() {
        return component;
    }

    /**
     * Abstracts
     * */

    public abstract void onIndeterminateVersion(PartMaster partMaster, List<PartIteration> partIterations) throws NotAllowedException;
    public abstract void onUnresolvedVersion(PartMaster partMaster) throws NotAllowedException;
    public abstract void onIndeterminatePath(List<PartLink> pCurrentPath, List<PartIteration> pCurrentPathPartIterations, PartUsageLink usageLink) throws NotAllowedException;
    public abstract void onUnresolvedPath(List<PartLink> pCurrentPath) throws NotAllowedException;
    public abstract void onBranchDiscovered(List<PartLink> pCurrentPath, List<PartIteration> copyPartIteration);

}

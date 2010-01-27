/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010 DocDoku SARL
 *
 * This file is part of DocDoku.
 *
 * DocDoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDoku is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DocDoku.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.docdoku.server;

import com.docdoku.core.entities.BinaryResource;
import com.docdoku.core.entities.keys.MasterDocumentKey;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;

/**
 *
 * @author flo
 */
@Stateless(name="IndexSearcherBean")
public class IndexSearcherBean implements com.docdoku.server.IIndexSearcherLocal {
    
    @Resource(name="indexPath")
    private String indexPath;
    
    private IndexReader indexReader;
    
    @PostConstruct
    public void setup(){
        try{
            indexReader = IndexReader.open(indexPath);
        }catch (CorruptIndexException ex) {
            throw new EJBException(ex);
        }catch (IOException ex) {
            throw new EJBException(ex);
        }
    }
    
    @PreDestroy
    public void tearDown(){
        try{
            indexReader.close();
        }catch (IOException ex) {
            throw new EJBException(ex);
        }
    }
    
    public Set<MasterDocumentKey> searchInIndex(String pWorkspaceId, String pContent){
        try{
            Set<MasterDocumentKey> indexedKeys = new HashSet<MasterDocumentKey>();
            
            Query fullNameQuery = new WildcardQuery(new Term("fullName",pWorkspaceId + "/*"));
            Query contentQuery = new TermQuery(new Term("content",pContent));
            BooleanQuery mainQuery = new BooleanQuery();
            mainQuery.add(fullNameQuery,BooleanClause.Occur.MUST);
            mainQuery.add(contentQuery,BooleanClause.Occur.MUST);
           
            if(!indexReader.isCurrent()){
                //TODO use IndexReader.reopen(); when available
                indexReader.close();
                indexReader = IndexReader.open(indexPath);
            }
            IndexSearcher indexSearcher=new IndexSearcher(indexReader);
            Hits hits = indexSearcher.search(mainQuery);
            
            for (int i = 0; i < hits.length(); i++) {
                org.apache.lucene.document.Document doc = hits.doc(i);
                String fullName=doc.get("fullName");
                String[] partRefs=BinaryResource.parseOwnerRef(fullName).split("/");
                MasterDocumentKey key = new MasterDocumentKey(pWorkspaceId, partRefs[0], partRefs[1]);
                indexedKeys.add(key);
            }
            return indexedKeys;
        }catch (CorruptIndexException ex) {
            throw new EJBException(ex);
        }catch (IOException ex) {
            throw new EJBException(ex);
        }
    } 
}
